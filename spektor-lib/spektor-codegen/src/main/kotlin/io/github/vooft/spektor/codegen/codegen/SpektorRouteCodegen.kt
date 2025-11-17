package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ExperimentalKotlinPoetApi
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.vooft.spektor.codegen.SpektorCodegenContext
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalKotlinPoetApi::class)
class SpektorRouteCodegen(
    private val config: SpektorCodegenConfig,
    private val context: SpektorCodegenContext,
) {
    fun generate(allPaths: Map<TagAndFile, List<SpektorPath>>) {
        for ((tagAndFile, paths) in allPaths) {
            val className = config.classNameForRoutes(tagAndFile)
            val typeSpec = generateSingleTag(className, tagAndFile, paths)
            context.generatedRouteSpecs[tagAndFile] = TypeAndClass(
                type = typeSpec,
                className = className,
                imports = IMPORTS,
            )
        }
    }

    private fun generateSingleTag(className: ClassName, tagAndFile: TagAndFile, paths: List<SpektorPath>) = TypeSpec.classBuilder(className)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(API_SERVICE_FIELD, config.classNameForServerApi(tagAndFile))
                .build()
        )
        .addProperty(
            PropertySpec.builder(API_SERVICE_FIELD, config.classNameForServerApi(tagAndFile), KModifier.PRIVATE)
                .initializer(API_SERVICE_FIELD)
                .build()
        )
        .also {
            for (path in paths) {
                it.addFunction(generateMethod(path))
            }
        }
        .build()

    private fun generateMethod(path: SpektorPath): FunSpec = FunSpec.builder(path.operationId)
        .contextParameter("route", KTOR_ROUTE_CLASS)
        .addCode(
            CodeBlock.builder()
                .apply {
                    // route start
                    add("route.%L(%S) {\n", path.method.name.lowercase(), path.path)

                    // reading path variables
                    for (variable in path.pathVariables) {
                        addPathVariable(variable)
                    }

                    // reading query variables
                    for (variable in path.queryVariables) {
                        addQueryVariable(variable)
                    }

                    // reading request body
                    val requestBody = path.requestBody
                    if (requestBody != null) {
                        add("  val request = call.receive<%T>()\n", context.resolvedTypes.getValue(requestBody.type))
                    }

                    // adding actual call
                    add("  val response = $API_SERVICE_FIELD.%L(\n", path.operationId)

                    if (requestBody != null) {
                        add("      request = request,\n")
                    }

                    for (variable in path.pathVariables) {
                        add("      ${variable.name} = ${variable.name},\n")
                    }

                    for (variable in path.queryVariables) {
                        add("      ${variable.name} = ${variable.name},\n")
                    }

                    add("      call = call,\n")

                    add("    )\n")

                    // add response
                    add("  call.respond(response)\n")

                    add("}\n")
                }
                .build()
        )
        .build()

    private fun CodeBlock.Builder.addPathVariable(pathVariable: SpektorPath.PathVariable) {
        add(
            "  val %L = %L[%S]?.let { %L -> ",
            pathVariable.name,
            "call.parameters",
            pathVariable.name,
            DEFAULT_VAR_NAME,
        )

        addParseFromString(
            type = pathVariable.type,
            varName = DEFAULT_VAR_NAME,
        )

        add(" }")

        if (pathVariable.required) {
            add(" ?: throw %T(%S)\n", KTOR_BAD_REQUEST_EXCEPTION_CLASS, "Path variable '${pathVariable.name}' is required")
        } else {
            add("\n")
        }
    }

    private fun CodeBlock.Builder.addQueryVariable(queryVariable: SpektorPath.QueryVariable) {
        when (val type = queryVariable.type) {
            is SpektorType.Array -> {
                add(
                    "  val %L = %L.getAll(%S)?.map { %L -> ",
                    queryVariable.name,
                    "call.request.queryParameters",
                    queryVariable.name,
                    DEFAULT_VAR_NAME,
                )

                addParseFromString(
                    type = type.itemType,
                    varName = DEFAULT_VAR_NAME,
                )

                add(" }")
            }

            is SpektorType.MicroType -> {
                add(
                    "  val %L = %L[%S]?.let { %L -> ",
                    queryVariable.name,
                    "call.request.queryParameters",
                    queryVariable.name,
                    DEFAULT_VAR_NAME,
                )

                addParseFromString(
                    type = queryVariable.type,
                    varName = DEFAULT_VAR_NAME,
                )

                add(" }")
            }
        }

        if (queryVariable.required) {
            add(" ?: throw %T(%S)\n", KTOR_BAD_REQUEST_EXCEPTION_CLASS, "Query variable '${queryVariable.name}' is required")
        } else {
            add("\n")
        }
    }

    private fun CodeBlock.Builder.addParseFromString(type: SpektorType, varName: String, parentRef: SpektorType.Ref? = null,) {
        when (type) {
            is SpektorType.MicroType.BooleanMicroType -> add("$varName.toBoolean()")
            is SpektorType.MicroType.IntegerMicroType -> add("$varName.toInt()")
            is SpektorType.MicroType.NumberMicroType -> when (type.format) {
                SpektorType.MicroType.NumberFormat.FLOAT -> add("$varName.toFloat()")
                SpektorType.MicroType.NumberFormat.DOUBLE -> add("$varName.toDouble()")
                SpektorType.MicroType.NumberFormat.BIG_DECIMAL -> add("%T($varName)", java.math.BigDecimal::class)
            }

            is SpektorType.MicroType.StringMicroType -> when (type.format) {
                SpektorType.MicroType.StringFormat.PLAIN -> add(varName)
                SpektorType.MicroType.StringFormat.UUID -> add("%T.fromString($varName)", UUID::class)
                SpektorType.MicroType.StringFormat.URI -> add("%T.create($varName)", URI::class)
                SpektorType.MicroType.StringFormat.DATE_TIME -> add("%T.parse($varName)", Instant::class)
                SpektorType.MicroType.StringFormat.DATE -> add("%T.parse($varName)", LocalDate::class)
            }

            is SpektorType.Ref -> {
                when (val spektorType = context.refs[type]) {
                    is SpektorType.Enum,
                    is SpektorType.MicroType -> addParseFromString(
                        type = spektorType,
                        varName = varName,
                        parentRef = type,
                    )

                    is SpektorType.Object,
                    is SpektorType.Array,
                    is SpektorType.Ref,
                    null -> error("Parsing from string is not supported for $type which refers to $spektorType")
                }
            }

            is SpektorType.Enum -> {
                if (parentRef == null) {
                    error("Parsing from string is not supported for non-referenced enums")
                }
                val generatedTypeSpec = context.generatedTypeSpecs[parentRef]
                    ?: error("Missing generated type spec for ${parentRef.modelName}")

                add("%T.valueOf($varName)", generatedTypeSpec.className)
            }

            is SpektorType.Object,
            is SpektorType.Array -> error("Parsing from string is not supported for $type")
        }
    }

    companion object {
        private val KTOR_ROUTE_CLASS = ClassName("io.ktor.server.routing", "Route")
        private val KTOR_BAD_REQUEST_EXCEPTION_CLASS = ClassName("io.ktor.server.plugins", "BadRequestException")

        private const val API_SERVICE_FIELD = "apiService"

        private val KTOR_METHOD_IMPORTS = SpektorPath.Method.entries
            .map { TypeAndClass.Import("io.ktor.server.routing", it.name.lowercase()) }
            .toSet()

        private val KTOR_RECEIVE_METHOD_IMPORT = TypeAndClass.Import("io.ktor.server.request", "receive")
        private val KTOR_RESPOND_METHOD_IMPORT = TypeAndClass.Import("io.ktor.server.response", "respond")

        /**
         * Required for ktor 2.x versions
         */
        private val KTOR_CALL_EXTENSION_IMPORT = TypeAndClass.Import("io.ktor.server.application", "call")

        private val IMPORTS = buildSet {
            addAll(KTOR_METHOD_IMPORTS)
            add(KTOR_RECEIVE_METHOD_IMPORT)
            add(KTOR_RESPOND_METHOD_IMPORT)
            add(KTOR_CALL_EXTENSION_IMPORT)
        }

        private const val DEFAULT_VAR_NAME = "v"
    }
}

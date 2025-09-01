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
import io.github.vooft.spektor.model.SpektorType.MicroType.OpenApiMicroType
import io.github.vooft.spektor.model.TagAndFile

@OptIn(ExperimentalKotlinPoetApi::class)
class SpektorRouteCodegen(
    private val config: SpektorCodegenConfig,
    private val context: SpektorCodegenContext,
) {
    fun generate(allPaths: Map<TagAndFile, List<SpektorPath>>) {
        for ((tagAndFile, paths) in allPaths) {
            val className = config.classNameForRoutes(tagAndFile)
            val typeSpec = generateSingleTag(className, tagAndFile, paths)
            context.generatedRouteSpecs[tagAndFile] = TypeAndClass(type = typeSpec, className = className)
        }
    }

    private fun generateSingleTag(className: ClassName, tagAndFile: TagAndFile, paths: List<SpektorPath>) = TypeSpec.classBuilder(className)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(API_SERVICE_FIELD_NAME, config.classNameForServerApi(tagAndFile))
                .build()
        )
        .addProperty(
            PropertySpec.builder(API_SERVICE_FIELD_NAME, config.classNameForServerApi(tagAndFile), KModifier.PRIVATE)
                .initializer(API_SERVICE_FIELD_NAME)
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
                        add(
                            "\tval %L = call.parameters[%S]?.let { %L }\n",
                            variable.name,
                            variable.name,
                            variable.type.parseFromString("it")
                        )
                    }

                    // reading query variables
                    for (variable in path.queryVariables) {
                        add(
                            "\tval %L = call.request.queryParameters[%S]?.let { %L }\n",
                            variable.name,
                            variable.name,
                            variable.type.parseFromString("it")
                        )
                    }

                    // reading request body
                    val requestBody = path.requestBody
                    if (requestBody != null) {
                        add("\tval requestBody = call.receive<%T>()\n", context.resolvedTypes.getValue(requestBody.type))
                    }

                    // adding actual call
                    add("\tval response = withApplicationCall {\n")
                    add("\t\t$API_SERVICE_FIELD.%L(\n", path.operationId)
                    if (requestBody != null) {
                        add("\t\t\trequestBody = requestBody,\n")
                    }

                    for (variable in path.pathVariables) {
                        add("\t\t\t${variable.name} = ${variable.name},\n")
                    }

                    for (variable in path.queryVariables) {
                        add("\t\t\t${variable.name} = ${variable.name},\n")
                    }

                    add("\t\t)\n\t}\n")

                    add("}\n")
                }
                .build()
        )
        .build()

    private fun SpektorType.MicroType.parseFromString(varName: String): String = when (type) {
        OpenApiMicroType.STRING -> varName
        OpenApiMicroType.INTEGER -> "$varName.toInt()"
        OpenApiMicroType.BOOLEAN -> "$varName.toBoolean()"
        OpenApiMicroType.NUMBER -> "$varName.toDouble()"
    }

    companion object {
        private val KTOR_ROUTE_CLASS = ClassName("io.ktor.server.routing", "Route")

        private const val API_SERVICE_FIELD_NAME = "apiService"
        private const val API_SERVICE_FIELD = "this.$API_SERVICE_FIELD_NAME"
    }
}

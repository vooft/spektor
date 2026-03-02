package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import io.github.vooft.spektor.codegen.SpektorCodegenContext
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.TagAndFile

class SpektorServerApiCodegen(
    private val config: SpektorCodegenConfig,
    private val context: SpektorCodegenContext,
    private val typeCodegen: SpektorTypeCodegen,
) {
    fun generate(allPaths: Map<TagAndFile, List<SpektorPath>>) {
        for ((tagAndFile, paths) in allPaths) {
            val className = config.classNameForServerApi(tagAndFile)
            val typeSpec = generateSingleTag(className, paths)
            context.generatedPathSpecs[tagAndFile] = TypeAndClass(type = typeSpec, className = className)
        }
    }

    private fun generateSingleTag(className: ClassName, paths: List<SpektorPath>): TypeSpec {
        val interfaceBuilder = TypeSpec.interfaceBuilder(className)

        for (path in paths) {
            val returnType: TypeName
            if (path.responses.isEmpty()) {
                returnType = UNIT_TYPENAME
            } else {
                val responseClassName = className.nestedClass(ResponseNameGenerator.generate(path.operationId))
                returnType = responseClassName
                interfaceBuilder.addType(generateResponseInterface(path, responseClassName))
            }
            interfaceBuilder.addFunction(path.toFunSpec(returnType))
        }

        return interfaceBuilder.build()
    }

    private fun generateResponseInterface(path: SpektorPath, className: ClassName): TypeSpec =
        TypeSpec.interfaceBuilder(className.simpleName)
            .addModifiers(KModifier.SEALED)
            .addProperty(
                PropertySpec.builder("statusCode", HTTP_STATUS_CODE_TYPENAME).build()
            )
            .addProperty(
                PropertySpec.builder("body", ANY.copy(nullable = true)).build()
            )
            .apply {
                for (response in path.responses) {
                    addType(generateResponseClass(response, className))
                }
            }
            .build()

    private fun generateResponseClass(response: SpektorPath.Response, parentInterfaceClass: ClassName): TypeSpec {
        val nestedClassName = response.statusCode.toResponseClassName()
        val bodyTypeName = response.body?.let {
            typeCodegen.generate(it.type).copy(nullable = !it.required)
        }

        return if (bodyTypeName != null) {
            TypeSpec.classBuilder(nestedClassName)
                .addSuperinterface(parentInterfaceClass)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("body", bodyTypeName)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("body", bodyTypeName, KModifier.OVERRIDE)
                        .initializer("body")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("statusCode", HTTP_STATUS_CODE_TYPENAME, KModifier.OVERRIDE)
                        .initializer("%T.fromValue(%L)", HTTP_STATUS_CODE_TYPENAME, response.statusCode)
                        .build()
                )
                .build()
        } else {
            TypeSpec.objectBuilder(nestedClassName)
                .addSuperinterface(parentInterfaceClass)
                .addProperty(
                    PropertySpec.builder("body", ANY.copy(nullable = true), KModifier.OVERRIDE)
                        .initializer("null")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("statusCode", HTTP_STATUS_CODE_TYPENAME, KModifier.OVERRIDE)
                        .initializer("%T.fromValue(%L)", HTTP_STATUS_CODE_TYPENAME, response.statusCode)
                        .build()
                )
                .build()
        }
    }

    private fun SpektorPath.toFunSpec(returnType: TypeName): FunSpec {
        val requestType = requestBody?.let { wrapper ->
            typeCodegen.generate(wrapper.type).copy(nullable = !wrapper.required)
        }

        return FunSpec.builder(operationId)
            .returns(returnType)
            .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
            .apply {
                if (requestType != null) {
                    addParameter("request", requestType)
                }
            }
            .addPathParameters(pathVariables)
            .addQueryParameters(queryVariables)
            .addParameter("call", KTOR_APPLICATION_CALL_TYPENAME)
            .build()
    }

    private fun FunSpec.Builder.addPathParameters(vars: List<SpektorPath.PathVariable>): FunSpec.Builder {
        for (v in vars) {
            val typeName = typeCodegen.generate(v.type)
            addParameter(v.name, typeName.copy(nullable = !v.required))
        }

        return this
    }

    private fun FunSpec.Builder.addQueryParameters(vars: List<SpektorPath.QueryVariable>): FunSpec.Builder {
        for (v in vars) {
            val typeName = typeCodegen.generate(v.type)
            addParameter(v.name, typeName.copy(nullable = !v.required))
        }
        return this
    }

    companion object {
        private val UNIT_TYPENAME = Unit::class.asClassName()
        private val KTOR_APPLICATION_CALL_TYPENAME = ClassName("io.ktor.server.application", "ApplicationCall")
        private val HTTP_STATUS_CODE_TYPENAME = ClassName("io.ktor.http", "HttpStatusCode")

        private val STATUS_CLASS_NAMES = mapOf(
            100 to "Continue",
            101 to "SwitchingProtocols",
            102 to "Processing",
            200 to "Ok",
            201 to "Created",
            202 to "Accepted",
            203 to "NonAuthoritativeInformation",
            204 to "NoContent",
            205 to "ResetContent",
            206 to "PartialContent",
            207 to "MultiStatus",
            300 to "MultipleChoices",
            301 to "MovedPermanently",
            302 to "Found",
            303 to "SeeOther",
            304 to "NotModified",
            305 to "UseProxy",
            306 to "SwitchProxy",
            307 to "TemporaryRedirect",
            308 to "PermanentRedirect",
            400 to "BadRequest",
            401 to "Unauthorized",
            402 to "PaymentRequired",
            403 to "Forbidden",
            404 to "NotFound",
            405 to "MethodNotAllowed",
            406 to "NotAcceptable",
            407 to "ProxyAuthenticationRequired",
            408 to "RequestTimeout",
            409 to "Conflict",
            410 to "Gone",
            411 to "LengthRequired",
            412 to "PreconditionFailed",
            413 to "PayloadTooLarge",
            414 to "RequestUriTooLong",
            415 to "UnsupportedMediaType",
            416 to "RequestedRangeNotSatisfiable",
            417 to "ExpectationFailed",
            422 to "UnprocessableEntity",
            423 to "Locked",
            424 to "FailedDependency",
            425 to "TooEarly",
            426 to "UpgradeRequired",
            429 to "TooManyRequests",
            431 to "RequestHeaderFieldTooLarge",
            500 to "InternalServerError",
            501 to "NotImplemented",
            502 to "BadGateway",
            503 to "ServiceUnavailable",
            504 to "GatewayTimeout",
            505 to "VersionNotSupported",
            506 to "VariantAlsoNegotiates",
            507 to "InsufficientStorage",
        )

        private fun Int.toResponseClassName(): String = STATUS_CLASS_NAMES[this] ?: "Status$this"
    }
}

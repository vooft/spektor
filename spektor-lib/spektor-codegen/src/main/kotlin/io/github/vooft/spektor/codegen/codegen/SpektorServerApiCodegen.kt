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

        private fun Int.toResponseClassName(): String = when (this) {
            100 -> "Continue"
            101 -> "SwitchingProtocols"
            102 -> "Processing"
            200 -> "Ok"
            201 -> "Created"
            202 -> "Accepted"
            203 -> "NonAuthoritativeInformation"
            204 -> "NoContent"
            205 -> "ResetContent"
            206 -> "PartialContent"
            207 -> "MultiStatus"
            300 -> "MultipleChoices"
            301 -> "MovedPermanently"
            302 -> "Found"
            303 -> "SeeOther"
            304 -> "NotModified"
            305 -> "UseProxy"
            306 -> "SwitchProxy"
            307 -> "TemporaryRedirect"
            308 -> "PermanentRedirect"
            400 -> "BadRequest"
            401 -> "Unauthorized"
            402 -> "PaymentRequired"
            403 -> "Forbidden"
            404 -> "NotFound"
            405 -> "MethodNotAllowed"
            406 -> "NotAcceptable"
            407 -> "ProxyAuthenticationRequired"
            408 -> "RequestTimeout"
            409 -> "Conflict"
            410 -> "Gone"
            411 -> "LengthRequired"
            412 -> "PreconditionFailed"
            413 -> "PayloadTooLarge"
            414 -> "RequestUriTooLong"
            415 -> "UnsupportedMediaType"
            416 -> "RequestedRangeNotSatisfiable"
            417 -> "ExpectationFailed"
            422 -> "UnprocessableEntity"
            423 -> "Locked"
            424 -> "FailedDependency"
            425 -> "TooEarly"
            426 -> "UpgradeRequired"
            429 -> "TooManyRequests"
            431 -> "RequestHeaderFieldTooLarge"
            500 -> "InternalServerError"
            501 -> "NotImplemented"
            502 -> "BadGateway"
            503 -> "ServiceUnavailable"
            504 -> "GatewayTimeout"
            505 -> "VersionNotSupported"
            506 -> "VariantAlsoNegotiates"
            507 -> "InsufficientStorage"
            else -> "Status$this"
        }
    }
}

package io.github.vooft.spektor.codegen.codegen

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
                val responseClassName = className.nestedClass(ResponseClassNameGenerator.generate(path.operationId))
                returnType = responseClassName
                interfaceBuilder.addType(generateResponseInterface(path, responseClassName))
            }
            interfaceBuilder.addFunction(path.toFunSpec(returnType))
        }

        return interfaceBuilder.build()
    }

    private fun generateResponseInterface(path: SpektorPath, className: ClassName): TypeSpec {
        val companionBuilder = TypeSpec.companionObjectBuilder()

        return TypeSpec.interfaceBuilder(className.simpleName)
            .addModifiers(KModifier.SEALED)
            .addProperty(PropertySpec.builder("statusCode", HTTP_STATUS_CODE_TYPENAME).build())
            .apply {
                for (response in path.responses) {
                    val bodyTypeName = response.body?.let {
                        typeCodegen.generate(it.type).copy(nullable = !it.required)
                    }

                    val typeSpec = generateResponseClass(
                        response = response,
                        parentInterfaceClass = className,
                        bodyTypeName = bodyTypeName,
                    )
                    addType(typeSpec)

                    val responseClassName = typeSpec.name ?: error("Response class name is null")
                    val factoryName = responseClassName.replaceFirstChar { it.lowercase() }
                    val factoryFun = if (bodyTypeName != null) {
                        FunSpec.builder(factoryName)
                            .addParameter("body", bodyTypeName)
                            .returns(className)
                            .addStatement("return %L(body)", responseClassName)
                            .build()
                    } else {
                        FunSpec.builder(factoryName)
                            .returns(className)
                            .addStatement("return %L", responseClassName)
                            .build()
                    }
                    companionBuilder.addFunction(factoryFun)
                }
            }
            .addType(companionBuilder.build())
            .build()
    }

    private fun generateResponseClass(response: SpektorPath.Response, parentInterfaceClass: ClassName, bodyTypeName: TypeName?,): TypeSpec {
        val nestedClassName = ResponseStatusClassNameGenerator.generate(response.statusCode)
        return if (bodyTypeName != null) {
            TypeSpec.classBuilder(nestedClassName)
                .addSuperinterface(parentInterfaceClass)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("body", bodyTypeName)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("body", bodyTypeName)
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
    }
}

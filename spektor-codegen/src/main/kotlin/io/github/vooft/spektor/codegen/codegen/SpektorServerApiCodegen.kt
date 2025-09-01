package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
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
    private val typeCodegen: SpektorTypeCodegen
) {
    fun generate(allPaths: Map<TagAndFile, List<SpektorPath>>) {
        for ((tagAndFile, paths) in allPaths) {
            val className = config.classNameForServerApi(tagAndFile)
            val typeSpec = generateSingleTag(className, paths)
            context.generatedPathSpecs[tagAndFile] = TypeAndClass(type = typeSpec, className = className)
        }
    }

    private fun generateSingleTag(className: ClassName, paths: List<SpektorPath>) = TypeSpec.interfaceBuilder(className)
        .also { builder -> paths.forEach { builder.addFunction(it.toFunSpec()) } }
        .build()

    private fun SpektorPath.toFunSpec(): FunSpec {
        val returnType = responseBody?.let { wrapper ->
            typeCodegen.generate(wrapper.type).copy(nullable = !wrapper.required)
        } ?: UNIT_TYPENAME

        val requestType = requestBody?.let { wrapper ->
            typeCodegen.generate(wrapper.type).copy(nullable = !wrapper.required)
        }

        return FunSpec.builder(operationId)
            .returns(returnType)
            .addModifiers(KModifier.ABSTRACT)
            .apply {
                if (requestType != null) {
                    addParameter("requestBody", requestType)
                }
            }
            .addParameters(pathVariables)
            .addParameters(queryVariables)
            .build()
    }

    private fun FunSpec.Builder.addParameters(vars: List<SpektorPath.Variable>): FunSpec.Builder {
        for (v in vars) {
            val typeName = typeCodegen.generate(v.type)
            addParameter(v.name, typeName.copy(nullable = !v.required))
        }

        return this
    }

    companion object {
        private val UNIT_TYPENAME = Unit::class.asClassName()
    }
}

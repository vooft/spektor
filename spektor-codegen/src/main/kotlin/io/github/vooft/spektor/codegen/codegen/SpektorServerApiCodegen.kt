package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import io.github.vooft.spektor.codegen.SpektorCodegenContext
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorPath

class SpektorServerApiCodegen(
    private val config: SpektorCodegenConfig,
    private val context: SpektorCodegenContext,
    private val typeCodegen: SpektorTypeCodegen
) {
    fun generate(paths: List<SpektorPath>) {
        val pathsByTag = paths.groupBy { it.tag }

        val multipleFilesPerTag = pathsByTag.mapValues { (_, list) -> list.map { it.file }.distinct() }
            .filterValues { it.size > 1 }

        require(multipleFilesPerTag.isEmpty()) {
            "Paths with the same tag should be in the same file, but got: $multipleFilesPerTag"
        }

        pathsByTag.map { (_, paths) ->
            val typeSpec = generateSingleTag(paths)
            val className = config.classNameFor(paths.first())
            TypeAndClass(type = typeSpec, className = className)
        }.forEach { context.generatedPathSpecs.add(it) }
    }

    private fun generateSingleTag(paths: List<SpektorPath>): TypeSpec = TypeSpec.interfaceBuilder(config.classNameFor(paths.first()))
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

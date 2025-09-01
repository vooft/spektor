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

class SpektorServerApiCodegen(
    private val config: SpektorCodegenConfig,
    private val context: SpektorCodegenContext,
    private val typeCodegen: SpektorTypeCodegen
) {
    fun generate(paths: List<SpektorPath>) {
        paths.validateSingleFilePerTag()
        val byTagAndFile = paths.groupBy { it.tagAndFile }

        byTagAndFile.map { (tagAndFile, paths) ->
            val className = config.classNameFor(tagAndFile)
            val typeSpec = generateSingleTag(className, paths)
            TypeAndClass(type = typeSpec, className = className)
        }.forEach { context.generatedPathSpecs.add(it) }
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

    private fun List<SpektorPath>.validateSingleFilePerTag() {
        val pathsByTag = groupBy { it.tagAndFile.tag }
        val multipleFilesPerTag = pathsByTag.mapValues { (_, paths) -> paths.map { it.tagAndFile.path }.distinct() }
            .filter { it.value.size > 1 }

        require(multipleFilesPerTag.isEmpty()) {
            "Paths with the same tag should be in the same file, but got: $multipleFilesPerTag"
        }
    }

    companion object {
        private val UNIT_TYPENAME = Unit::class.asClassName()
    }
}

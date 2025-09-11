package io.github.vooft.spektor.codegen

import com.squareup.kotlinpoet.ClassName
import io.github.vooft.spektor.codegen.codegen.SpektorRouteCodegen
import io.github.vooft.spektor.codegen.codegen.SpektorServerApiCodegen
import io.github.vooft.spektor.codegen.codegen.SpektorTypeCodegen
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.writer.SpektorClassWriter
import io.github.vooft.spektor.model.SpektorSchema
import java.nio.file.Path

class SpektorCodegen(
    private val config: SpektorCodegenConfig
) {

    fun generateAndWrite(schema: SpektorSchema, outputRoot: Path) {
        val context = generate(schema)
        write(context, outputRoot)
    }

    fun generate(schema: SpektorSchema): SpektorCodegenContext {
        val context = SpektorCodegenContext(schema.paths, schema.refs)
        context.substituteRefsFromConfig()

        val typeCodegen = SpektorTypeCodegen(config, context)
        val apiCodegen = SpektorServerApiCodegen(config, context, typeCodegen)
        val routeCodegen = SpektorRouteCodegen(config, context)

        apiCodegen.generate(schema.paths)
        routeCodegen.generate(schema.paths)

        return context
    }

    fun write(context: SpektorCodegenContext, outputRoot: Path) {
        val classWriter = SpektorClassWriter(outputRoot)
        for ((_, typeAndClass) in context.generatedTypeSpecs) {
            classWriter.write(typeAndClass)
        }

        for ((_, typeAndClass) in context.generatedRouteSpecs) {
            classWriter.write(typeAndClass)
        }

        for ((_, typeAndClass) in context.generatedPathSpecs) {
            classWriter.write(typeAndClass)
        }
    }

    private fun SpektorCodegenContext.substituteRefsFromConfig() {
        for ((ref, clazz) in config.dtoSubstitutions) {
            resolvedTypes[ref] = ClassName.bestGuess(clazz)
        }
    }
}

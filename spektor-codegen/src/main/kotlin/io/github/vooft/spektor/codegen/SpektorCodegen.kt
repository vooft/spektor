package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.codegen.SpektorTypeCodegen
import io.github.vooft.spektor.codegen.writer.SpektorClassWriter
import io.github.vooft.spektor.model.SpektorSchema
import java.nio.file.Path

class SpektorCodegen(
    private val specRoot: Path,
    private val packagePrefix: String
) {

    fun generateAndWrite(schema: SpektorSchema, outputRoot: Path) {
        val context = generate(schema)
        write(context, outputRoot)
    }

    fun generate(schema: SpektorSchema): SpektorCodegenContext {
        val context = SpektorCodegenContext(specRoot, packagePrefix, schema.paths, schema.refs)
        val typeCodegen = SpektorTypeCodegen(context)
        for ((ref, type) in schema.refs) {
            if (type is io.github.vooft.spektor.model.SpektorType.Object) {
                typeCodegen.generate(ref)
            }
        }

        return context
    }

    fun write(context: SpektorCodegenContext, outputRoot: Path) {
        val classWriter = SpektorClassWriter(outputRoot, context)
        for ((ref, _) in context.generatedTypeSpecs) {
            classWriter.write(ref)
        }
    }
}

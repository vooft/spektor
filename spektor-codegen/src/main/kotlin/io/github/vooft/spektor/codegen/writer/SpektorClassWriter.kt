package io.github.vooft.spektor.codegen.writer

import com.squareup.kotlinpoet.FileSpec
import io.github.vooft.spektor.codegen.SpektorCodegenContext
import io.github.vooft.spektor.model.SpektorType
import java.nio.file.Path

class SpektorClassWriter(private val outputRoot: Path, private val context: SpektorCodegenContext) {
    fun write(ref: SpektorType.Ref) {
        val className = context.classNameFor(ref)

        FileSpec.Companion.builder(className)
            .addType(context.generatedTypeSpecs.getValue(ref))
            .build()
            .writeTo(outputRoot)
    }
}

package io.github.vooft.spektor.codegen.writer

import com.squareup.kotlinpoet.FileSpec
import io.github.vooft.spektor.codegen.SpektorCodegenContext
import io.github.vooft.spektor.model.SpektorType
import java.nio.file.Path

class SpektorClassWriter(private val outputRoot: Path, private val context: SpektorCodegenContext) {
    fun write(ref: SpektorType.Ref) {
        val (type, className) = context.generatedTypeSpecs.getValue(ref)

        FileSpec.builder(className)
            .addType(type)
            .build()
            .writeTo(outputRoot)
    }
}

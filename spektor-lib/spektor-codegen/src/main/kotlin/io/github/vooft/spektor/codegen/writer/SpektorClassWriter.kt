package io.github.vooft.spektor.codegen.writer

import com.squareup.kotlinpoet.FileSpec
import io.github.vooft.spektor.codegen.common.TypeAndClass
import java.nio.file.Path

class SpektorClassWriter(private val outputRoot: Path) {
    fun write(typeAndClass: TypeAndClass) {
        FileSpec.builder(typeAndClass.className)
            .addType(typeAndClass.type)
            .build()
            .writeTo(outputRoot)
    }
}

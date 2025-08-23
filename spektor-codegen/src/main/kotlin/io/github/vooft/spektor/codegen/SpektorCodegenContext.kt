package io.github.vooft.spektor.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import java.nio.file.Path

class SpektorCodegenContext(
    val specRoot: Path,
    val packagePrefix: String,
    val paths: List<SpektorPath>,
    val refs: Map<SpektorType.Ref, SpektorType>,
    val generatedTypeSpecs: MutableMap<SpektorType.Ref, TypeSpec> = mutableMapOf(),
) {
    val specRootNormalized = this@SpektorCodegenContext.specRoot.toAbsolutePath().normalize().toString()

    fun classNameFor(ref: SpektorType.Ref): ClassName {
        val packageName = listOf(packagePrefix, ref.file.toPackageName()).joinToString(".")
        return ClassName(packageName, ref.modelName)
    }

    private fun Path.toPackageName(): String {
        val thisNormalized = this.toAbsolutePath().normalize().toString()
        val relativePath = thisNormalized.removePrefix(specRootNormalized).removePrefix("/").substringBeforeLast('/')
        return relativePath.replace("/", ".")
    }
}

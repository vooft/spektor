package io.github.vooft.spektor.codegen.common

import com.squareup.kotlinpoet.ClassName
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import java.nio.file.Path

data class SpektorCodegenConfig(
    val basePackage: String,
    val specRoot: Path,
    val dtoSuffix: String = "Dto",
    val serverApiSuffix: String = "ServerApi"
) {
    fun classNameFor(ref: SpektorType.Ref): ClassName {
        val packageName = listOf(basePackage, ref.file.toPackageName(specRoot)).joinToString(".")
        return ClassName(packageName, ref.modelName + dtoSuffix)
    }

    fun classNameFor(tagAndFile: SpektorPath.TagAndFile): ClassName {
        val packageName = listOf(basePackage, tagAndFile.path.toPackageName(specRoot)).joinToString(".")
        return ClassName(packageName, tagAndFile.tag + serverApiSuffix)
    }
}

fun Path.toPackageName(specRoot: Path): String {
    val specRootNormalized = specRoot.toAbsolutePath().normalize().toString()
    val thisNormalized = this.toAbsolutePath().normalize().toString()
    val relativePath = thisNormalized.removePrefix(specRootNormalized).removePrefix("/").substringBeforeLast('/')
    return relativePath.replace("/", ".")
}

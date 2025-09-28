package io.github.vooft.spektor.codegen.common

import com.squareup.kotlinpoet.ClassName
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile
import java.nio.file.Path

data class SpektorCodegenConfig(
    val basePackage: String,
    val specRoot: Path,
    val dtoSuffix: String = "Dto",
    val serverApiSuffix: String = "ServerApi",
    val routesSuffix: String = "Routes",
    val dtoSubstitutions: Map<SpektorType.Ref, String> = emptyMap(),
    val microtypeSubstitutions: Map<SpektorPropertyRef, String> = emptyMap()
) {
    fun classNameFor(ref: SpektorType.Ref): ClassName {
        val packageName = listOf(basePackage, ref.file.toPackageName(specRoot)).joinToString(".")
        return ClassName(packageName, ref.modelName + dtoSuffix)
    }

    fun classNameForServerApi(tagAndFile: TagAndFile): ClassName {
        val packageName = listOf(basePackage, tagAndFile.path.toPackageName(specRoot)).joinToString(".")
        return ClassName(packageName, tagAndFile.tag + serverApiSuffix)
    }

    fun classNameForRoutes(tagAndFile: TagAndFile): ClassName {
        val packageName = listOf(basePackage, tagAndFile.path.toPackageName(specRoot)).joinToString(".")
        return ClassName(packageName, tagAndFile.tag + routesSuffix)
    }
}

data class SpektorPropertyRef(val ref: SpektorType.Ref, val propertyName: String)

fun Path.toPackageName(specRoot: Path): String {
    val specRootNormalized = specRoot.toAbsolutePath().normalize().toString()
    val thisNormalized = this.toAbsolutePath().normalize().toString()

    val packageParts = thisNormalized.removePrefix(specRootNormalized)
        .removePrefix("/")
        .substringBeforeLast('/')
        .split('/') + this.toFile().nameWithoutExtension.lowercase()

    return packageParts.joinToString(".") { it.sanitizeKotlinIdentifier() }
}

private fun String.sanitizeKotlinIdentifier(): String = replace("[^A-Za-z0-9_]".toRegex(), "_").let {
    if (it.firstOrNull()?.isDigit() == true) "_$it" else it
}

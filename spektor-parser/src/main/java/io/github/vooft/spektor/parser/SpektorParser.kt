package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorSchema
import io.github.vooft.spektor.model.SpektorType
import java.nio.file.Path

class SpektorParser {
    fun parse(files: List<Path>): SpektorSchema {
        val absolutePaths = files.map { it.toAbsolutePath().normalize() }

        val spektorFiles = mutableMapOf<Path, SpektorFile>()
        val resultPaths = absolutePaths.map { path ->
            SpektorFile(path).also { spektorFiles[path] = it }
        }.flatMap { it.parsePaths() }

        val allRefs = resultPaths.flatMap { it.extractRefs() }.toSet()

        val resultRefs = mutableMapOf<SpektorType.Ref, SpektorType>()
        for (ref in allRefs) {
            val file = spektorFiles.computeIfAbsent(ref.file) { SpektorFile(it) }
            val type = file.parseModel(ref)
            if (type == null) {
                logger.warn { "Cannot resolve reference $ref in file ${ref.file}" }
                continue
            }

            resultRefs[ref] = type
        }

        return SpektorSchema(paths = resultPaths, refs = resultRefs.toMap())
    }

    private fun SpektorPath.extractRefs(): Set<SpektorType.Ref> {
        val reqBodyRefs = requestBody?.type?.extractRefs() ?: emptySet()
        val respBodyRefs = responseBody?.type?.extractRefs() ?: emptySet()
        val pathVarRefs = pathVariables.flatMap { it.type.extractRefs() }.toSet()
        val queryVarRefs = queryVariables.flatMap { it.type.extractRefs() }.toSet()
        return reqBodyRefs + respBodyRefs + pathVarRefs + queryVarRefs
    }

    private fun SpektorType.extractRefs(): Set<SpektorType.Ref> = when (this) {
        is SpektorType.MicroType -> emptySet()
        is SpektorType.List -> itemType.extractRefs()
        is SpektorType.Object -> properties.values.flatMap { it.type.extractRefs() }.toSet()
        is SpektorType.Ref -> setOf(this)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}

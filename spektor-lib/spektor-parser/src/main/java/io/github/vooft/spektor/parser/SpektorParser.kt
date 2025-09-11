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

        val allRefs = mutableSetOf<SpektorType.Ref>()

        val resultPaths = absolutePaths.map { path ->
            SpektorFile(path, allRefs).also { spektorFiles[path] = it }
        }.flatMap { it.parsePaths() }

        val resultRefs = mutableMapOf<SpektorType.Ref, SpektorType>()
        while (allRefs.isNotEmpty()) {
            val ref = allRefs.first()
            allRefs.remove(ref)

            val file = spektorFiles.computeIfAbsent(ref.file) { SpektorFile(it, allRefs) }
            val type = file.parseModel(ref)
            if (type == null) {
                logger.warn { "Cannot resolve reference $ref in file ${ref.file}" }
                continue
            }

            resultRefs[ref] = type

            allRefs.removeAll(resultRefs.keys)
        }

        resultPaths.validateSingleFilePerTag()

        return SpektorSchema(paths = resultPaths.groupBy { it.tagAndFile }, refs = resultRefs.toMap())
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
        private val logger = KotlinLogging.logger { }
    }
}

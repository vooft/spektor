package io.github.vooft.spektor.merger

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.github.vooft.spektor.merger.SpektorMerger.Companion.isNotExcluded
import io.github.vooft.spektor.merger.SpektorMerger.Companion.isYaml
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.io.path.walk

class SpektorMergeTraverser(
    private val specRoot: Path,
) {
    // TODO: migrate to SpektorFile
    fun resolveAllSpecFiles(unifiedSpecName: String): Map<String, Path> = specRoot.walk()
        .filter { it.isYaml() && it.isNotExcluded(listOf(unifiedSpecName)) }
        .flatMap { file ->
            logger.debug { "Processing $file" }

            val parseResult = OpenAPIV3Parser().readContents(
                file.readText(),
                listOf(),
                ParseOptions().apply { isResolve = true },
                file.absolutePathString()
            )

            parseResult.messages?.takeIf { it.isNotEmpty() }?.let {
                throw InvalidOpenApiSpecException(file, it)
            }

            val tree = parseResult.openAPI

            tree.paths?.let {
                it.entries.map { (key, _) -> key to file }
            } ?: emptyList()
        }.groupBy(
            { (pathKey, _) -> pathKey },
            { (_, file) -> file }
        ).also {
            val duplicates = it.filter { (_, entries) -> entries.size > 1 }
            require(duplicates.isEmpty()) { "Duplicate paths in api files: $duplicates" }
        }.mapValues { (_, entries) -> entries.single() }

    companion object {
        private val logger = logger { }
    }
}

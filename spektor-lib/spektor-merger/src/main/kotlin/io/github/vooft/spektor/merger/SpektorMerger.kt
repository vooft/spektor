package io.github.vooft.spektor.merger

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.parser.OpenAPIV3Parser
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.walk
import kotlin.io.path.writeText

class SpektorMerger(
    val unifiedSpecName: String,
    val unifiedSpecTitle: String,
    val unifiedSpecDescription: String?,
    val servers: List<String>,
    val specRoot: Path
) {

    init {
        require(specRoot.isDirectory()) { "Spec root must be a directory" }
        logger.debug { "YAML mapper version: ${yamlMapper.version()}" }
    }

    companion object {
        private val logger = logger { }
        private val yamlMapper = ObjectMapper(
            YAMLFactory().apply {
                disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            }
        ).apply {
            findAndRegisterModules()
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }

        fun Path.isYaml() = extension == "yaml" || extension == "yml"
        fun Path.isNotExcluded(ignoredNames: List<String>) = name !in ignoredNames.flatMap {
            listOf(
                "$it.yaml",
                "$it.yml",
                "$it.json",
                "synthetic_$it.yaml"
            )
        }
    }

    fun merge() = runCatching {
        val unifiedSpec = buildUnifiedSpec()

        val yamlOut = specRoot.resolve("$unifiedSpecName.yaml")

        logger.debug { "Writing unified OpenAPI spec to $yamlOut" }
        yamlOut.writeText(yamlMapper.writeValueAsString(unifiedSpec))
    }.onFailure { logger.error(it) { "Failed to merge OpenAPI specs" } }

    private fun escapeJsonPointerSegment(segment: String): String = segment.replace("~", "~0")
        .replace("/", "~1")

    private fun buildUnifiedSpec(): OpenAPI {
        logger.debug { "Building synthetic root OpenAPI spec YAML" }
        val openapi = OpenAPI().apply {
            openapi = "3.0.1"
            info = Info().apply {
                title = unifiedSpecTitle
                description = unifiedSpecDescription
                version = "1.0.0"
            }
            this.servers = this@SpektorMerger.servers.map { url -> Server().url(url) }
            paths = Paths()
        }

        logger.debug { "Walking $specRoot" }
        val pathFiles = specRoot.walk()
            .filter { it.isYaml() && it.isNotExcluded(listOf(unifiedSpecName)) }
            .flatMap { file ->
                logger.debug { "Processing $file" }
                val tree = OpenAPIV3Parser().read(file.toAbsolutePath().toUri().toString())

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

        logger.debug { "Merging paths" }
        pathFiles.forEach { (pathKey, file) ->
            logger.debug { "Merging path '$pathKey' from $file" }
            val relativeLocation = file.relativePathTo(specRoot)
            val escapedPath = escapeJsonPointerSegment(pathKey)

            openapi.path(
                pathKey,
                PathItem().apply {
                    this.`$ref` = "./$relativeLocation#/paths/$escapedPath"
                }
            )
        }

        return openapi
    }

    private fun Path.relativePathTo(other: Path) = other.relativize(this).toString().replace('\\', '/')
}

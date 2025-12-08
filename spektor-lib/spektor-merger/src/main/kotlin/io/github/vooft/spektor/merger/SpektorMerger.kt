package io.github.vooft.spektor.merger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.walk
import kotlin.io.path.writeText

class SpektorMerger(
    val unifiedSpecName: String,
    val unifiedSpecTitle: String,
    val unifiedSpecDescription: String?,
    val specRoot: Path
) {

    init {
        require(specRoot.isDirectory()) { "Spec root must be a directory" }
        logger.debug { "YAML mapper version: ${yamlMapper.version()}" }
        logger.debug { "JSON mapper version: ${jsonMapper.version()}" }
    }

    companion object {
        private val logger = logger { }
        private val yamlMapper = ObjectMapper(YAMLFactory()).findAndRegisterModules()
        private val jsonMapper = ObjectMapper().findAndRegisterModules()

        private val HTTP_METHODS = setOf(
            "get",
            "put",
            "post",
            "delete",
            "options",
            "head",
            "patch",
            "trace"
        )

        private const val REF = $$"$ref"

        fun Path.isYaml() = extension == "yaml" || extension == "yml"
        fun Path.isNotExcluded(ignoredNames: List<String>) = name !in ignoredNames.flatMap {
            listOf(
                "$it.yaml",
                "$it.yml",
                "$it.json"
            )
        }
    }

    fun merge() = runCatching {
        val syntheticYaml = buildSyntheticRootYaml()
        val resolvedOpenApi = resolveWithSwagger(syntheticYaml)

        val yamlOut = specRoot.resolve("$unifiedSpecName.yaml")
        val jsonOut = specRoot.resolve("$unifiedSpecName.json")

        logger.debug { "Writing unified OpenAPI spec to $yamlOut" }
        yamlOut.writeText(yamlMapper.writeValueAsString(resolvedOpenApi))

        logger.debug { "Writing unified OpenAPI spec to $jsonOut" }
        jsonOut.writeText(jsonMapper.writeValueAsString(resolvedOpenApi))
    }.onFailure { logger.error(it) { "Failed to merge OpenAPI specs" } }

    private fun resolveWithSwagger(syntheticYaml: String,): OpenAPI {
        logger.debug { "Resolving synthetic OpenAPI root" }
        val options = ParseOptions().apply {
            isResolve = true
        }

        val result = OpenAPIV3Parser().readContents(
            /* swaggerAsString = */
            syntheticYaml,
            /* auth = */
            null,
            /* options = */
            options,
            /* location = */
            specRoot.toUri().toString()
        )

        return result.openAPI ?: error("Failed to parse/resolve synthetic OpenAPI root")
    }

    private fun escapeJsonPointerSegment(segment: String): String = segment.replace("~", "~0")
        .replace("/", "~1")

    private fun buildSyntheticRootYaml(): String {
        logger.debug { "Building synthetic root OpenAPI spec YAML" }
        val root: ObjectNode = yamlMapper.createObjectNode().apply {
            put("openapi", "3.0.1")
            putObject("info").apply {
                put("title", unifiedSpecTitle)
                put("version", "1.0.0")
                put("description", unifiedSpecDescription)
            }
        }

        val pathsNode = root.putObject("paths")

        logger.debug { "Walking $specRoot" }
        val pathEntries = specRoot.walk()
            .filter { it.isYaml() && it.isNotExcluded(listOf(unifiedSpecName)) }
            .flatMap { file ->
                logger.debug { "Processing $file" }
                val tree = yamlMapper.readTree(file.inputStream())

                tree.get("paths")?.takeIf { it.isObject }?.let {
                    (it as ObjectNode).properties().map { (pathKey, pathItemNode) ->
                        Triple(pathKey, file, pathItemNode as ObjectNode)
                    }
                } ?: emptyList()
            }.groupBy(
                { (pathKey, _, _) -> pathKey },
                { (_, file, pathItemNode) -> file to pathItemNode }
            )

        logger.debug { "Merging paths" }
        pathEntries.forEach { (pathKey, pathFileEntries) ->
            logger.debug { "Merging path '$pathKey', files: ${pathFileEntries.size}" }
            if (pathFileEntries.size == 1) {
                logger.debug { "Path '$pathKey' is unique, adding file reference" }
                val (file, _) = pathFileEntries.single()
                val relativeLocation = file.relativePathTo(specRoot)
                val escapedPath = escapeJsonPointerSegment(pathKey)

                val refObj = pathsNode.putObject(pathKey)
                refObj.put(REF, "./$relativeLocation#/paths/$escapedPath")
            } else {
                logger.debug {
                    "Path '$pathKey' is duplicated, merging method entries from: ${pathFileEntries.map { it.first }.joinToString()}"
                }
                val mergedPath = pathsNode.putObject(pathKey)

                pathFileEntries.flatMap { (file, pathItemNode) ->
                    HTTP_METHODS.mapNotNull { method ->
                        pathItemNode.get(method)?.let {
                            Triple(method, file, it.deepCopy<ObjectNode>())
                        }
                    }
                }.groupBy { (method, _, _) -> method }
                    .also {
                        val duplicates = it.filter { (_, entries) -> entries.size > 1 }
                        require(duplicates.isEmpty()) { "Duplicate HTTP methods for path '$pathKey': $duplicates" }
                    }
                    .forEach { (method, entries) ->
                        val (_, file, pathMethodNode) = entries.single()
                        rewriteRefsInPlace(pathMethodNode, file)
                        mergedPath.set<ObjectNode>(method, pathMethodNode)
                    }
            }
        }

        return yamlMapper.writeValueAsString(root)
    }

    private fun rewriteRefsInPlace(node: JsonNode, sourceFile: Path,) {
        when {
            node.isObject -> {
                (node as ObjectNode).properties().forEach { (name, child) ->
                    if (name == REF && child.isTextual) {
                        val newRef = adjustRefForSource(child.asText(), sourceFile)
                        node.put(REF, newRef)
                    } else {
                        rewriteRefsInPlace(child, sourceFile)
                    }
                }
            }

            node.isArray -> node.forEach { child ->
                rewriteRefsInPlace(child, sourceFile)
            }

            else -> Unit
        }
    }

    private fun adjustRefForSource(ref: String, sourceFile: Path,): String {
        if (ref.startsWith("#") ||
            ref.startsWith("http://") ||
            ref.startsWith("https://")
        ) {
            return ref
        }

        val index = ref.indexOf('#')
        val filePart = if (index >= 0) ref.take(index) else ref
        val fragmentPart = if (index >= 0) ref.substring(index) else ""

        if (filePart.isEmpty()) {
            return ref
        }

        val resolvedFile = sourceFile.parent.resolve(filePart).normalize()
        val relToRoot = resolvedFile.relativePathTo(specRoot)

        return "./$relToRoot$fragmentPart"
    }

    private fun Path.relativePathTo(other: Path) = other.relativize(this).toString().replace('\\', '/')
}

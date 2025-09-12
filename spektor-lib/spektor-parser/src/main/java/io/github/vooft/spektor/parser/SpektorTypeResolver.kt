package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorType
import io.swagger.v3.oas.models.media.Schema
import java.net.URI
import java.nio.file.Path

class SpektorTypeResolver(private val file: Path, private val allRefs: MutableSet<SpektorType.Ref>) {

    fun resolve(schema: Schema<*>): SpektorType? = when {
        schema.`$ref` != null -> resolveRef(schema.`$ref`)?.also { allRefs.add(it) }
        schema.type == "object" -> resolveObject(schema)
        schema.type == "array" -> resolveArray(schema)
        schema.type == "string" && schema.enum != null -> SpektorType.Enum(schema.enum.map { it.toString() })
        schema.type != null -> SpektorType.MicroType.from(schema.type, schema.format)
        else -> {
            logger.warn { "Schema in file $file is not a reference or primitive type: $schema" }
            null
        }
    }

    private fun resolveArray(schema: Schema<*>): SpektorType.Array? {
        val itemsRef = schema.items ?: run {
            logger.warn { "Array schema has no items: $schema" }
            return null
        }

        val itemType = resolve(itemsRef) ?: run {
            logger.warn { "Cannot resolve item type in array schema: $itemsRef" }
            return null
        }

        return SpektorType.Array(itemType)
    }

    private fun resolveObject(schema: Schema<*>): SpektorType.Object? {
        val required = schema.required?.toSet() ?: emptySet()
        val properties = schema.properties ?: run {
            logger.warn { "Object schema has no properties: $schema" }
            return null
        }

        val props = properties.mapValues { (propName, propSchema) ->
            val propertyModel = resolve(propSchema) ?: run {
                logger.warn { "Cannot resolve model for property $propName: $propSchema" }
                return null
            }

            SpektorType.RequiredWrapper(propertyModel, required.contains(propName))
        }

        return SpektorType.Object(props)
    }

    private fun resolveRef(rawRef: String): SpektorType.Ref? {
        // check if a URI reference
        try {
            val uri = URI(rawRef)
            if (uri.scheme != null) {
                logger.warn { "Found a URI reference in file $file, which is not supported: $rawRef" }
                return null
            }
        } catch (_: Exception) {
            // ignore, not a valid URI
        }

        // then probably it is either a file reference or a local one
        val refPath = rawRef.extractFilePath() ?: run {
            logger.warn { "Found invalid reference $rawRef in file $file, expected to start with #/ or a valid file path" }
            return null
        }

        val refName = rawRef.substringAfter("#/").extractRefName() ?: return null

        return SpektorType.Ref(file.parent.resolve(refPath), refName)
    }

    private fun String.extractFilePath(): Path? {
        // check if it is a local reference
        if (startsWith("#/")) {
            return file
        }

        val fileSeparatorIndex = indexOf("#/")
        if (fileSeparatorIndex == -1) {
            return null
        }

        return file.parent.resolve(take(fileSeparatorIndex)).toRealPath()
    }

    private fun String.extractRefName(): String? {
        val refNameStartIndex = lastIndexOf('/') + 1
        if (take(refNameStartIndex) != "components/schemas/") {
            logger.warn { "Found invalid reference #/$this, expected to start with #/components/schemas/" }
            return null
        }

        return substring(refNameStartIndex)
    }

    companion object Companion {
        private val logger = KotlinLogging.logger { }
    }
}

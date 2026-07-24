package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.SpektorType.MicroType
import io.github.vooft.spektor.model.SpektorType.MicroType.StringFormat
import io.swagger.v3.oas.models.media.Schema
import java.net.URI
import java.nio.file.Path

class SpektorTypeResolver(private val file: Path, private val allRefs: MutableSet<SpektorType.Ref>) {

    fun resolve(schema: Schema<*>): SpektorType {
        val type = schema.singleType()
        return when {
            schema.`$ref` != null -> resolveRef(schema.`$ref`).also { allRefs.add(it) }
            type == "object" -> resolveObject(schema)
            type == "array" -> resolveArray(schema)
            type == "string" && schema.enum != null -> SpektorType.Enum(schema.enum.map { it.toString() })
            type != null -> MicroType.from(type, schema.format)
            schema.oneOf != null && schema.discriminator != null -> resolveOneOf(schema)
            else -> error("Schema in file $file is of invalid type ${schema.types}: $schema")
        }
    }

    private fun Schema<*>.singleType(): String? {
        val types = this.types ?: return null
        require(types.size == 1) { "Expected exactly one type, but got $types in file $file" }
        return types.single()
    }

    private fun resolveArray(schema: Schema<*>): SpektorType.Array {
        val itemsRef = schema.items ?: error("Array schema has no items: $schema")
        return SpektorType.Array(resolve(itemsRef))
    }

    private fun resolveObject(schema: Schema<*>): SpektorType.Object {
        val required = schema.required?.toSet() ?: emptySet()
        val properties = schema.properties ?: run {
            val additionalProps = schema.additionalProperties
            if (additionalProps is Schema<*>) {
                val keyType = resolvePropertyNamesKeyType(schema)
                return SpektorType.Object.AdditionalProperties(
                    keyType = keyType,
                    valueType = resolve(additionalProps),
                )
            }
            return SpektorType.Object.FreeForm
        }

        val props = properties.mapValues { (propName, propSchema) ->
            SpektorType.RequiredWrapper(resolve(propSchema), required.contains(propName))
        }

        return SpektorType.Object.WithProperties(props)
    }

    private fun resolvePropertyNamesKeyType(schema: Schema<*>): SpektorType = schema.propertyNames?.let {
        val type = resolve(it)
        require(type is SpektorType.Ref || type is MicroType.StringMicroType) {
            "propertyNames must be a \$ref to a string-based type, but got $type in file $file"
        }
        type
    } ?: MicroType.StringMicroType(StringFormat.PLAIN)

    private fun resolveOneOf(schema: Schema<*>): SpektorType.OneOf {
        val discriminator = schema.discriminator

        val propertyName = discriminator.propertyName
            ?: error("oneOf schema in $file has no discriminator propertyName")

        val mapping = discriminator.mapping
            ?: error("oneOf schema in $file has no discriminator mapping")

        val variants = mapping.mapValues { (_, refString) ->
            resolveRef(refString).also { allRefs.add(it) }
        }

        return SpektorType.OneOf(
            discriminatorPropertyName = propertyName,
            variants = variants
        )
    }

    private fun resolveRef(rawRef: String): SpektorType.Ref {
        // check if a URI reference
        val uriScheme = try {
            URI(rawRef).scheme
        } catch (_: Exception) {
            // ignore, not a valid URI
            null
        }
        require(uriScheme == null) { "Found a URI reference in file $file, which is not supported: $rawRef" }

        // then probably it is either a file reference or a local one
        val refPath = rawRef.extractFilePath()
            ?: error("Found invalid reference $rawRef in file $file, expected to start with #/ or a valid file path")

        val refName = rawRef.substringAfter("#/").extractRefName()

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

    private fun String.extractRefName(): String {
        val refNameStartIndex = lastIndexOf('/') + 1
        require(take(refNameStartIndex) == "components/schemas/") {
            "Found invalid reference #/$this, expected to start with #/components/schemas/"
        }

        return substring(refNameStartIndex)
    }
}

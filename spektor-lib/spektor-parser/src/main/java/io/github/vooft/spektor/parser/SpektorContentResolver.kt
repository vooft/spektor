package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorType
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema

internal class SpektorContentResolver(private val typeResolver: SpektorTypeResolver) {

    data class ResolvedContent(val contentType: SpektorContentType, val type: SpektorType)

    fun resolve(content: Content, isRequestBody: Boolean = false): ResolvedContent? {
        val jsonSchema = content.findSchema(SpektorContentType.JSON)
        val textPlainSchema = content.findSchema(SpektorContentType.TEXT_PLAIN)
        val multipartSchema = content.findSchema(SpektorContentType.MULTIPART_FORM_DATA)
        val hasBinaryContent = content.any { (mediaType, media) -> mediaType !in KNOWN_MEDIA_TYPES && media.isBinaryContent() }

        return when {
            content.isEmpty() -> null
            jsonSchema != null -> resolveJsonContent(jsonSchema)
            textPlainSchema != null -> resolveTextPlainContent(textPlainSchema)
            multipartSchema != null || hasBinaryContent -> {
                if (!isRequestBody) {
                    logger.warn {
                        "${SpektorContentType.MULTIPART_FORM_DATA.mediaType} and binary content types " +
                            "are only supported in request bodies, skipping ${content.keys}"
                    }
                    null
                } else if (multipartSchema != null) {
                    resolveMultipartContent(multipartSchema)
                } else {
                    ResolvedContent(SpektorContentType.BINARY, SpektorType.Binary)
                }
            }

            else -> {
                logger.warn {
                    "Only ${SpektorContentType.entries.mapNotNull { it.mediaType }} and binary bodies " +
                        "(any other media type with 'contentMediaType' or an empty schema) " +
                        "are supported, but present ${content.keys}"
                }
                null
            }
        }
    }

    private fun Content.findSchema(contentType: SpektorContentType): Schema<*>? = contentType.mediaType?.let { get(it) }?.schema

    /**
     * A binary body is either explicitly marked as such with `contentMediaType`, or has an empty or missing schema,
     * e.g. a wildcard image media type declared with an empty schema `{ }`.
     */
    private fun MediaType.isBinaryContent(): Boolean {
        val schemaValue = schema ?: return true
        return schemaValue.contentMediaType != null || schemaValue.isEmptySchema()
    }

    private fun Schema<*>.isEmptySchema(): Boolean = types == null && `$ref` == null && oneOf == null

    private fun resolveMultipartContent(schema: Schema<*>): ResolvedContent {
        if (schema.types?.singleOrNull() != OBJECT_TYPE) {
            logger.warn {
                "Expected '$OBJECT_TYPE' schema for ${SpektorContentType.MULTIPART_FORM_DATA.mediaType}, " +
                    "but got ${schema.types}, schema is not validated"
            }
        }
        return ResolvedContent(SpektorContentType.MULTIPART_FORM_DATA, SpektorType.Multipart)
    }

    private fun resolveJsonContent(schema: Schema<*>): ResolvedContent? = typeResolver.resolve(schema)?.let {
        ResolvedContent(SpektorContentType.JSON, it)
    }

    private fun resolveTextPlainContent(schema: Schema<*>): ResolvedContent? {
        val resolved = typeResolver.resolve(schema) ?: return null
        if (resolved != SpektorType.MicroType.StringMicroType(SpektorType.MicroType.StringFormat.PLAIN)) {
            logger.warn { "Only plain string schema is supported for ${SpektorContentType.TEXT_PLAIN.mediaType}, but got $resolved" }
            return null
        }
        return ResolvedContent(SpektorContentType.TEXT_PLAIN, resolved)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        private val KNOWN_MEDIA_TYPES = SpektorContentType.entries.mapNotNull { it.mediaType }.toSet()
        private const val OBJECT_TYPE = "object"
    }
}

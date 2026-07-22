package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorType
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.Schema

internal class SpektorContentResolver(private val typeResolver: SpektorTypeResolver) {

    data class ResolvedContent(val contentType: SpektorContentType, val type: SpektorType)

    fun resolve(content: Content, isRequestBody: Boolean = false): ResolvedContent? {
        val jsonSchema = content.findSchema(SpektorContentType.JSON)
        val textPlainSchema = content.findSchema(SpektorContentType.TEXT_PLAIN)
        val multipartSchema = content.findSchema(SpektorContentType.MULTIPART_FORM_DATA)
        val binarySchema = content.values.firstOrNull { it.schema.isBinarySchema() }?.schema

        return when {
            content.isEmpty() -> null
            jsonSchema != null -> resolveJsonContent(jsonSchema)
            textPlainSchema != null -> resolveTextPlainContent(textPlainSchema)
            multipartSchema != null || binarySchema != null -> {
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
                        "(schema with 'format: $BINARY_FORMAT' or 'contentMediaType') are supported, but present ${content.keys}"
                }
                null
            }
        }
    }

    private fun Content.findSchema(contentType: SpektorContentType): Schema<*>? = contentType.mediaType?.let { get(it) }?.schema

    private fun Schema<*>?.isBinarySchema(): Boolean = this != null && (format == BINARY_FORMAT || contentMediaType != null)

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
        private const val BINARY_FORMAT = "binary"
        private const val OBJECT_TYPE = "object"
    }
}

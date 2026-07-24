package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.SpektorType.MicroType.StringFormat
import io.github.vooft.spektor.model.SpektorType.MicroType.StringMicroType
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema

internal class SpektorContentResolver(private val typeResolver: SpektorTypeResolver) {

    data class ResolvedContent(val contentType: SpektorContentType, val type: SpektorType)

    fun resolve(content: Content, isRequestBody: Boolean = false): ResolvedContent? {
        val jsonSchema = content.findSchema(SpektorContentType.JSON)
        val textPlainSchema = content.findSchema(SpektorContentType.TEXT_PLAIN)
        val multipartMedia = content.findMedia(SpektorContentType.MULTIPART_FORM_DATA)
        val hasBinaryContent = content.any { (mediaType, media) -> mediaType !in KNOWN_MEDIA_TYPES && media.isBinaryContent() }

        return when {
            content.isEmpty() -> null
            jsonSchema != null -> resolveJsonContent(jsonSchema)
            textPlainSchema != null -> resolveTextPlainContent(textPlainSchema)
            multipartMedia != null -> {
                require(isRequestBody) {
                    "${SpektorContentType.MULTIPART_FORM_DATA.mediaType} is only supported in request bodies, " +
                        "but present in ${content.keys}"
                }
                resolveMultipartContent(multipartMedia.schema)
            }

            hasBinaryContent -> {
                require(isRequestBody) { "Binary content is only supported in request bodies, but present in ${content.keys}" }
                ResolvedContent(SpektorContentType.BINARY, SpektorType.Binary)
            }

            else -> error(
                "Only ${SpektorContentType.entries.mapNotNull { it.mediaType }} and binary bodies " +
                    "(any other media type with 'contentMediaType' or an empty schema) " +
                    "are supported, but present ${content.keys}"
            )
        }
    }

    private fun Content.findMedia(contentType: SpektorContentType): MediaType? = contentType.mediaType?.let { get(it) }

    private fun Content.findSchema(contentType: SpektorContentType): Schema<*>? = findMedia(contentType)?.schema

    /**
     * A binary body is either explicitly marked as such with `contentMediaType`, or has an empty or missing schema,
     * e.g. a wildcard image media type declared with an empty schema `{ }`.
     */
    private fun MediaType.isBinaryContent(): Boolean {
        val schemaValue = schema ?: return true
        return schemaValue.contentMediaType != null || schemaValue.isEmptySchema()
    }

    private fun Schema<*>.isEmptySchema(): Boolean = types == null && `$ref` == null && oneOf == null

    /**
     * The multipart schema is only documentation, so a missing, empty or `$ref` schema is accepted silently,
     * only an explicit non-object type is suspicious enough to warn about.
     */
    private fun resolveMultipartContent(schema: Schema<*>?): ResolvedContent {
        val types = schema?.types
        if (types != null && types.singleOrNull() != OBJECT_TYPE) {
            logger.warn {
                "Expected '$OBJECT_TYPE' schema for ${SpektorContentType.MULTIPART_FORM_DATA.mediaType}, " +
                    "but got $types, schema is not validated"
            }
        }
        return ResolvedContent(SpektorContentType.MULTIPART_FORM_DATA, SpektorType.Multipart)
    }

    private fun resolveJsonContent(schema: Schema<*>): ResolvedContent {
        val resolved = typeResolver.resolve(schema)
        return ResolvedContent(contentType = SpektorContentType.JSON, type = resolved)
    }

    private fun resolveTextPlainContent(schema: Schema<*>): ResolvedContent {
        val resolved = typeResolver.resolve(schema)
        require(resolved == PLAIN_TEXT_TYPE) {
            "Only plain string schema is supported for ${SpektorContentType.TEXT_PLAIN.mediaType}, but got $resolved"
        }
        return ResolvedContent(SpektorContentType.TEXT_PLAIN, resolved)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        private val KNOWN_MEDIA_TYPES = SpektorContentType.entries.mapNotNull { it.mediaType }.toSet()
        private const val OBJECT_TYPE = "object"
        private val PLAIN_TEXT_TYPE = StringMicroType(StringFormat.PLAIN)
    }
}

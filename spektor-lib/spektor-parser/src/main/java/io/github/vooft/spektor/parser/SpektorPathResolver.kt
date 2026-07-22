package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorPath.PathVariable
import io.github.vooft.spektor.model.SpektorPath.QueryVariable
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponses
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

class SpektorPathResolver(private val typeResolver: SpektorTypeResolver) {

    private val operationIdCounter = AtomicInteger()

    fun resolve(file: Path, path: String, method: SpektorPath.Method, operation: Operation): SpektorPath {
        val requestContent = operation.requestBody?.content?.findContent(isRequestBody = true)
        return SpektorPath(
            tagAndFile = TagAndFile(operation.resolveTag(), file),
            operationId = operation.operationId ?: "$OPERATION_PLACEHOLDER${operationIdCounter.getAndIncrement()}",
            path = path,
            requestBody = requestContent?.let {
                SpektorType.RequiredWrapper(it.type, it.resolveRequired(operation))
            },
            requestBodyContentType = requestContent?.contentType ?: SpektorContentType.JSON,
            responses = operation.responses?.responses() ?: emptyList(),
            pathVariables = operation.parameters?.extractPathParameters(ParameterLocation.PATH) ?: listOf(),
            queryVariables = operation.parameters?.extractQueryParameters(ParameterLocation.QUERY) ?: listOf(),
            method = method
        )
    }

    private fun Operation.resolveTag(): String {
        val tagsVal = this.tags ?: return TAG_PLACEHOLDER
        if (tagsVal.size > 1) {
            logger.warn { "Multiple tags found: $tagsVal, using the first one" }
        }

        return tagsVal.firstOrNull() ?: run {
            logger.warn { "No tags found, using placeholder" }
            TAG_PLACEHOLDER
        }
    }

    private fun Collection<Parameter>.extractPathParameters(location: ParameterLocation): List<PathVariable> {
        return filter { it.`in` == location.value }
            .mapNotNull { parameter ->
                val type = parameter.findParameterSpektorType() ?: run {
                    logger.warn { "Path parameter ${parameter.name} has no valid type, skipping" }
                    return@mapNotNull null
                }

                return@mapNotNull when (type) {
                    is SpektorType.MicroType,
                    is SpektorType.Ref -> PathVariable(
                        name = parameter.name,
                        type = type,
                        required = parameter.required ?: false
                    )

                    is SpektorType.Enum,
                    is SpektorType.OneOf,
                    is SpektorType.Array,
                    is SpektorType.Multipart,
                    is SpektorType.Binary,
                    is SpektorType.Object -> {
                        logger.warn { "Path parameter ${parameter.name} has unsupported $type, skipping" }
                        null
                    }
                }
            }
    }

    private fun Collection<Parameter>.extractQueryParameters(location: ParameterLocation): List<QueryVariable> {
        return filter { it.`in` == location.value }
            .mapNotNull { parameter ->
                val type = parameter.findParameterSpektorType() ?: run {
                    logger.warn { "Query parameter ${parameter.name} has no valid type, skipping" }
                    return@mapNotNull null
                }

                return@mapNotNull when (type) {
                    is SpektorType.MicroType,
                    is SpektorType.Array,
                    is SpektorType.Ref -> QueryVariable(
                        name = parameter.name,
                        type = type,
                        required = parameter.required ?: false
                    )

                    is SpektorType.Enum,
                    is SpektorType.OneOf,
                    is SpektorType.Multipart,
                    is SpektorType.Binary,
                    is SpektorType.Object -> {
                        logger.warn { "Query parameter ${parameter.name} has unsupported $type, skipping" }
                        null
                    }
                }
            }
    }

    private fun Parameter.findParameterSpektorType(): SpektorType? {
        val schemaVal = schema
        val contentVal = content

        return when {
            schemaVal != null -> typeResolver.resolve(schemaVal)
            contentVal != null -> contentVal.findContent()?.type
            else -> {
                logger.warn { "Parameter has neither schema nor content: $this" }
                null
            }
        }
    }

    private fun ApiResponses.responses(): List<SpektorPath.Response> = map { (code, response) ->
        val content = response.content?.findContent()
        SpektorPath.Response(
            statusCode = code.toInt(),
            body = content?.type?.let { SpektorType.RequiredWrapper(it, true) },
            contentType = content?.contentType ?: SpektorContentType.JSON,
        )
    }

    private data class ResolvedContent(val contentType: SpektorContentType, val type: SpektorType)

    private fun ResolvedContent.resolveRequired(operation: Operation): Boolean {
        val required = operation.requestBody?.required ?: false
        if (contentType == SpektorContentType.MULTIPART_FORM_DATA && !required) {
            logger.warn {
                "Optional ${SpektorContentType.MULTIPART_FORM_DATA.mediaType} request body is not supported " +
                    "in operation ${operation.operationId}, treating as required"
            }
            return true
        }
        return required
    }

    private fun Content.findContent(isRequestBody: Boolean = false): ResolvedContent? {
        val jsonSchema = findSchema(SpektorContentType.JSON)
        val textPlainSchema = findSchema(SpektorContentType.TEXT_PLAIN)
        val multipartSchema = findSchema(SpektorContentType.MULTIPART_FORM_DATA)
        val binarySchema = values.firstOrNull { it.schema.isBinarySchema() }?.schema

        return when {
            isEmpty() -> null
            jsonSchema != null -> resolveJsonContent(jsonSchema)
            textPlainSchema != null -> resolveTextPlainContent(textPlainSchema)
            multipartSchema != null || binarySchema != null -> {
                if (!isRequestBody) {
                    logger.warn {
                        "${SpektorContentType.MULTIPART_FORM_DATA.mediaType} and binary content types " +
                            "are only supported in request bodies, skipping $keys"
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
                        "(schema with 'format: $BINARY_FORMAT' or 'contentMediaType') are supported, but present $keys"
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

    enum class ParameterLocation(val value: String) {
        PATH("path"),
        QUERY("query"),
        HEADER("header"),
        COOKIE("cookie")
    }

    companion object Companion {
        private val logger = KotlinLogging.logger { }
        private const val TAG_PLACEHOLDER = "SpektorDefault"
        private const val OPERATION_PLACEHOLDER = "DoOperation"
        private const val BINARY_FORMAT = "binary"
        private const val OBJECT_TYPE = "object"
    }
}

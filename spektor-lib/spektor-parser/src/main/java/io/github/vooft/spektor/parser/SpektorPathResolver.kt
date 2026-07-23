package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorPath.PathVariable
import io.github.vooft.spektor.model.SpektorPath.QueryVariable
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponses
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

class SpektorPathResolver(private val typeResolver: SpektorTypeResolver) {

    private val contentResolver = SpektorContentResolver(typeResolver)
    private val operationIdCounter = AtomicInteger()

    fun resolve(file: Path, path: String, method: SpektorPath.Method, operation: Operation): SpektorPath {
        val requestContent = operation.requestBody?.content?.let { contentResolver.resolve(it, isRequestBody = true) }
        return SpektorPath(
            tagAndFile = TagAndFile(operation.resolveTag(), file),
            operationId = operation.operationId ?: "$OPERATION_PLACEHOLDER${operationIdCounter.getAndIncrement()}",
            path = path,
            requestBody = requestContent?.toRequestBody(operation),
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
            contentVal != null -> contentResolver.resolve(contentVal)?.type
            else -> {
                logger.warn { "Parameter has neither schema nor content: $this" }
                null
            }
        }
    }

    private fun ApiResponses.responses(): List<SpektorPath.Response> = map { (code, response) ->
        val content = response.content?.let { contentResolver.resolve(it) }
        SpektorPath.Response(
            statusCode = code.toInt(),
            body = content?.type?.let { SpektorType.RequiredWrapper(it, true) },
            contentType = content?.contentType ?: SpektorContentType.JSON,
        )
    }

    private fun SpektorContentResolver.ResolvedContent.toRequestBody(operation: Operation): SpektorType.RequiredWrapper<SpektorType> =
        SpektorType.RequiredWrapper(type, resolveRequired(operation))

    private fun SpektorContentResolver.ResolvedContent.resolveRequired(operation: Operation): Boolean = when {
        operation.requestBody?.required == true -> true
        contentType != SpektorContentType.MULTIPART_FORM_DATA -> false
        else -> {
            logger.warn {
                "Optional ${contentType.mediaType} request body is not supported " +
                    "in operation ${operation.operationId}, treating as required"
            }
            true
        }
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
    }
}

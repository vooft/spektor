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

    fun resolve(file: Path, path: String, method: SpektorPath.Method, operation: Operation): SpektorPath = SpektorPath(
        tagAndFile = TagAndFile(operation.resolveTag(), file),
        operationId = operation.operationId ?: "$OPERATION_PLACEHOLDER${operationIdCounter.getAndIncrement()}",
        path = path,
        requestBody = operation.resolveRequestBody(),
        responses = operation.responses?.responses() ?: emptyList(),
        pathVariables = operation.parameters?.extractPathParameters(ParameterLocation.PATH) ?: listOf(),
        queryVariables = operation.parameters?.extractQueryParameters(ParameterLocation.QUERY) ?: listOf(),
        method = method
    )

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
            .map { parameter ->
                val type = parameter.findParameterSpektorType()
                    ?: error("Path parameter ${parameter.name} has no valid type")

                when (type) {
                    is SpektorType.MicroType,
                    is SpektorType.Ref,
                        -> PathVariable(
                        name = parameter.name,
                        type = type,
                        required = parameter.required ?: false
                    )

                    is SpektorType.Enum,
                    is SpektorType.OneOf,
                    is SpektorType.Array,
                    is SpektorType.Multipart,
                    is SpektorType.Binary,
                    is SpektorType.Object,
                        -> error("Path parameter ${parameter.name} has unsupported $type")
                }
            }
    }

    private fun Collection<Parameter>.extractQueryParameters(location: ParameterLocation): List<QueryVariable> {
        return filter { it.`in` == location.value }
            .map { parameter ->
                val type = parameter.findParameterSpektorType()
                    ?: error("Query parameter ${parameter.name} has no valid type")

                when (type) {
                    is SpektorType.MicroType,
                    is SpektorType.Array,
                    is SpektorType.Ref,
                        -> QueryVariable(
                        name = parameter.name,
                        type = type,
                        required = parameter.required ?: false
                    )

                    is SpektorType.Enum,
                    is SpektorType.OneOf,
                    is SpektorType.Multipart,
                    is SpektorType.Binary,
                    is SpektorType.Object,
                        -> error("Query parameter ${parameter.name} has unsupported $type")
                }
            }
    }

    private fun Parameter.findParameterSpektorType(): SpektorType? {
        val schemaVal = schema
        val contentVal = content

        return when {
            schemaVal != null -> typeResolver.resolve(schemaVal)
            contentVal != null -> contentResolver.resolve(contentVal)?.type
            else -> error("Parameter has neither schema nor content: $this")
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

    private fun Operation.resolveRequestBody(): SpektorPath.RequestBody? {
        val content = requestBody?.content ?: return null
        val resolved = contentResolver.resolve(
            content = content,
            isRequestBody = true,
        ) ?: return null
        return SpektorPath.RequestBody(
            type = resolved.type,
            required = resolved.resolveRequired(this),
            contentType = resolved.contentType,
        )
    }

    private fun SpektorContentResolver.ResolvedContent.resolveRequired(operation: Operation): Boolean = when {
        operation.requestBody?.required == true -> true
        contentType != SpektorContentType.MULTIPART_FORM_DATA -> false
        else -> {
            logger.warn {
                "Optional ${contentType.mediaType} request body is not supported in operation ${operation.operationId}, treating as required"
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

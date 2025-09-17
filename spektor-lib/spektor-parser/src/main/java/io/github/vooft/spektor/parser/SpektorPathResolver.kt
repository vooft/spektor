package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorPath.*
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponses
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

class SpektorPathResolver(private val typeResolver: SpektorTypeResolver) {

    private val operationIdCounter = AtomicInteger()

    fun resolve(file: Path, path: String, method: SpektorPath.Method, operation: Operation): SpektorPath = SpektorPath(
        tagAndFile = TagAndFile(operation.resolveTag(), file),
        operationId = operation.operationId ?: "operationPlaceholder-${operationIdCounter.getAndIncrement()}",
        path = path,
        requestBody = operation.requestBody?.let { rq ->
            rq.content?.findSpektorType()?.let { SpektorType.RequiredWrapper(it, rq.required) }
        },
        responseBody = operation.responses?.find2xxSpektorType()?.let {
            SpektorType.RequiredWrapper(it, true)
        }, // TODO: can response body be optional?
        pathVariables = operation.parameters?.extractPathParameters(ParameterLocation.PATH) ?: listOf(),
        queryPathVariables = operation.parameters?.extractQueryParameters(ParameterLocation.QUERY) ?: listOf(),
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
            .mapNotNull { parameter ->
                val type = parameter.findSpektorType() ?: run {
                    logger.warn { "Path parameter ${parameter.name} has no valid type, skipping" }
                    return@mapNotNull null
                }

                return@mapNotNull when (type) {
                    is SpektorType.MicroType -> PathVariable(
                        name = parameter.name,
                        type = type,
                        required = parameter.required ?: false
                    )

                    is SpektorType.Enum,
                    is SpektorType.Array,
                    is SpektorType.Object,
                    is SpektorType.Ref -> {
                        logger.warn { "Path parameter ${parameter.name} has unsupported $type, skipping" }
                        null
                    }
                }
            }
    }

    private fun Collection<Parameter>.extractQueryParameters(location: ParameterLocation): List<QueryVariable> {
        return filter { it.`in` == location.value }
            .mapNotNull { parameter ->
                val type = parameter.findSpektorType() ?: run {
                    logger.warn { "Query parameter ${parameter.name} has no valid type, skipping" }
                    return@mapNotNull null
                }

                return@mapNotNull when (type) {
                    is SpektorType.MicroType,
                    is SpektorType.Array -> QueryVariable(
                        name = parameter.name,
                        type = type,
                        required = parameter.required ?: false
                    )

                    is SpektorType.Enum,
                    is SpektorType.Object,
                    is SpektorType.Ref -> {
                        logger.warn { "Query parameter ${parameter.name} has unsupported $type, skipping" }
                        null
                    }
                }
            }
    }

    private fun Parameter.findSpektorType(): SpektorType? {
        val schemaVal = schema
        val contentVal = content

        return when {
            schemaVal != null -> typeResolver.resolve(schemaVal)
            contentVal != null -> contentVal.findSpektorType()
            else -> {
                logger.warn { "Parameter has neither schema nor content: $this" }
                null
            }
        }
    }

    private fun ApiResponses.find2xxSpektorType(): SpektorType? {
        if (isEmpty()) return null

        val responses2xx = entries.filter { it.key.length == 3 && it.key.startsWith("2") }.map { it.value }
        if (responses2xx.isEmpty()) {
            logger.warn { "No 2xx responses found in API responses: $this" }
            return null
        }

        return responses2xx.firstNotNullOfOrNull { it.content?.findSpektorType() } ?: run {
            logger.warn { "No valid response types found in 2xx responses: $responses2xx" }
            null
        }
    }

    private fun Content.findSpektorType(): SpektorType? {
        if (isEmpty()) return null

        val schema = get(APPLICATION_JSON)?.schema ?: run {
            logger.warn { "Only $APPLICATION_JSON is supported, but present $keys" }
            return null
        }

        return typeResolver.resolve(schema)
    }

    enum class ParameterLocation(val value: String) {
        PATH("path"),
        QUERY("query"),
        HEADER("header"),
        COOKIE("cookie")
    }

    companion object Companion {
        private val logger = KotlinLogging.logger { }
        private const val APPLICATION_JSON = "application/json"
        private const val TAG_PLACEHOLDER = "SpektorDefault"
    }
}

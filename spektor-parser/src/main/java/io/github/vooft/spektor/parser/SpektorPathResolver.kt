package io.github.vooft.spektor.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponses
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

class SpektorPathResolver(private val typeResolver: SpektorTypeResolver) {

    private val operationIdCounter = AtomicInteger()

    fun resolve(file: Path, path: String, method: SpektorPath.Method, operation: Operation): SpektorPath = SpektorPath(
        file = file,
        operationId = operation.operationId ?: "operationPlaceholder-${operationIdCounter.getAndIncrement()}",
        path = path,
        requestBody = operation.requestBody?.let { rq ->
            rq.content?.findSpektorType()?.let { SpektorType.RequiredWrapper(it, rq.required) }
        },
        responseBody = operation.responses?.find2xxSpektorType()?.let {
            SpektorType.RequiredWrapper(it, true)
        }, // TODO: can response body be optional?
        pathVariables = operation.parameters?.extractParameters(ParameterLocation.PATH) ?: listOf(),
        queryVariables = operation.parameters?.extractParameters(ParameterLocation.QUERY) ?: listOf(),
        method = method
    )

    private fun Collection<Parameter>.extractParameters(location: ParameterLocation): List<SpektorPath.Variable> {
        return filter { it.`in` == location.value }
            .mapNotNull { parameter ->
                val type = parameter.findSpektorType() ?: run {
                    logger.warn { "Parameter ${parameter.name} has no valid type, skipping" }
                    return@mapNotNull null
                }

                SpektorPath.Variable(
                    name = parameter.name,
                    type = type,
                )
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
    }
}

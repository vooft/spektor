package io.github.vooft.spektor.parser

import java.nio.file.Path

data class SpektorPath(
    val file: Path,
    val operationId: String,
    val path: String,
    val requestBody: SpektorType.RequiredWrapper<SpektorType>?,
    val responseBody: SpektorType.RequiredWrapper<SpektorType>?,
    val pathVariables: List<Variable>,
    val queryVariables: List<Variable>,
    val method: Method,
) {
    data class Variable(
        val name: String,
        val type: SpektorType
    )

    enum class Method {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        OPTIONS,
        HEAD
    }
}

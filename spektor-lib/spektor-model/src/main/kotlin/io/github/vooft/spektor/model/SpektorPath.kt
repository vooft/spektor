package io.github.vooft.spektor.model

data class SpektorPath(
    val tagAndFile: TagAndFile,
    val operationId: String,
    val path: String,
    val requestBody: SpektorType.RequiredWrapper<SpektorType>?,
    val responseBody: SpektorType.RequiredWrapper<SpektorType>?,
    val pathVariables: List<PathVariable>,
    val queryVariables: List<QueryVariable>,
    val method: Method,
) {
    init {
        val pathNames = pathVariables.map { it.name }.groupBy { it }
        val pathNameDuplicates = pathNames.filter { it.value.size > 1 }.keys
        require(pathNameDuplicates.isEmpty()) {
            "Duplicate path variable names in $path: ${pathNameDuplicates.joinToString()}"
        }

        val queryNames = queryVariables.map { it.name }.groupBy { it }
        val queryNameDuplicates = queryNames.filter { it.value.size > 1 }.keys
        require(queryNameDuplicates.isEmpty()) {
            "Duplicate query variable names in $path: ${queryNameDuplicates.joinToString()}"
        }

        val intersection = pathNames.keys.intersect(queryNames.keys)
        require(intersection.isEmpty()) {
            "Variables cannot be both in path and query in $path: ${intersection.joinToString()}"
        }
    }

    data class PathVariable(
        val name: String,
        val type: SpektorType.PathVariableType,
        val required: Boolean
    )

    data class QueryVariable(
        val name: String,
        val type: SpektorType.QueryVariableType,
        val required: Boolean
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

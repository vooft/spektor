package io.github.vooft.spektor.codegen.codegen

object ResponseClassNameGenerator {

    fun generate(operationId: String): String {
        require(operationId.isNotBlank()) { "operationId must not be blank" }
        val normalized = operationId
            .split(regex)
            .filter { it.isNotEmpty() }
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
        return "${normalized}Response"
    }

    private val regex = Regex("[^A-Za-z0-9]+")
}

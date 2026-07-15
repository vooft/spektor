package io.github.vooft.spektor.merger

import java.nio.file.Path

class InvalidOpenApiSpecException(val file: Path, val messages: List<String>) :
    RuntimeException("Failed to parse OpenAPI spec in $file: ${messages.joinToString("; ")}")

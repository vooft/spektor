package io.github.vooft.spektor.merger

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal object OpenApiValidatingParser {

    private val isErrorIgnored: List<(String) -> Boolean> = listOf(
        // this is not critical, since we already verify that operation id can not repeat within the same tag
        { it.contains("operationId is repeated") }
    )

    fun parseAndValidate(content: String, location: Path): OpenAPI {
        val parseResult = OpenAPIV3Parser().readContents(
            content,
            listOf(),
            ParseOptions().apply { isResolve = true },
            location.absolutePathString()
        )

        parseResult.messages
            ?.filterNot { message -> isErrorIgnored.any { it(message) } }
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                throw InvalidOpenApiSpecException(location, it)
            }

        return parseResult.openAPI
    }
}

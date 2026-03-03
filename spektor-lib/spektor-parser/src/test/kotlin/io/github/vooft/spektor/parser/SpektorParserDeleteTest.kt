package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType.MicroType.StringFormat
import io.github.vooft.spektor.model.SpektorType.MicroType.StringMicroType
import io.github.vooft.spektor.model.TagAndFile
import io.github.vooft.spektor.test.TestFiles.deleteBookFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorParserDeleteTest {

    private val parser = SpektorParser()

    @Test
    fun `should parse 204 response with no body`() {
        val schema = parser.parse(listOf(deleteBookFile))

        val path = schema.paths.values.single().single()
        path.responses shouldBe listOf(
            SpektorPath.Response(
                statusCode = 204,
                body = null,
            )
        )
    }

    @Test
    fun `should parse delete operation with path variable`() {
        val schema = parser.parse(listOf(deleteBookFile))

        val path = schema.paths.values.single().single()
        path shouldBe SpektorPath(
            tagAndFile = TagAndFile("Book", deleteBookFile.toAbsolutePath().normalize()),
            operationId = "Delete",
            path = "/book/{id}",
            requestBody = null,
            responses = listOf(SpektorPath.Response(statusCode = 204, body = null)),
            pathVariables = listOf(
                SpektorPath.PathVariable(
                    name = "id",
                    type = StringMicroType(format = StringFormat.UUID),
                    required = true,
                )
            ),
            queryVariables = emptyList(),
            method = SpektorPath.Method.DELETE,
        )
    }
}

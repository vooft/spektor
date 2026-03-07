package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile
import io.github.vooft.spektor.test.TestFiles.bookModelFile
import io.github.vooft.spektor.test.TestFiles.optionalRequestBodyFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorParserOptionalRequestBodyTest {

    private val parser = SpektorParser()

    @Test
    fun `should parse optional request body with required=false`() {
        val schema = parser.parse(listOf(optionalRequestBodyFile))

        val path = schema.paths.values.single().single()
        path shouldBe SpektorPath(
            tagAndFile = TagAndFile("OptionalRequestBodyBook", optionalRequestBodyFile.toAbsolutePath().normalize()),
            operationId = "update",
            path = "/book/update",
            requestBody = SpektorType.RequiredWrapper(
                type = SpektorType.Ref(
                    file = bookModelFile.toAbsolutePath().normalize(),
                    modelName = "BookRequest"
                ),
                required = false
            ),
            responses = listOf(
                SpektorPath.Response(
                    statusCode = 200,
                    body = SpektorType.RequiredWrapper(
                        type = SpektorType.Ref(
                            file = bookModelFile.toAbsolutePath().normalize(),
                            modelName = "Book"
                        ),
                        required = true
                    )
                )
            ),
            pathVariables = emptyList(),
            queryVariables = emptyList(),
            method = SpektorPath.Method.PATCH,
        )
    }
}

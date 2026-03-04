package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile
import io.github.vooft.spektor.test.TestFiles.authorModelFile
import io.github.vooft.spektor.test.TestFiles.pathParamRefFile
import io.github.vooft.spektor.test.TestFiles.queryParamRefFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorParserParamRefTest {

    private val parser = SpektorParser()

    @Test
    fun `should parse path parameter typed via ref`() {
        val schema = parser.parse(listOf(pathParamRefFile))

        val path = schema.paths.values.single().single()
        path shouldBe SpektorPath(
            tagAndFile = TagAndFile("Author", pathParamRefFile.toAbsolutePath().normalize()),
            operationId = "listByCountry",
            path = "/author/byCountry/{country}",
            requestBody = null,
            responses = listOf(
                SpektorPath.Response(
                    statusCode = 200,
                    body = SpektorType.RequiredWrapper(
                        type = SpektorType.Ref(
                            file = authorModelFile.toAbsolutePath().normalize(),
                            modelName = "AuthorsList"
                        ),
                        required = true,
                    )
                )
            ),
            pathVariables = listOf(
                SpektorPath.PathVariable(
                    name = "country",
                    type = SpektorType.Ref(
                        file = authorModelFile.toAbsolutePath().normalize(),
                        modelName = "AuthorCountry"
                    ),
                    required = true,
                )
            ),
            queryVariables = emptyList(),
            method = SpektorPath.Method.GET,
        )
    }

    @Test
    fun `should parse query parameter typed via ref`() {
        val schema = parser.parse(listOf(queryParamRefFile))

        val path = schema.paths.values.single().single()
        path shouldBe SpektorPath(
            tagAndFile = TagAndFile("Author", queryParamRefFile.toAbsolutePath().normalize()),
            operationId = "listByCountry",
            path = "/author/byCountry",
            requestBody = null,
            responses = listOf(
                SpektorPath.Response(
                    statusCode = 200,
                    body = SpektorType.RequiredWrapper(
                        type = SpektorType.Ref(
                            file = authorModelFile.toAbsolutePath().normalize(),
                            modelName = "AuthorsList"
                        ),
                        required = true,
                    )
                )
            ),
            pathVariables = emptyList(),
            queryVariables = listOf(
                SpektorPath.QueryVariable(
                    name = "country",
                    type = SpektorType.Ref(
                        file = authorModelFile.toAbsolutePath().normalize(),
                        modelName = "AuthorCountry"
                    ),
                    required = false,
                )
            ),
            method = SpektorPath.Method.GET,
        )
    }
}

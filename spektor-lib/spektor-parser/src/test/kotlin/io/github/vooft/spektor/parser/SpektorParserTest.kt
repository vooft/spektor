package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorSchema
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.SpektorType.MicroType.NumberFormat
import io.github.vooft.spektor.model.SpektorType.MicroType.NumberMicroType
import io.github.vooft.spektor.model.SpektorType.MicroType.StringFormat
import io.github.vooft.spektor.model.SpektorType.MicroType.StringMicroType
import io.github.vooft.spektor.model.TagAndFile
import io.github.vooft.spektor.test.TestFiles.authorModelFile
import io.github.vooft.spektor.test.TestFiles.bookModelFile
import io.github.vooft.spektor.test.TestFiles.listFile
import io.github.vooft.spektor.test.TestFiles.moneyModelFile
import io.github.vooft.spektor.test.TestFiles.pathVarFile
import io.github.vooft.spektor.test.TestFiles.requestBodyFile
import io.kotest.assertions.withClue
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorParserTest {
    @Test
    fun test() {
        val parser = SpektorParser()

        val schema = parser.parse(listOf(listFile, pathVarFile, requestBodyFile))
        withClue("Paths do not match") {
            schema.paths.shouldContainExactly(expected.paths)
        }
        withClue("Refs do not match") {
            schema.refs shouldContainExactly expected.refs
        }

        schema shouldBe expected
    }
}

private val expected = SpektorSchema(
    paths = mapOf(
        TagAndFile("BookList", listFile.toAbsolutePath().normalize()) to listOf(
            SpektorPath(
                tagAndFile = TagAndFile("BookList", listFile.toAbsolutePath().normalize()),
                operationId = "list",
                path = "/book",
                requestBody = null,
                responseBody = SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = bookModelFile.toAbsolutePath().normalize(),
                        modelName = "BooksList"
                    ),
                    required = true
                ),
                pathVariables = emptyList(),
                queryVariables = emptyList(),
                method = SpektorPath.Method.GET
            )
        ),
        TagAndFile("Author", pathVarFile.toAbsolutePath().normalize()) to listOf(
            SpektorPath(
                tagAndFile = TagAndFile("Author", pathVarFile.toAbsolutePath().normalize()),
                operationId = "get",
                path = "/author/{id}",
                requestBody = null,
                responseBody = SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = authorModelFile.toAbsolutePath().normalize(),
                        modelName = "Author"
                    ),
                    required = true
                ),
                pathVariables = listOf(
                    SpektorPath.PathVariable(
                        name = "id",
                        type = StringMicroType(format = StringFormat.UUID),
                        required = true
                    )
                ),
                queryVariables = emptyList(),
                method = SpektorPath.Method.GET
            )
        ),
        TagAndFile("Book", requestBodyFile.toAbsolutePath().normalize()) to listOf(
            SpektorPath(
                tagAndFile = TagAndFile("Book", requestBodyFile.toAbsolutePath().normalize()),
                operationId = "create",
                path = "/book",
                requestBody = SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = bookModelFile.toAbsolutePath().normalize(),
                        modelName = "BookRequest"
                    ),
                    required = true
                ),
                responseBody = SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = bookModelFile.toAbsolutePath().normalize(),
                        modelName = "Book"
                    ),
                    required = true
                ),
                pathVariables = emptyList(),
                queryVariables = emptyList(),
                method = SpektorPath.Method.POST
            )
        )
    ),
    refs = mapOf(
        SpektorType.Ref(
            file = bookModelFile.toAbsolutePath().normalize(),
            modelName = "BooksList"
        ) to SpektorType.Object.WithProperties(
            properties = mapOf(
                "books" to SpektorType.RequiredWrapper(
                    type = SpektorType.Array(
                        itemType = SpektorType.Ref(
                            file = bookModelFile.toAbsolutePath().normalize(),
                            modelName = "Book"
                        )
                    ),
                    required = true
                )
            )
        ),
        SpektorType.Ref(
            file = authorModelFile.toAbsolutePath().normalize(),
            modelName = "Author"
        ) to SpektorType.Object.WithProperties(
            properties = mapOf(
                "id" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.UUID),
                    required = true
                ),
                "name" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.PLAIN),
                    required = true
                ),
                "country" to SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = authorModelFile.toAbsolutePath().normalize(),
                        modelName = "AuthorCountry"
                    ),
                    required = false
                ),
                "dateOfBirth" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.DATE),
                    required = true
                ),
                "additionalDetails" to SpektorType.RequiredWrapper(
                    type = SpektorType.Object.FreeForm,
                    required = false
                ),
                "dateOfDeath" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.DATE),
                    required = false
                ),
                "createdAt" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.DATE_TIME),
                    required = true
                )
            )
        ),
        SpektorType.Ref(
            file = bookModelFile.toAbsolutePath().normalize(),
            modelName = "BookRequest"
        ) to SpektorType.Object.WithProperties(
            properties = mapOf(
                "title" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.PLAIN),
                    required = true
                ),
                "nativeTitle" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.PLAIN),
                    required = false
                ),
                "authorId" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.UUID),
                    required = true
                ),
                "price" to SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = moneyModelFile.toAbsolutePath().normalize(),
                        modelName = "Money"
                    ),
                    required = false
                )
            )
        ),
        SpektorType.Ref(
            file = bookModelFile.toAbsolutePath().normalize(),
            modelName = "Book"
        ) to SpektorType.Object.WithProperties(
            properties = mapOf(
                "id" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.UUID),
                    required = true
                ),
                "title" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.PLAIN),
                    required = true
                ),
                "nativeTitle" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.PLAIN),
                    required = false
                ),
                "imageLink" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.URI),
                    required = true
                ),
                "yearMonth" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.YEAR_MONTH),
                    required = true
                ),
                "referenceLinks" to SpektorType.RequiredWrapper(
                    type = SpektorType.Array(itemType = StringMicroType(format = StringFormat.URI)),
                    required = true
                ),
                "author" to SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = authorModelFile.toAbsolutePath().normalize(),
                        modelName = "Author"
                    ),
                    required = true
                ),
                "price" to SpektorType.RequiredWrapper(
                    type = SpektorType.Ref(
                        file = moneyModelFile.toAbsolutePath().normalize(),
                        modelName = "Money"
                    ),
                    required = false
                ),
                "weight" to SpektorType.RequiredWrapper(
                    type = NumberMicroType(format = NumberFormat.BIG_DECIMAL),
                    required = false
                )
            ),
        ),
        SpektorType.Ref(
            file = authorModelFile.toAbsolutePath().normalize(),
            modelName = "AuthorCountry"
        ) to SpektorType.Enum(values = listOf("US", "DE", "JP")),
        SpektorType.Ref(
            file = moneyModelFile.toAbsolutePath().normalize(),
            modelName = "Money"
        ) to SpektorType.Object.WithProperties(
            properties = mapOf(
                "minorUnits" to SpektorType.RequiredWrapper(
                    type = SpektorType.MicroType.IntegerMicroType(null),
                    required = true
                ),
                "currency" to SpektorType.RequiredWrapper(
                    type = StringMicroType(format = StringFormat.PLAIN),
                    required = true
                ),
            ),
        )
    )
)

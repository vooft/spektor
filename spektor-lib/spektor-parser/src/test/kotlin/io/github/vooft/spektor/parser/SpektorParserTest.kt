package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorSchema
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.SpektorType.MicroType.StringFormat
import io.github.vooft.spektor.model.SpektorType.MicroType.StringMicroType
import io.github.vooft.spektor.model.TagAndFile
import io.github.vooft.spektor.test.TestFiles.authorModelFile
import io.github.vooft.spektor.test.TestFiles.bookModelFile
import io.github.vooft.spektor.test.TestFiles.listFile
import io.github.vooft.spektor.test.TestFiles.moneyModelFile
import io.github.vooft.spektor.test.TestFiles.pathVarFile
import io.github.vooft.spektor.test.TestFiles.requestBodyFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorParserTest {
    @Test
    fun test() {
        val parser = SpektorParser()

        val schema = parser.parse(listOf(listFile, pathVarFile, requestBodyFile))

        schema shouldBe SpektorSchema(
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
                            SpektorPath.Variable(
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
                ) to SpektorType.Object(
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
                ) to SpektorType.Object(
                    properties = mapOf(
                        "id" to SpektorType.RequiredWrapper(
                            type = StringMicroType(format = StringFormat.UUID),
                            required = true
                        ),
                        "name" to SpektorType.RequiredWrapper(
                            type = StringMicroType(format = StringFormat.PLAIN),
                            required = true
                        ),
                        "dateOfBirth" to SpektorType.RequiredWrapper(
                            type = StringMicroType(format = StringFormat.DATE),
                            required = true
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
                ) to SpektorType.Object(
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
                ) to SpektorType.Object(
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
                        )
                    ),
                ),
                SpektorType.Ref(
                    file = moneyModelFile.toAbsolutePath().normalize(),
                    modelName = "Money"
                ) to SpektorType.Object(
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
    }
}

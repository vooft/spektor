package io.github.vooft.spektor.parser

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class SpektorParserTest {
    @Test
    fun test() {
        val parser = SpektorParser()

        val listFile = Paths.get("./src/test/resources/api/list-book.yaml")
        val pathVarFile = Paths.get("./src/test/resources/api/path-var-author.yaml")
        val requestBodyFile = Paths.get("./src/test/resources/api/request-body-book.yaml")
        val bookModelFile = Paths.get("./src/test/resources/models/book.yaml")
        val authorModelFile = Paths.get("./src/test/resources/models/author.yaml")

        val schema = parser.parse(listOf(listFile, pathVarFile, requestBodyFile))

        schema shouldBe SpektorSchema(
            paths = listOf(
                SpektorPath(
                    file = listFile.toAbsolutePath().normalize(),
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
                ),
                SpektorPath(
                    file = pathVarFile.toAbsolutePath().normalize(),
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
                            type = SpektorType.MicroType(type = "string", format = "uuid")
                        )
                    ),
                    queryVariables = emptyList(),
                    method = SpektorPath.Method.GET
                ),
                SpektorPath(
                    file = requestBodyFile.toAbsolutePath().normalize(),
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
            ),
            refs = mapOf(
                SpektorType.Ref(
                    file = bookModelFile.toAbsolutePath().normalize(),
                    modelName = "BooksList"
                ) to SpektorType.Object(
                    properties = mapOf(
                        "books" to SpektorType.RequiredWrapper(
                            type = SpektorType.List(
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
                            type = SpektorType.MicroType(type = "string", format = "uuid"),
                            required = true
                        ),
                        "name" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = null),
                            required = true
                        ),
                        "dateOfBirth" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = "date"),
                            required = true
                        ),
                        "dateOfDeath" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = "date"),
                            required = false
                        ),
                        "createdAt" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = "date-time"),
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
                            type = SpektorType.MicroType(type = "string", format = null),
                            required = true
                        ),
                        "nativeTitle" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = null),
                            required = false
                        ),
                        "authorId" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = "uuid"),
                            required = true
                        )
                    )
                ),
                SpektorType.Ref(
                    file = bookModelFile.toAbsolutePath().normalize(),
                    modelName = "Book"
                ) to SpektorType.Object(
                    properties = mapOf(
                        "id" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = "uuid"),
                            required = true
                        ),
                        "title" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = null),
                            required = true
                        ),
                        "nativeTitle" to SpektorType.RequiredWrapper(
                            type = SpektorType.MicroType(type = "string", format = null),
                            required = false
                        ),
                        "author" to SpektorType.RequiredWrapper(
                            type = SpektorType.Ref(
                                file = authorModelFile.toAbsolutePath().normalize(),
                                modelName = "Author"
                            ),
                            required = true
                        )
                    )
                )
            )
        )
    }
}

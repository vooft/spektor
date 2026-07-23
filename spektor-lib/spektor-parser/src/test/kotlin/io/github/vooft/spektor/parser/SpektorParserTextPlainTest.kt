package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.SpektorType.MicroType.StringFormat
import io.github.vooft.spektor.model.SpektorType.MicroType.StringMicroType
import io.github.vooft.spektor.model.TagAndFile
import io.github.vooft.spektor.test.TestFiles.echoApiFile
import io.github.vooft.spektor.test.TestFiles.pingApiFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorParserTextPlainTest {

    private val parser = SpektorParser()

    @Test
    fun `should parse text plain response`() {
        val schema = parser.parse(listOf(pingApiFile))

        val path = schema.paths.values.single().single()
        path shouldBe SpektorPath(
            tagAndFile = TagAndFile("Ping", pingApiFile.toAbsolutePath().normalize()),
            operationId = "ping",
            path = "/ping",
            requestBody = null,
            responses = listOf(
                SpektorPath.Response(
                    statusCode = 200,
                    body = SpektorType.RequiredWrapper(StringMicroType(StringFormat.PLAIN), true),
                    contentType = SpektorContentType.TEXT_PLAIN,
                )
            ),
            pathVariables = emptyList(),
            queryVariables = emptyList(),
            method = SpektorPath.Method.GET,
        )
    }

    @Test
    fun `should parse required text plain request body`() {
        val schema = parser.parse(listOf(echoApiFile))

        val path = schema.paths.values.single().single { it.operationId == "echo" }
        path.requestBody shouldBe SpektorPath.RequestBody(StringMicroType(StringFormat.PLAIN), true, SpektorContentType.TEXT_PLAIN)
    }

    @Test
    fun `should parse optional text plain request body`() {
        val schema = parser.parse(listOf(echoApiFile))

        val path = schema.paths.values.single().single { it.operationId == "echoOptional" }
        path.requestBody shouldBe SpektorPath.RequestBody(StringMicroType(StringFormat.PLAIN), false, SpektorContentType.TEXT_PLAIN)
    }
}

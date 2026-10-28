package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.test.TestFiles.binaryImageResponseApiFile
import io.github.vooft.spektor.test.TestFiles.imageApiFile
import io.github.vooft.spektor.test.TestFiles.octetStreamApiFile
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class SpektorParserBinaryTest {

    private val parser = SpektorParser()

    @Test
    fun `should parse custom media type binary request body`() {
        val schema = parser.parse(listOf(imageApiFile))

        val path = schema.paths.values.single().single { it.operationId == "uploadImage" }
        path.requestBody shouldBe SpektorPath.RequestBody(SpektorType.Binary, true, SpektorContentType.BINARY)
    }

    @Test
    fun `should parse wildcard media type without schema as binary request body`() {
        val schema = parser.parse(listOf(imageApiFile))

        val path = schema.paths.values.single().single { it.operationId == "uploadImageAny" }
        path.requestBody shouldBe SpektorPath.RequestBody(SpektorType.Binary, true, SpektorContentType.BINARY)
    }

    @Test
    fun `should parse optional binary request body`() {
        val schema = parser.parse(listOf(imageApiFile))

        val path = schema.paths.values.single().single { it.operationId == "uploadImageOptional" }
        path.requestBody shouldBe SpektorPath.RequestBody(SpektorType.Binary, false, SpektorContentType.BINARY)
    }

    @Test
    fun `should parse octet stream response body`() {
        val schema = parser.parse(listOf(octetStreamApiFile))
        val paths = schema.paths.values.single()

        for (operationId in listOf("downloadEmptySchema", "downloadContentMediaType")) {
            val path = paths.single { it.operationId == operationId }
            path.responses.single { it.statusCode == 200 } shouldBe SpektorPath.Response(
                statusCode = 200,
                body = SpektorType.RequiredWrapper(SpektorType.Binary, true),
                contentType = SpektorContentType.BINARY,
            )
        }
    }

    @Test
    fun `should parse response without body next to octet stream response`() {
        val schema = parser.parse(listOf(octetStreamApiFile))

        val path = schema.paths.values.single().single { it.operationId == "downloadEmptySchema" }
        path.responses.single { it.statusCode == 404 } shouldBe SpektorPath.Response(statusCode = 404, body = null)
    }

    @Test
    fun `should fail on binary response with media type other than octet stream`() {
        val exception = shouldThrow<IllegalArgumentException> {
            parser.parse(listOf(binaryImageResponseApiFile))
        }

        exception.message shouldContain "application/octet-stream"
    }
}

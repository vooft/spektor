package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.test.TestFiles.imageApiFile
import io.kotest.matchers.shouldBe
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
}

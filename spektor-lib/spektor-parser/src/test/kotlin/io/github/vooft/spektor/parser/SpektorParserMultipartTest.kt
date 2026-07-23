package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorContentType
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.test.TestFiles.multipartApiFile
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorParserMultipartTest {

    private val parser = SpektorParser()

    @Test
    fun `should parse required multipart request body`() {
        val schema = parser.parse(listOf(multipartApiFile))

        val path = schema.paths.values.single().single { it.operationId == "uploadFile" }
        path.requestBody shouldBe SpektorPath.RequestBody(SpektorType.Multipart, true, SpektorContentType.MULTIPART_FORM_DATA)
    }

    @Test
    fun `should coerce optional multipart request body to required`() {
        val schema = parser.parse(listOf(multipartApiFile))

        val path = schema.paths.values.single().single { it.operationId == "uploadFileOptional" }
        path.requestBody shouldBe SpektorPath.RequestBody(SpektorType.Multipart, true, SpektorContentType.MULTIPART_FORM_DATA)
    }

    @Test
    fun `should not collect refs from multipart schema`() {
        val schema = parser.parse(listOf(multipartApiFile))

        schema.refs.keys.map { it.modelName } shouldContainExactly listOf("UploadedFile")
    }
}

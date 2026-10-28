package io.github.vooft.spektor

import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.Test

class DocumentTest {

    @Test
    fun `should upload document of any content type`() = testClient("user") {
        val response = client.post("/documents") {
            contentType(ContentType.Application.Pdf)
            setBody(byteArrayOf(1, 2, 3, 4, 5))
        }

        response.status shouldBe HttpStatusCode.OK

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        body.getValue("name").jsonPrimitive.content shouldBe "application/pdf"
        body.getValue("size").jsonPrimitive.long shouldBe 5L
    }

    @Test
    fun `should download document as octet stream`() = testClient("user") {
        val response = client.get("/documents/sample")

        response.status shouldBe HttpStatusCode.OK
        response.contentType()?.withoutParameters() shouldBe ContentType.Application.OctetStream
        response.bodyAsBytes() shouldBe byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)
    }
}

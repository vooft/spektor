package io.github.vooft.spektor

import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.Test

class UploadTest {

    @Test
    fun `should upload any content type`() = testClient("user") {
        val response = client.post("/uploads") {
            contentType(ContentType.Application.Pdf)
            setBody(byteArrayOf(1, 2, 3, 4, 5))
        }

        response.status shouldBe HttpStatusCode.OK

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        body.getValue("name").jsonPrimitive.content shouldBe "application/pdf"
        body.getValue("size").jsonPrimitive.long shouldBe 5L
    }
}

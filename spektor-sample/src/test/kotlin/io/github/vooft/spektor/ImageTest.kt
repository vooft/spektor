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

class ImageTest {

    @Test
    fun `should upload png image`() = testClient("user") {
        val response = client.post("/images") {
            contentType(ContentType.Image.PNG)
            setBody(byteArrayOf(1, 2, 3))
        }

        response.status shouldBe HttpStatusCode.OK

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        body.getValue("name").jsonPrimitive.content shouldBe "image/png"
        body.getValue("size").jsonPrimitive.long shouldBe 3L
    }

    @Test
    fun `should upload jpeg image`() = testClient("user") {
        val response = client.post("/images") {
            contentType(ContentType.Image.JPEG)
            setBody(byteArrayOf(1, 2, 3, 4))
        }

        response.status shouldBe HttpStatusCode.OK

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        body.getValue("name").jsonPrimitive.content shouldBe "image/jpeg"
        body.getValue("size").jsonPrimitive.long shouldBe 4L
    }
}

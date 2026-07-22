package io.github.vooft.spektor

import io.kotest.matchers.shouldBe
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.Test

class MultipartTest {

    @Test
    fun `should upload multipart file`() = testClient("user") {
        val response = client.post("/files") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "test file")
                        append(
                            "file",
                            byteArrayOf(1, 2, 3, 4, 5),
                            Headers.build {
                                append(HttpHeaders.ContentType, "application/octet-stream")
                                append(HttpHeaders.ContentDisposition, "filename=\"test.bin\"")
                            }
                        )
                    }
                )
            )
        }

        response.status shouldBe HttpStatusCode.OK

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        body.getValue("name").jsonPrimitive.content shouldBe "test.bin"
        body.getValue("size").jsonPrimitive.long shouldBe 5L
        body.getValue("description").jsonPrimitive.content shouldBe "test file"
    }
}

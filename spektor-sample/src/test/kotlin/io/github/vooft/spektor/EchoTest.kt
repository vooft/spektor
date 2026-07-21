package io.github.vooft.spektor

import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Test

class EchoTest {

    @Test
    fun `should echo plain text request body`() = testClient("user") {
        val response = client.post("/echo") {
            contentType(ContentType.Text.Plain)
            setBody("hello, spektor!")
        }

        response.status shouldBe HttpStatusCode.OK
        response.contentType()?.withoutParameters() shouldBe ContentType.Text.Plain
        response.bodyAsText() shouldBe "hello, spektor!"
    }
}

package io.github.vooft.spektor

import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Test

class PingTest {

    @Test
    fun `should return pong as plain text`() = testClient("user") {
        val response = client.get("/ping")

        response.status shouldBe HttpStatusCode.OK
        response.contentType()?.withoutParameters() shouldBe ContentType.Text.Plain
        response.bodyAsText() shouldBe "pong"
    }
}

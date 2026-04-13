package io.github.vooft.spektor

import io.github.vooft.spektor.test.apis.EventTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.github.vooft.spektor.test.models.ClickEventTestDto
import io.github.vooft.spektor.test.models.EventTypeTestDto
import io.github.vooft.spektor.test.models.PingEventTestDto
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EventTest {

    @Test
    fun `should list events`() = testClient("admin") { client ->
        val api = EventTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val response = api.list()
        response.status shouldBe 200

        val events = response.body().events
        events shouldHaveSize 2

        events.map { it.actualInstance }.filterIsInstance<ClickEventTestDto>().single().run {
            type shouldBe EventTypeTestDto.CLICK
            x shouldBe 10
            y shouldBe 20
        }

        events.map { it.actualInstance }.filterIsInstance<PingEventTestDto>().single().run {
            type shouldBe EventTypeTestDto.PING
        }
    }
}

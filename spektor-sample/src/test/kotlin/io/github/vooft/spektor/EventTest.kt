package io.github.vooft.spektor

import io.github.vooft.spektor.test.models.EventTestDto
import io.github.vooft.spektor.test.models.EventTypeTestDto
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EventTest {

    @Test
    fun `should list events`() = testClient("admin") {
        val response = api.event.list()
        response.status shouldBe 200

        val events = response.body().events
        events shouldHaveSize 2

        events.filterIsInstance<EventTestDto.ClickTestDtoWrapper>().single().value.run {
            type shouldBe EventTypeTestDto.CLICK
            x shouldBe 10
            y shouldBe 20
        }

        events.filterIsInstance<EventTestDto.PingTestDtoWrapper>().single().value.run {
            type shouldBe EventTypeTestDto.PING
        }
    }
}

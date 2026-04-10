package io.github.vooft.spektor

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import spektor.example.models.event.EventDto
import spektor.example.models.event.EventsListDto

class EventTest {

    @Test
    fun `should list events with empty object variant`() = testClient("admin") { client ->
        val response = client.get("/event")
        response.status shouldBe HttpStatusCode.OK

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val events = body["events"].shouldNotBeNull().jsonArray
        events shouldHaveSize 2

        val click = events.first { it.jsonObject["type"].shouldNotBeNull().jsonPrimitive.content == "CLICK" }.jsonObject
        click["x"].shouldNotBeNull().jsonPrimitive.content shouldBe "10"
        click["y"].shouldNotBeNull().jsonPrimitive.content shouldBe "20"

        val ping = events.first { it.jsonObject["type"].shouldNotBeNull().jsonPrimitive.content == "PING" }.jsonObject
        ping.keys shouldBe setOf("type")
    }

    @Test
    fun `should deserialize events into sealed hierarchy`() = testClient("admin") { client ->
        val response = client.get("/event")
        response.status shouldBe HttpStatusCode.OK

        val eventsList = Json.decodeFromString<EventsListDto>(response.bodyAsText())
        eventsList.events shouldHaveSize 2

        val click = eventsList.events.filterIsInstance<EventDto.ClickEventDto>().single()
        click.x shouldBe 10
        click.y shouldBe 20

        val ping = eventsList.events.filterIsInstance<EventDto.PingEventDto>().single()
        ping shouldBe EventDto.PingEventDto
    }

    @Test
    fun `should serialize PingEvent as object with only discriminator`() {
        val json = Json.encodeToString(EventsListDto(events = listOf(EventDto.PingEventDto)))
        val parsed = Json.parseToJsonElement(json).jsonObject
        val events = parsed["events"].shouldNotBeNull().jsonArray
        events shouldHaveSize 1

        val ping = events.single().jsonObject
        ping.keys shouldBe setOf("type")
        ping["type"].shouldNotBeNull().jsonPrimitive.content shouldBe "PING"
    }

    @Test
    fun `should round-trip serialize and deserialize ClickEvent`() {
        val original = EventsListDto(events = listOf(EventDto.ClickEventDto(x = 42, y = 99)))
        val json = Json.encodeToString(original)
        val deserialized = Json.decodeFromString<EventsListDto>(json)

        deserialized.events shouldHaveSize 1
        val click = deserialized.events.single().shouldBeInstanceOf<EventDto.ClickEventDto>()
        click.x shouldBe 42
        click.y shouldBe 99
    }
}

package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import spektor.example.api.event.EventServerApi
import spektor.example.api.event.EventServerApi.ListResponse
import spektor.example.models.event.EventDto
import spektor.example.models.event.EventsListDto

class EventRestService : EventServerApi {
    private val events = mutableListOf<EventDto>(
        EventDto.ClickEventDto(
            x = 10,
            y = 20,
        ),
        EventDto.PingEventDto,
    )

    override suspend fun list(call: ApplicationCall): ListResponse = ListResponse.ok(EventsListDto(events = events))
}

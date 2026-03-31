package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import spektor.example.api.owner.OwnerServerApi
import spektor.example.api.owner.OwnerServerApi.ListResponse
import spektor.example.models.owner.OwnerDto
import spektor.example.models.owner.OwnersListDto
import java.util.UUID

class OwnerRestService : OwnerServerApi {
    private val owners = mutableListOf<OwnerDto>(
        OwnerDto.IndividualDto(
            id = UUID.randomUUID(),
            firstName = "John",
            lastName = "Doe",
        ),
        OwnerDto.BusinessDto(
            id = UUID.randomUUID(),
            name = "Acme Corp",
        ),
    )

    override suspend fun list(call: ApplicationCall): ListResponse = ListResponse.ok(OwnersListDto(owners = owners))
}

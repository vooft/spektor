package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import spektor.example.api.ping.PingServerApi
import spektor.example.api.ping.PingServerApi.PingResponse

class PingRestService : PingServerApi {
    override suspend fun ping(call: ApplicationCall): PingResponse = PingResponse.ok("pong")
}

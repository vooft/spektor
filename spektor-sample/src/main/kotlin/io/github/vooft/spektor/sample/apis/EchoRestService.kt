package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import spektor.example.api.echo.EchoServerApi
import spektor.example.api.echo.EchoServerApi.EchoResponse

class EchoRestService : EchoServerApi {
    override suspend fun echo(request: String, call: ApplicationCall): EchoResponse = EchoResponse.ok(request)
}

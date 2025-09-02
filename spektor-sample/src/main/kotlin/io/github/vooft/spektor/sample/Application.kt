package io.github.vooft.spektor.sample

import io.ktor.server.application.Application
import io.ktor.server.cio.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureSecurity()
    configureSerialization()
    configureRouting()
}

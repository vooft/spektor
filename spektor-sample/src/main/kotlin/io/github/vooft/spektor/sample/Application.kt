package io.github.vooft.spektor.sample

import io.github.vooft.spektor.sample.ktor.configureDependencyInjection
import io.github.vooft.spektor.sample.ktor.configureRouting
import io.github.vooft.spektor.sample.ktor.configureSecurity
import io.github.vooft.spektor.sample.ktor.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.cio.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDependencyInjection()
    configureSecurity()
    configureSerialization()
    configureRouting()
}

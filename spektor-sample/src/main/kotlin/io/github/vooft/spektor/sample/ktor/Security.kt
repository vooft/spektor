package io.github.vooft.spektor.sample.ktor

import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic

fun Application.configureSecurity() {
    authentication {
        basic(name = "admin") {
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "admin") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        basic(name = "user") {
            validate { credentials ->
                if (credentials.name == "user" && credentials.password == "user") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}

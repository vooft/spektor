package io.github.vooft.spektor

import io.github.vooft.spektor.sample.module
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication

fun testClient(username: String, block: suspend (HttpClient) -> Unit) = testApplication {
    application { module() }
    block(
        createClient {
            install(ContentNegotiation) {
                json()
            }

            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = username, password = username)
                    }
                }
            }
        }
    )
}

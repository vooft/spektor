package io.github.vooft.spektor

import io.github.vooft.spektor.sample.ktor.serializersModule
import io.github.vooft.spektor.sample.module
import io.github.vooft.spektor.test.apis.AuthorTestApi
import io.github.vooft.spektor.test.apis.BookTestApi
import io.github.vooft.spektor.test.apis.EventTestApi
import io.github.vooft.spektor.test.apis.NotificationTestApi
import io.github.vooft.spektor.test.apis.OwnerTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json

fun testClient(username: String, block: suspend TestContext.() -> Unit) = testApplication {
    application { module() }

    val configure: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(testJson)
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = username, password = username)
                }
            }
        }
    }

    val rawClient = createClient { configure() }

    val ctx = TestContext(
        client = rawClient,
        api = Api(
            engine = rawClient.engine,
            apiConfig = { it.configure() },
        )
    )

    block(ctx)
}

class TestContext(
    val client: HttpClient,
    val api: Api,
)

class Api(
    engine: HttpClientEngine,
    apiConfig: (HttpClientConfig<*>) -> Unit,
) {

    val author: AuthorTestApi = AuthorTestApi(
        baseUrl = ApiClient.BASE_URL,
        httpClientEngine = engine,
        httpClientConfig = apiConfig,
    )

    val book: BookTestApi = BookTestApi(
        baseUrl = ApiClient.BASE_URL,
        httpClientEngine = engine,
        httpClientConfig = apiConfig,
    )

    val event: EventTestApi = EventTestApi(
        baseUrl = ApiClient.BASE_URL,
        httpClientEngine = engine,
        httpClientConfig = apiConfig,
    )

    val notification: NotificationTestApi = NotificationTestApi(
        baseUrl = ApiClient.BASE_URL,
        httpClientEngine = engine,
        httpClientConfig = apiConfig,
    )

    val owner: OwnerTestApi = OwnerTestApi(
        baseUrl = ApiClient.BASE_URL,
        httpClientEngine = engine,
        httpClientConfig = apiConfig,
    )
}

private val testJson = Json {
    ignoreUnknownKeys = true
    serializersModule = serializersModule()
}

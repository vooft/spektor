package io.github.vooft.spektor

import io.github.vooft.spektor.test.apis.AuthorTestApi
import io.github.vooft.spektor.test.apis.BookTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.github.vooft.spektor.test.models.AuthorRequestTestDto
import io.github.vooft.spektor.test.models.BookRequestTestDto
import io.github.vooft.spektor.test.models.CountryTestDto
import io.github.vooft.spektor.test.models.MoneyTestDto
import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class BookTest {
    @Test
    fun `should create book`() = testClient("admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val title = UUID.randomUUID().toString()
        val minorUnits = ThreadLocalRandom.current().nextInt(1000)
        val currency = "USD"
        val response = api.create(
            BookRequestTestDto(
                title = title,
                authorId = authorId,
                price = MoneyTestDto(
                    minorUnits = minorUnits,
                    currency = currency
                )
            )
        )

        response.status shouldBe 200

        val dto = response.body()
        dto.title shouldBe title
        dto.author.id shouldBe authorId
        dto.price?.minorUnits shouldBe minorUnits
        dto.price?.currency shouldBe currency
        dto.metadata shouldBe null
    }

    @Test
    fun `should create book with countryPrices`() = testClient("admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val title = UUID.randomUUID().toString()
        val countryPrices = mapOf(
            "US" to MoneyTestDto(minorUnits = 999, currency = "USD"),
            "DE" to MoneyTestDto(minorUnits = 899, currency = "EUR"),
        )
        val response = api.create(
            BookRequestTestDto(
                title = title,
                authorId = authorId,
                countryPrices = countryPrices,
            )
        )

        response.status shouldBe 200

        val dto = response.body()
        dto.title shouldBe title
        dto.countryPrices shouldBe countryPrices
    }

    @Test
    fun `should create book without countryPrices`() = testClient("admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val title = UUID.randomUUID().toString()
        val response = api.create(
            BookRequestTestDto(
                title = title,
                authorId = authorId,
            )
        )

        response.status shouldBe 200

        val dto = response.body()
        dto.title shouldBe title
        dto.countryPrices shouldBe emptyMap()
    }

    @Test
    fun `should create book with metadata`() = testClient("admin") { client ->
        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val title = UUID.randomUUID().toString()
        val response = client.post("/book") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"$title","authorId":"$authorId","metadata":{"key":"value","nested":{"a":1}}}""")
        }

        response.status.value shouldBe 200

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val metadata = body.getValue("metadata").jsonObject
        metadata.getValue("key").jsonPrimitive.content shouldBe "value"
        metadata.getValue("nested").jsonObject.getValue("a").jsonPrimitive.content shouldBe "1"
    }

    @Test
    fun `should create book without metadata`() = testClient("admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val response = api.create(
            BookRequestTestDto(
                title = UUID.randomUUID().toString(),
                authorId = authorId,
            )
        )

        response.status shouldBe 200
        response.body().metadata shouldBe null
    }

    @Test
    fun `should delete book`() = testClient("admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val bookId = api.create(
            BookRequestTestDto(
                title = UUID.randomUUID().toString(),
                authorId = authorId,
            )
        ).body().id

        val response = api.delete(bookId)
        response.status shouldBe 204
    }

    @Test
    fun `should update book with body`() = testClient("admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val bookId = api.create(
            BookRequestTestDto(
                title = "original title",
                authorId = authorId,
            )
        ).body().id

        val response = client.post("/book/$bookId/optional-update") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"updated title"}""")
        }

        response.status.value shouldBe 200
    }

    @Test
    fun `should update book without body`() = testClient("admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val authorId = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client).create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.JP,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        ).body().id

        val bookId = api.create(
            BookRequestTestDto(
                title = "original title",
                authorId = authorId,
            )
        ).body().id

        val response = client.post("/book/$bookId/optional-update")

        response.status.value shouldBe 200
    }

    @Test
    fun `should fail to create author with 401`() = testClient("not admin") { client ->
        val api = BookTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val response = api.create(
            BookRequestTestDto(
                title = "test",
                authorId = UUID.randomUUID().toString(),
            )
        )

        response.status shouldBe 401
    }
}

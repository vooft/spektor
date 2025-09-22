package io.github.vooft.spektor

import io.github.vooft.spektor.models.AuthorRequestTestDto
import io.github.vooft.spektor.test.apis.AuthorTestApi
import io.github.vooft.spektor.test.apis.BookTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.github.vooft.spektor.test.models.AuthorCountryTestDto
import io.github.vooft.spektor.test.models.BookRequestTestDto
import io.github.vooft.spektor.test.models.MoneyTestDto
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
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
                country = AuthorCountryTestDto.JP,
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

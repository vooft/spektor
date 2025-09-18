package io.github.vooft.spektor

import io.github.vooft.spektor.test.apis.AuthorTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.github.vooft.spektor.test.models.AuthorCountryTestDto
import io.github.vooft.spektor.test.models.AuthorRequestTestDto
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class AuthorTest {
    @Test
    fun `should create author`() = testClient("admin") { client ->
        val api = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val name = UUID.randomUUID().toString()
        val dob = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000))
        val dod = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000))
        val response = api.create(
            AuthorRequestTestDto(
                name = name,
                dateOfBirth = dob,
                country = AuthorCountryTestDto.DE,
                dateOfDeath = dod
            )
        )

        response.status shouldBe 200

        val dto = response.body()
        dto.name shouldBe name
        dto.dateOfBirth shouldBe dob
        dto.dateOfDeath shouldBe dod
    }

    @Test
    fun `should fail to create author with 401`() = testClient("not admin") { client ->
        val api = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val response = api.create(
            AuthorRequestTestDto(
                name = "test",
                country = AuthorCountryTestDto.US,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        )

        response.status shouldBe 401
    }

    @Test
    fun `should list authors for countries`() = testClient("admin") { client ->
        val api = AuthorTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        api.create(
            AuthorRequestTestDto(
                name = UUID.randomUUID().toString(),
                dateOfBirth = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
                country = AuthorCountryTestDto.DE,
                dateOfDeath = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
            )
        )

        api.create(
            AuthorRequestTestDto(
                name = UUID.randomUUID().toString(),
                dateOfBirth = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
                country = AuthorCountryTestDto.US,
                dateOfDeath = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
            )
        )

        api.create(
            AuthorRequestTestDto(
                name = UUID.randomUUID().toString(),
                dateOfBirth = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
                country = AuthorCountryTestDto.JP,
                dateOfDeath = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
            )
        )

        val response = api.list(countries = listOf(AuthorCountryTestDto.US, AuthorCountryTestDto.JP))

        response.status shouldBe 200

        response.body().authors.map { it.country } shouldContainExactlyInAnyOrder listOf(AuthorCountryTestDto.US, AuthorCountryTestDto.JP)
    }
}

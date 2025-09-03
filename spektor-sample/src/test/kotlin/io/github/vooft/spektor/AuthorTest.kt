package io.github.vooft.spektor

import io.github.vooft.spektor.test.apis.AuthorTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.github.vooft.spektor.test.models.AuthorRequestTestDto
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
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        )

        response.status shouldBe 401
    }
}

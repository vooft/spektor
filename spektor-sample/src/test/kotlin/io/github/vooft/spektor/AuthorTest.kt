package io.github.vooft.spektor

import io.github.vooft.spektor.test.models.AuthorRequestTestDto
import io.github.vooft.spektor.test.models.CountryTestDto
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class AuthorTest {
    @Test
    fun `should create author`() = testClient("admin") {
        val name = UUID.randomUUID().toString()
        val dob = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000))
        val dod = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000))
        val response = api.author.create(
            AuthorRequestTestDto(
                name = name,
                dateOfBirth = dob,
                country = CountryTestDto.DE,
                dateOfDeath = dod,
                additionalDetails = buildJsonObject {
                    put("ref", "some-ref")
                }
            )
        )

        response.status shouldBe 200

        val dto = response.body()
        dto.name shouldBe name
        dto.dateOfBirth shouldBe dob
        dto.dateOfDeath shouldBe dod
        dto.additionalDetails.toString() shouldBe """{"ref":"some-ref"}"""
    }

    @Test
    fun `should fail to create author with 401`() = testClient("not admin") {
        val response = api.author.create(
            AuthorRequestTestDto(
                name = "test",
                country = CountryTestDto.US,
                dateOfBirth = LocalDate.parse("1800-01-01"),
            )
        )

        response.status shouldBe 401
    }

    @Test
    fun `should fail to get author with invalid id`() = testClient("admin") {
        val response = client.get("/author/not-a-valid-uuid")

        response.status.value shouldBe 400
    }

    @Test
    fun `should list authors for countries`() = testClient("admin") {
        api.author.create(
            AuthorRequestTestDto(
                name = UUID.randomUUID().toString(),
                dateOfBirth = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
                country = CountryTestDto.DE,
                dateOfDeath = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
            )
        )

        api.author.create(
            AuthorRequestTestDto(
                name = UUID.randomUUID().toString(),
                dateOfBirth = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
                country = CountryTestDto.US,
                dateOfDeath = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
            )
        )

        api.author.create(
            AuthorRequestTestDto(
                name = UUID.randomUUID().toString(),
                dateOfBirth = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
                country = CountryTestDto.JP,
                dateOfDeath = LocalDate.fromEpochDays(ThreadLocalRandom.current().nextInt(1000)),
            )
        )

        val response = api.author.list(countries = listOf(CountryTestDto.US, CountryTestDto.JP))

        response.status shouldBe 200

        response.body().authors.map { it.country } shouldContainExactlyInAnyOrder listOf(CountryTestDto.US, CountryTestDto.JP)
    }
}

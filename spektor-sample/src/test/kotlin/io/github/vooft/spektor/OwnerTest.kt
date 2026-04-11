package io.github.vooft.spektor

import io.github.vooft.spektor.test.apis.OwnerTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.github.vooft.spektor.test.models.BusinessTestDto
import io.github.vooft.spektor.test.models.IndividualTestDto
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OwnerTest {

    @Test
    fun `should list owners`() = testClient("admin") { client ->
        val api = OwnerTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val response = api.list()

        response.status shouldBe 200

        val owners = response.body().owners
        owners shouldHaveSize 2

        owners.map { it.actualInstance }.filterIsInstance<IndividualTestDto>().single().run {
            type shouldBe IndividualTestDto.Type.INDIVIDUAL
            firstName shouldBe "John"
            lastName shouldBe "Doe"
        }

        owners.map { it.actualInstance }.filterIsInstance<BusinessTestDto>().single().run {
            type shouldBe BusinessTestDto.Type.BUSINESS
            name shouldBe "Acme Corp"
        }
    }
}

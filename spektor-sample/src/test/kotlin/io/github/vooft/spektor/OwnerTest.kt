package io.github.vooft.spektor

import io.github.vooft.spektor.test.models.OwnerTestDto
import io.github.vooft.spektor.test.models.OwnerTypeTestDto
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OwnerTest {

    @Test
    fun `should list owners`() = testClient("admin") {
        val response = api.owner.list()

        response.status shouldBe 200

        val owners = response.body().owners
        owners shouldHaveSize 2

        owners.filterIsInstance<OwnerTestDto.IndividualTestDtoWrapper>().single().value.run {
            type shouldBe OwnerTypeTestDto.INDIVIDUAL
            firstName shouldBe "John"
            lastName shouldBe "Doe"
        }

        owners.filterIsInstance<OwnerTestDto.BusinessTestDtoWrapper>().single().value.run {
            type shouldBe OwnerTypeTestDto.BUSINESS
            name shouldBe "Acme Corp"
        }
    }
}

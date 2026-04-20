package io.github.vooft.spektor

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class NotificationTest {

    @Test
    fun `should return notification settings`() = testClient("admin") {
        val response = api.notification.getSettings()

        response.status shouldBe 200

        response.body() shouldBe mapOf(
            "PUSH" to mapOf(
                "CARD_PAYMENT" to true,
                "REWARDS" to true,
            ),
            "SMS" to mapOf(
                "CARD_PAYMENT" to true,
                "REWARDS" to false,
            ),
        )
    }
}

package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import spektor.example.api.notification.NotificationServerApi
import spektor.example.api.notification.NotificationServerApi.GetSettingsResponse
import spektor.example.models.notification.NotificationCategoryDto

class NotificationRestService : NotificationServerApi {
    private val settings: Map<String, Map<NotificationCategoryDto, Boolean>> = mapOf(
        "PUSH" to mapOf(
            NotificationCategoryDto.CARD_PAYMENT to true,
            NotificationCategoryDto.REWARDS to true,
        ),
        "SMS" to mapOf(
            NotificationCategoryDto.CARD_PAYMENT to true,
            NotificationCategoryDto.REWARDS to false,
        ),
    )

    override suspend fun getSettings(call: ApplicationCall): GetSettingsResponse = GetSettingsResponse.ok(settings)
}

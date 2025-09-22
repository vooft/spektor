package io.github.vooft.spektor.models

import io.github.vooft.spektor.test.models.AuthorCountryTestDto
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AuthorTestDto(
    val id: String,
    val name: String,
    val dateOfBirth: LocalDate,
    val country: AuthorCountryTestDto,
    val createdAt: Instant,
    val additionalDetails: JsonObject? = null,
    val dateOfDeath: LocalDate? = null
)

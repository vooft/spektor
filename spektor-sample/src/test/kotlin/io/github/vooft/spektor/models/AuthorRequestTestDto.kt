package io.github.vooft.spektor.models

import io.github.vooft.spektor.test.models.AuthorCountryTestDto
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AuthorRequestTestDto(
    val name: String,
    val dateOfBirth: LocalDate,
    val country: AuthorCountryTestDto,
    val dateOfDeath: LocalDate? = null,
    val additionalDetails: JsonObject? = null,
)

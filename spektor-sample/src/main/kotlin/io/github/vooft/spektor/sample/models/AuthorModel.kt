package io.github.vooft.spektor.sample.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class AuthorModel(
    val id: AuthorId,
    val name: String,
    val dateOfBirth: LocalDate,
    val dateOfDeath: LocalDate?,
    val country: String,
    val createdAt: Instant,
    val additionalDetails: JsonObject? = null,
)

@Serializable
@JvmInline
value class AuthorId(val value: @Contextual UUID)

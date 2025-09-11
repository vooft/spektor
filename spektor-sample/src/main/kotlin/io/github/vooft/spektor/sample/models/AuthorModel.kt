package io.github.vooft.spektor.sample.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class AuthorModel(
    val id: AuthorId,
    val name: String,
    val dateOfBirth: LocalDate,
    val dateOfDeath: LocalDate?,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class AuthorId(val value: @Contextual UUID)

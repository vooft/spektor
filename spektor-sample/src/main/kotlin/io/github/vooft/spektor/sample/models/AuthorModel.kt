package io.github.vooft.spektor.sample.models

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class AuthorModel(
    val id: UUID,
    val name: String,
    val dateOfBirth: LocalDate,
    val dateOfDeath: LocalDate?,
    val createdAt: Instant,
)

@JvmInline
value class AuthorId(val value: UUID)

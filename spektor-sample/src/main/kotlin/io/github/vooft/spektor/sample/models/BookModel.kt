package io.github.vooft.spektor.sample.models

import java.time.Instant
import java.util.UUID

data class BookModel(
    val id: UUID,
    val title: String,
    val nativeTitle: String?,
    val authorId: UUID,
    val price: Money?,
    val createdAt: Instant
)

@JvmInline
value class BookId(val value: UUID)

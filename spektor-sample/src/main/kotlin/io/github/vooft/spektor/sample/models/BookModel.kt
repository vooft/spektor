package io.github.vooft.spektor.sample.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.URI
import java.time.Instant
import java.time.YearMonth
import java.util.UUID

data class BookModel(
    val id: BookId,
    val title: String,
    val nativeTitle: String?,
    val imageLink: URI,
    val authorId: AuthorId,
    val price: Money?,
    val createdAt: Instant,
    val yearMonth: YearMonth,
    val referenceLinks: List<URI>,
)

@Serializable
@JvmInline
value class BookId(@Contextual val value: UUID)

package io.github.vooft.spektor.sample.apis

import io.github.vooft.spektor.sample.models.AuthorModel
import io.github.vooft.spektor.sample.models.BookModel
import io.github.vooft.spektor.sample.repository.AuthorRepository
import spektor.example.models.AuthorDto
import spektor.example.models.BookDto
import spektor.example.models.MoneyDto

object Mappers {
    fun AuthorModel.toDto() = AuthorDto(
        id = id,
        name = name,
        dateOfBirth = dateOfBirth,
        dateOfDeath = dateOfDeath,
        createdAt = createdAt,
    )

    fun BookModel.toDto(authors: AuthorRepository) = BookDto(
        id = id,
        title = title,
        nativeTitle = nativeTitle,
        author = authors.single { it.id == authorId }.toDto(),
        price = price?.let { MoneyDto(it.minorUnits, it.currency) },
        createdAt = createdAt
    )
}

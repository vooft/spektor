package io.github.vooft.spektor.sample.apis

import io.github.vooft.spektor.sample.models.AuthorModel
import io.github.vooft.spektor.sample.models.BookModel
import io.github.vooft.spektor.sample.repository.AuthorRepository
import spektor.example.models.author.AuthorDto
import spektor.example.models.book.BookDto
import spektor.example.models.country.CountryDto

object Mappers {
    fun AuthorModel.toDto() = AuthorDto(
        id = id,
        name = name,
        dateOfBirth = dateOfBirth,
        dateOfDeath = dateOfDeath,
        country = CountryDto.valueOf(country),
        createdAt = createdAt,
        additionalDetails = additionalDetails,
    )

    fun BookModel.toDto(authors: AuthorRepository) = BookDto(
        id = id,
        title = title,
        nativeTitle = nativeTitle,
        imageLink = imageLink,
        author = authors.single { it.id == authorId }.toDto(),
        price = price,
        countryPrices = countryPrices,
        metadata = metadata,
        createdAt = createdAt,
        yearMonth = yearMonth,
        referenceLinks = referenceLinks,
    )
}

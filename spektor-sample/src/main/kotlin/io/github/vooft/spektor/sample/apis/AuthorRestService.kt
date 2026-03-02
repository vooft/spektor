package io.github.vooft.spektor.sample.apis

import io.github.vooft.spektor.sample.apis.Mappers.toDto
import io.github.vooft.spektor.sample.models.AuthorId
import io.github.vooft.spektor.sample.models.AuthorModel
import io.github.vooft.spektor.sample.repository.AuthorRepository
import io.github.vooft.spektor.sample.repository.BookRepository
import io.ktor.server.application.ApplicationCall
import spektor.example.api.author.AuthorServerApi
import spektor.example.api.author.AuthorServerApi.CreateResponse
import spektor.example.api.author.AuthorServerApi.GetResponse
import spektor.example.api.author.AuthorServerApi.ListResponse
import spektor.example.api.author.AuthorServerApi.SearchBooksResponse
import spektor.example.models.author.AuthorCountryDto
import spektor.example.models.author.AuthorRequestDto
import spektor.example.models.author.AuthorsListDto
import spektor.example.models.book.BooksListDto
import java.time.Instant
import java.util.UUID

class AuthorRestService(private val authors: AuthorRepository, private val books: BookRepository) : AuthorServerApi {
    override suspend fun list(countries: List<AuthorCountryDto>?, call: ApplicationCall): ListResponse {
        val countryNames = countries?.map { it.name } ?: emptyList()
        val filteredAuthors = if (countryNames.isNotEmpty()) {
            authors.filter { it.country in countryNames }
        } else {
            authors
        }
        return ListResponse.Ok(
            AuthorsListDto(authors = filteredAuthors.map { it.toDto() })
        )
    }

    override suspend fun create(request: AuthorRequestDto, call: ApplicationCall): CreateResponse {
        val author = AuthorModel(
            id = AuthorId(UUID.randomUUID()),
            name = request.name,
            dateOfBirth = request.dateOfBirth,
            dateOfDeath = request.dateOfDeath,
            country = request.country.name,
            createdAt = Instant.now(),
            additionalDetails = request.additionalDetails,
        )

        authors.addAuthor(author)
        return CreateResponse.Ok(author.toDto())
    }

    override suspend fun get(id: UUID, call: ApplicationCall): GetResponse {
        val author = authors.single { it.id.value == id }
        return GetResponse.Ok(author.toDto())
    }

    override suspend fun searchBooks(id: UUID, filter: String, call: ApplicationCall): SearchBooksResponse {
        val books = books.filter { it.authorId.value == id }.filter { it.title.contains(filter, ignoreCase = true) }
        return SearchBooksResponse.Ok(BooksListDto(books = books.map { it.toDto(authors) }))
    }
}

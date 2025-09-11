package io.github.vooft.spektor.sample.apis

import io.github.vooft.spektor.sample.apis.Mappers.toDto
import io.github.vooft.spektor.sample.models.AuthorModel
import io.github.vooft.spektor.sample.repository.AuthorRepository
import io.github.vooft.spektor.sample.repository.BookRepository
import io.ktor.server.application.ApplicationCall
import spektor.example.api.AuthorServerApi
import spektor.example.models.AuthorDto
import spektor.example.models.AuthorRequestDto
import spektor.example.models.AuthorsListDto
import spektor.example.models.BooksListDto
import java.time.Instant
import java.util.UUID

class AuthorRestService(private val authors: AuthorRepository, private val books: BookRepository) : AuthorServerApi {
    override fun list(call: ApplicationCall): AuthorsListDto = AuthorsListDto(
        authors = authors.map { it.toDto() }
    )

    override fun create(request: AuthorRequestDto, call: ApplicationCall): AuthorDto {
        val author = AuthorModel(
            id = UUID.randomUUID(),
            name = request.name,
            dateOfBirth = request.dateOfBirth,
            dateOfDeath = request.dateOfDeath,
            createdAt = Instant.now()
        )

        authors.addAuthor(author)
        return author.toDto()
    }

    override fun get(id: UUID, call: ApplicationCall): AuthorDto {
        val author = authors.single { it.id == id }
        return author.toDto()
    }

    override fun searchBooks(id: UUID, filter: String, call: ApplicationCall): BooksListDto {
        val books = books.filter { it.authorId == id }.filter { it.title.contains(filter, ignoreCase = true) }
        return BooksListDto(books = books.map { it.toDto(authors) })
    }
}

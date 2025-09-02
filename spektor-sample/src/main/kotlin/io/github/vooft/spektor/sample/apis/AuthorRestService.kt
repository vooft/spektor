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
import java.time.LocalDate
import java.util.UUID

class AuthorRestService(private val authors: AuthorRepository, private val books: BookRepository) : AuthorServerApi {
    override fun list(call: ApplicationCall): AuthorsListDto = AuthorsListDto(
        authors = authors.map { it.toDto() }
    )

    override fun create(request: AuthorRequestDto, call: ApplicationCall): AuthorDto {
        val author = AuthorModel(
            id = UUID.randomUUID(),
            name = request.name,
            dateOfBirth = LocalDate.parse(request.dateOfBirth),
            dateOfDeath = request.dateOfDeath?.let { LocalDate.parse(it) },
            createdAt = Instant.now()
        )

        authors.addAuthor(author)
        return author.toDto()
    }

    override fun get(id: String, call: ApplicationCall): AuthorDto {
        val author = authors.single { it.id.toString() == id }
        return author.toDto()
    }

    override fun searchBooks(id: String, filter: String, call: ApplicationCall): BooksListDto {
        val authorId = UUID.fromString(id)
        val books = books.filter { it.authorId == authorId }.filter { it.title.contains(filter, ignoreCase = true) }
        return BooksListDto(books = books.map { it.toDto(authors) })
    }
}

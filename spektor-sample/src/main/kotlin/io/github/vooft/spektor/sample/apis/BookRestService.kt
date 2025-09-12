package io.github.vooft.spektor.sample.apis

import io.github.vooft.spektor.sample.apis.Mappers.toDto
import io.github.vooft.spektor.sample.models.BookId
import io.github.vooft.spektor.sample.models.BookModel
import io.github.vooft.spektor.sample.repository.AuthorRepository
import io.github.vooft.spektor.sample.repository.BookRepository
import io.ktor.server.application.ApplicationCall
import spektor.example.api.BookServerApi
import spektor.example.models.BookDto
import spektor.example.models.BookRequestDto
import java.net.URI
import java.time.Instant
import java.util.UUID

class BookRestService(
    private val authors: AuthorRepository,
    private val books: BookRepository
) : BookServerApi {
    override suspend fun create(request: BookRequestDto, call: ApplicationCall): BookDto {
        val book = BookModel(
            id = BookId(UUID.randomUUID()),
            title = request.title,
            nativeTitle = request.nativeTitle,
            imageLink = URI.create("https://books.net/images/${UUID.randomUUID()}.jpg"),
            authorId = request.authorId,
            price = request.price,
            createdAt = Instant.now()
        )

        books.addBook(book)
        return book.toDto(authors)
    }
}

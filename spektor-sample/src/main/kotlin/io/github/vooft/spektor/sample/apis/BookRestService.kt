package io.github.vooft.spektor.sample.apis

import io.github.vooft.spektor.sample.apis.Mappers.toDto
import io.github.vooft.spektor.sample.models.BookId
import io.github.vooft.spektor.sample.models.BookModel
import io.github.vooft.spektor.sample.repository.AuthorRepository
import io.github.vooft.spektor.sample.repository.BookRepository
import io.ktor.server.application.ApplicationCall
import kotlinx.serialization.Contextual
import spektor.example.api.book.BookServerApi
import spektor.example.api.book.BookServerApi.CreateResponse
import spektor.example.api.book.BookServerApi.DeleteResponse
import spektor.example.api.book.BookServerApi.OptionalUpdateResponse
import spektor.example.models.book.BookRequestDto
import spektor.example.models.book.BookUpdateRequestDto
import java.net.URI
import java.time.Instant
import java.time.YearMonth
import java.util.UUID

class BookRestService(
    private val authors: AuthorRepository,
    private val books: BookRepository
) : BookServerApi {

    override suspend fun create(request: BookRequestDto, call: ApplicationCall): CreateResponse {
        val book = BookModel(
            id = BookId(UUID.randomUUID()),
            title = request.title,
            nativeTitle = request.nativeTitle,
            imageLink = URI.create("https://books.net/images/${UUID.randomUUID()}.jpg"),
            authorId = request.authorId,
            price = request.price,
            createdAt = Instant.now(),
            yearMonth = YearMonth.now(),
            referenceLinks = listOf(URI.create("https://books.net/books/${UUID.randomUUID()}")),
        )

        books.addBook(book)
        return CreateResponse.ok(book.toDto(authors))
    }

    override suspend fun delete(id: UUID, call: ApplicationCall): DeleteResponse {
        books.removeBook(BookId(id))
        return DeleteResponse.noContent()
    }

    override suspend fun optionalUpdate(
        request: BookUpdateRequestDto?,
        id: @Contextual UUID,
        call: ApplicationCall
    ): OptionalUpdateResponse {
        if (request != null) {
            val book = books.single { it.id.value == id }
            books.removeBook(book.id)
            books.addBook(
                book.copy(
                    title = request.title
                )
            )
        }
        return OptionalUpdateResponse.ok()
    }
}

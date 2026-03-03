package io.github.vooft.spektor.sample.repository

import io.github.vooft.spektor.sample.models.BookId
import io.github.vooft.spektor.sample.models.BookModel

class BookRepository(
    private val books: MutableList<BookModel> = mutableListOf(),
) : List<BookModel> by books {

    fun addBook(book: BookModel) {
        books.add(book)
    }

    fun removeBook(id: BookId) {
        books.removeIf { it.id == id }
    }
}

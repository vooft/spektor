package io.github.vooft.spektor.sample.ktor

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import spektor.example.api.author.AuthorRoutes
import spektor.example.api.book.BookRoutes

fun Application.configureRouting() {
    val authorRoutes: AuthorRoutes by dependencies
    val bookRoutes: BookRoutes by dependencies

    routing {
        authenticate("admin") {
            authorRoutes.create()
            bookRoutes.create()
        }

        authenticate("admin", "user") {
            authorRoutes.list()
            authorRoutes.get()
            authorRoutes.searchBooks()
        }
    }
}

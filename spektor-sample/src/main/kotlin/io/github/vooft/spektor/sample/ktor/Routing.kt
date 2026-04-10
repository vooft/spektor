package io.github.vooft.spektor.sample.ktor

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import spektor.example.api.author.AuthorRoutes
import spektor.example.api.book.BookRoutes
import spektor.example.api.event.EventRoutes
import spektor.example.api.owner.OwnerRoutes

fun Application.configureRouting() {
    val authorRoutes: AuthorRoutes by dependencies
    val bookRoutes: BookRoutes by dependencies
    val ownerRoutes: OwnerRoutes by dependencies
    val eventRoutes: EventRoutes by dependencies

    routing {
        authenticate("admin") {
            authorRoutes.create()
            bookRoutes.create()
            bookRoutes.delete()
            bookRoutes.optionalUpdate()
        }

        authenticate("admin", "user") {
            authorRoutes.list()
            authorRoutes.get()
            authorRoutes.searchBooks()
            ownerRoutes.list()
            eventRoutes.list()
        }
    }
}

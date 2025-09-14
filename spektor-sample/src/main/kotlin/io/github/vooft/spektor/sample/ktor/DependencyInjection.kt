package io.github.vooft.spektor.sample.ktor

import io.github.vooft.spektor.sample.apis.AuthorRestService
import io.github.vooft.spektor.sample.apis.BookRestService
import io.github.vooft.spektor.sample.repository.AuthorRepository
import io.github.vooft.spektor.sample.repository.BookRepository
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import spektor.example.api.author.AuthorRoutes
import spektor.example.api.book.BookRoutes

fun Application.configureDependencyInjection() {
    dependencies {
        provide { AuthorRepository() }
        provide { BookRepository() }

        provide { AuthorRestService(resolve(), resolve()) }
        provide { BookRestService(resolve(), resolve()) }

        provide { AuthorRoutes(resolve()) }
        provide { BookRoutes(resolve()) }
    }
}

package io.github.vooft.spektor.sample.ktor

import io.github.vooft.spektor.sample.apis.AuthorRestService
import io.github.vooft.spektor.sample.apis.BookRestService
import io.github.vooft.spektor.sample.apis.EchoRestService
import io.github.vooft.spektor.sample.apis.EventRestService
import io.github.vooft.spektor.sample.apis.ImageRestService
import io.github.vooft.spektor.sample.apis.MultipartRestService
import io.github.vooft.spektor.sample.apis.NotificationRestService
import io.github.vooft.spektor.sample.apis.OwnerRestService
import io.github.vooft.spektor.sample.apis.PingRestService
import io.github.vooft.spektor.sample.repository.AuthorRepository
import io.github.vooft.spektor.sample.repository.BookRepository
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import spektor.example.api.author.AuthorRoutes
import spektor.example.api.book.BookRoutes
import spektor.example.api.echo.EchoRoutes
import spektor.example.api.event.EventRoutes
import spektor.example.api.image.ImageRoutes
import spektor.example.api.multipart.MultipartRoutes
import spektor.example.api.notification.NotificationRoutes
import spektor.example.api.owner.OwnerRoutes
import spektor.example.api.ping.PingRoutes

fun Application.configureDependencyInjection() {
    dependencies {
        provide { AuthorRepository() }
        provide { BookRepository() }

        provide { AuthorRestService(resolve(), resolve()) }
        provide { BookRestService(resolve(), resolve()) }
        provide { OwnerRestService() }
        provide { EventRestService() }
        provide { NotificationRestService() }
        provide { PingRestService() }
        provide { EchoRestService() }
        provide { MultipartRestService() }
        provide { ImageRestService() }

        provide { AuthorRoutes(resolve()) }
        provide { BookRoutes(resolve()) }
        provide { OwnerRoutes(resolve()) }
        provide { EventRoutes(resolve()) }
        provide { NotificationRoutes(resolve()) }
        provide { PingRoutes(resolve()) }
        provide { EchoRoutes(resolve()) }
        provide { MultipartRoutes(resolve()) }
        provide { ImageRoutes(resolve()) }
    }
}

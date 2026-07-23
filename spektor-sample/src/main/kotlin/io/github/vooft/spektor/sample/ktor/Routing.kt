package io.github.vooft.spektor.sample.ktor

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import spektor.example.api.author.AuthorRoutes
import spektor.example.api.book.BookRoutes
import spektor.example.api.echo.EchoRoutes
import spektor.example.api.event.EventRoutes
import spektor.example.api.image.ImageRoutes
import spektor.example.api.multipart.MultipartRoutes
import spektor.example.api.notification.NotificationRoutes
import spektor.example.api.owner.OwnerRoutes
import spektor.example.api.ping.PingRoutes
import spektor.example.api.upload.UploadRoutes

fun Application.configureRouting() {
    val authorRoutes: AuthorRoutes by dependencies
    val bookRoutes: BookRoutes by dependencies
    val ownerRoutes: OwnerRoutes by dependencies
    val eventRoutes: EventRoutes by dependencies
    val notificationRoutes: NotificationRoutes by dependencies
    val pingRoutes: PingRoutes by dependencies
    val echoRoutes: EchoRoutes by dependencies
    val multipartRoutes: MultipartRoutes by dependencies
    val imageRoutes: ImageRoutes by dependencies
    val uploadRoutes: UploadRoutes by dependencies

    routing {
        pingRoutes.ping()
        echoRoutes.echo()
        multipartRoutes.uploadFile()
        imageRoutes.uploadPng()
        imageRoutes.uploadAnyImage()
        uploadRoutes.upload()

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
            notificationRoutes.getSettings()
        }
    }
}

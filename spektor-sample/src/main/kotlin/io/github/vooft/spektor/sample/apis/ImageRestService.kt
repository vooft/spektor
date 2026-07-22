package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentType
import spektor.example.api.image.ImageServerApi
import spektor.example.api.image.ImageServerApi.UploadImageResponse
import spektor.example.models.uploadedfile.UploadedFileDto

class ImageRestService : ImageServerApi {
    override suspend fun uploadImage(request: ByteArray, call: ApplicationCall): UploadImageResponse = UploadImageResponse.ok(
        UploadedFileDto(
            name = call.request.contentType().toString(),
            size = request.size.toLong(),
            description = null,
        )
    )
}

package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentType
import spektor.example.api.image.ImageServerApi
import spektor.example.api.image.ImageServerApi.UploadAnyResponse
import spektor.example.api.image.ImageServerApi.UploadImageResponse
import spektor.example.models.uploadedfile.UploadedFileDto

class ImageRestService : ImageServerApi {
    override suspend fun uploadImage(request: ByteArray, call: ApplicationCall): UploadImageResponse = UploadImageResponse.ok(
        request.toUploadedFileDto(call)
    )

    override suspend fun uploadAny(request: ByteArray, call: ApplicationCall): UploadAnyResponse = UploadAnyResponse.ok(
        request.toUploadedFileDto(call)
    )

    private fun ByteArray.toUploadedFileDto(call: ApplicationCall) = UploadedFileDto(
        name = call.request.contentType().toString(),
        size = size.toLong(),
        description = null,
    )
}

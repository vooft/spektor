package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentType
import spektor.example.api.image.ImageServerApi
import spektor.example.api.image.ImageServerApi.UploadAnyImageResponse
import spektor.example.api.image.ImageServerApi.UploadPngResponse
import spektor.example.models.uploadedfile.UploadedFileDto

class ImageRestService : ImageServerApi {
    override suspend fun uploadPng(request: ByteArray, call: ApplicationCall): UploadPngResponse = UploadPngResponse.ok(
        request.toUploadedFileDto(call)
    )

    override suspend fun uploadAnyImage(request: ByteArray, call: ApplicationCall): UploadAnyImageResponse = UploadAnyImageResponse.ok(
        request.toUploadedFileDto(call)
    )

    private fun ByteArray.toUploadedFileDto(call: ApplicationCall) = UploadedFileDto(
        name = call.request.contentType().toString(),
        size = size.toLong(),
        description = null,
    )
}

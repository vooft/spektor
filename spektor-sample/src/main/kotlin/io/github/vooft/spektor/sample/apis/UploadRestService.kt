package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentType
import spektor.example.api.upload.UploadServerApi
import spektor.example.api.upload.UploadServerApi.UploadResponse
import spektor.example.models.uploadedfile.UploadedFileDto

class UploadRestService : UploadServerApi {
    override suspend fun upload(request: ByteArray, call: ApplicationCall): UploadResponse = UploadResponse.ok(
        UploadedFileDto(
            name = call.request.contentType().toString(),
            size = request.size.toLong(),
            description = null,
        )
    )
}

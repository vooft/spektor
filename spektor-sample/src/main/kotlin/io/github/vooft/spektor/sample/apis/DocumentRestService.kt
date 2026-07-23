package io.github.vooft.spektor.sample.apis

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentType
import spektor.example.api.document.DocumentServerApi
import spektor.example.api.document.DocumentServerApi.UploadDocumentResponse
import spektor.example.models.uploadedfile.UploadedFileDto

class DocumentRestService : DocumentServerApi {
    override suspend fun uploadDocument(request: ByteArray, call: ApplicationCall): UploadDocumentResponse = UploadDocumentResponse.ok(
        UploadedFileDto(
            name = call.request.contentType().toString(),
            size = request.size.toLong(),
            description = null,
        )
    )
}

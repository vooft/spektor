package io.github.vooft.spektor.sample.apis

import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import spektor.example.api.multipart.MultipartServerApi
import spektor.example.api.multipart.MultipartServerApi.UploadFileResponse
import spektor.example.api.multipart.MultipartServerApi.UploadFilesResponse
import spektor.example.models.uploadedfile.UploadedFileDto

class MultipartRestService : MultipartServerApi {
    override suspend fun uploadFile(request: MultiPartData, call: ApplicationCall): UploadFileResponse {
        var fileName: String? = null
        var size = 0L
        var description: String? = null

        request.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    fileName = part.originalFileName
                    size = part.provider().readRemaining().readByteArray().size.toLong()
                }

                is PartData.FormItem -> if (part.name == "description") {
                    description = part.value
                }

                else -> Unit
            }
            part.release()
        }

        return UploadFileResponse.ok(
            UploadedFileDto(
                name = fileName ?: "unknown",
                size = size,
                description = description,
            )
        )
    }

    override suspend fun uploadFiles(request: MultiPartData, call: ApplicationCall): UploadFilesResponse {
        val files = mutableListOf<UploadedFileDto>()

        request.forEachPart { part ->
            if (part is PartData.FileItem && part.name == "files") {
                files += UploadedFileDto(
                    name = part.originalFileName ?: "unknown",
                    size = part.provider().readRemaining().readByteArray().size.toLong(),
                    description = null,
                )
            }
            part.release()
        }

        return UploadFilesResponse.ok(files)
    }
}

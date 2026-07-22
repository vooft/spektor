package io.github.vooft.spektor.model

enum class SpektorContentType(val mediaType: String? = null) {
    JSON("application/json"),
    TEXT_PLAIN("text/plain"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    BINARY,
}

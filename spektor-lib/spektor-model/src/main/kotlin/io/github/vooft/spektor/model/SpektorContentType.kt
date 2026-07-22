package io.github.vooft.spektor.model

enum class SpektorContentType(val mediaType: String? = null) {
    JSON("application/json"),
    TEXT_PLAIN("text/plain"),
    MULTIPART_FORM_DATA("multipart/form-data"),

    /**
     * Any media type with a binary schema (`format: binary` or `contentMediaType`),
     * e.g. image/png or application/pdf, so there is no single [mediaType].
     */
    BINARY,
}

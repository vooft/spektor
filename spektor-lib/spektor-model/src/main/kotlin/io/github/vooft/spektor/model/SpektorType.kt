package io.github.vooft.spektor.model

import java.nio.file.Path

sealed interface SpektorType {
    data class MicroType(val type: OpenApiMicroType, val format: String?) : SpektorType {
        enum class OpenApiMicroType(val typeName: String) {
            STRING("string"),
            INTEGER("integer"),
            BOOLEAN("boolean"),
            NUMBER("number");

            companion object {
                fun from(typeName: String): OpenApiMicroType =
                    entries.find { it.typeName == typeName } ?: error("Unsupported OpenAPI micro type: $typeName")
            }
        }
    }

    data class List(val itemType: SpektorType) : SpektorType

    data class Object(val properties: Map<String, RequiredWrapper<SpektorType>>) : SpektorType

    data class Ref(val file: Path, val modelName: String) : SpektorType

    data class RequiredWrapper<T : SpektorType>(val type: T, val required: Boolean)
}

package io.github.vooft.spektor.model

import java.nio.file.Path

sealed interface SpektorType {
    data class MicroType(val type: String, val format: String?) : SpektorType

    data class List(val itemType: SpektorType) : SpektorType

    data class Object(val properties: Map<String, RequiredWrapper<SpektorType>>) : SpektorType

    data class Ref(val file: Path, val modelName: String) : SpektorType

    data class RequiredWrapper<T : SpektorType>(val type: T, val required: Boolean)
}

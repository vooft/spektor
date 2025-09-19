package io.github.vooft.spektor.model

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

sealed interface SpektorType {

    sealed interface QueryVariableType : SpektorType
    sealed interface PathVariableType : SpektorType

    val isContextual: Boolean get() = false

    data class Array(val itemType: SpektorType) : QueryVariableType

    data class Object(val properties: Map<String, RequiredWrapper<SpektorType>>) : SpektorType

    data class Ref(val file: Path, val modelName: String) : SpektorType

    data class Enum(val values: List<String>) : SpektorType

    data class RequiredWrapper<T : SpektorType>(val type: T, val required: Boolean)

    sealed interface MicroType : QueryVariableType, PathVariableType {
        data class StringMicroType(val format: StringFormat) : MicroType {
            override val isContextual
                get() = when (format) {
                    StringFormat.PLAIN -> false
                    StringFormat.UUID,
                    StringFormat.URI,
                    StringFormat.DATE_TIME,
                    StringFormat.DATE -> true
                }
        }

        data class IntegerMicroType(val format: String?) : MicroType
        data object BooleanMicroType : MicroType
        data class NumberMicroType(val format: NumberFormat) : MicroType

        enum class NumberFormat(val formatName: String) {
            FLOAT("float"),
            DOUBLE("double");

            companion object {
                fun from(formatName: String?): NumberFormat = entries.find { it.formatName == formatName } ?: run {
                    logger.warn { "Unsupported NUMBER type format $formatName, falling back to double" }
                    DOUBLE
                }
            }
        }

        enum class StringFormat(val formatName: String?) {
            PLAIN(null),
            UUID("uuid"),
            URI("uri"),
            DATE_TIME("date-time"),
            DATE("date");

            companion object {
                fun from(formatName: String?): StringFormat = entries.find { it.formatName == formatName } ?: run {
                    logger.warn { "Unsupported STRING type format $formatName, falling back to plain string" }
                    PLAIN
                }
            }
        }

        companion object {
            fun from(typeName: String, format: String?): MicroType = when (typeName) {
                "string" -> StringMicroType(StringFormat.from(format))
                "integer" -> IntegerMicroType(format)
                "boolean" -> BooleanMicroType
                "number" -> NumberMicroType(NumberFormat.from(format))
                else -> error("Unsupported OpenAPI micro type: $typeName")
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}

package io.github.vooft.spektor.model

import java.nio.file.Path

sealed interface SpektorType {

    sealed interface QueryVariableType : SpektorType
    sealed interface PathVariableType : SpektorType

    val isContextual: Boolean get() = false

    data class Array(val itemType: SpektorType) : QueryVariableType

    sealed interface Object : SpektorType {
        data class WithProperties(val properties: Map<String, RequiredWrapper<SpektorType>>) : Object
        data class AdditionalProperties(val valueType: SpektorType) : Object
        object FreeForm : Object
    }

    data class Ref(val file: Path, val modelName: String) : QueryVariableType, PathVariableType

    data class Enum(val values: List<String>) : SpektorType

    data class OneOf(
        val discriminatorPropertyName: String,
        val variants: Map<String, Ref>,
    ) : SpektorType {
        data class ResolvedVariant(
            val ref: Ref,
            val objectType: Object.WithProperties,
            val discriminatorValue: String,
        )
    }

    data class RequiredWrapper<T : SpektorType>(val type: T, val required: Boolean)

    sealed interface MicroType : QueryVariableType, PathVariableType {
        data class StringMicroType(val format: StringFormat) : MicroType {
            override val isContextual
                get() = when (format) {
                    StringFormat.PLAIN -> false
                    StringFormat.UUID,
                    StringFormat.URI,
                    StringFormat.YEAR_MONTH,
                    StringFormat.DATE_TIME,
                    StringFormat.DATE -> true
                }
        }

        data class IntegerMicroType(val format: IntegerFormat) : MicroType
        data object BooleanMicroType : MicroType
        data class NumberMicroType(val format: NumberFormat) : MicroType {
            override val isContextual get() = format.isContextual
        }

        enum class IntegerFormat(val formatName: String) {
            INT32("int32"),
            INT64("int64");

            companion object {
                fun from(formatName: String?): IntegerFormat {
                    if (formatName == null) {
                        return INT32
                    }
                    return IntegerFormat.entries.find { it.formatName == formatName }
                        ?: error("Unsupported integer format: $formatName")
                }
            }
        }

        enum class NumberFormat(val formatName: String, val isContextual: Boolean) {
            BIG_DECIMAL("decimal", true),
            FLOAT("float", false),
            DOUBLE("double", false);

            companion object {
                fun from(formatName: String?): NumberFormat {
                    if (formatName == null) {
                        return BIG_DECIMAL
                    }
                    return entries.find { it.formatName == formatName }
                        ?: error("Unsupported number format: $formatName")
                }
            }
        }

        enum class StringFormat(val formatName: String) {
            PLAIN("plain"),
            UUID("uuid"),
            URI("uri"),
            DATE_TIME("date-time"),
            YEAR_MONTH("year-month"),
            DATE("date");

            companion object {
                fun from(formatName: String?): StringFormat {
                    if (formatName == null) {
                        return PLAIN
                    }
                    return entries.find { it.formatName == formatName }
                        ?: error("Unsupported string format: $formatName")
                }
            }
        }

        companion object {
            fun from(typeName: String, format: String?): MicroType = when (typeName) {
                "boolean" -> BooleanMicroType
                "string" -> StringMicroType(StringFormat.from(format))
                "integer" -> IntegerMicroType(IntegerFormat.from(format))
                "number" -> NumberMicroType(NumberFormat.from(format))
                else -> error("Unsupported OpenAPI micro type: $typeName")
            }
        }
    }
}

package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import io.github.vooft.spektor.codegen.SpektorCodegenContext
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorType
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class SpektorTypeCodegen(
    private val config: SpektorCodegenConfig,
    private val context: SpektorCodegenContext
) {

    private val classCodegen = SpektorTypeDtoCodegen(config, this)

    fun generate(type: SpektorType): TypeName {
        val resolvedType = context.resolvedTypes[type]
        if (resolvedType != null) {
            return resolvedType
        }

        return when (type) {
            is SpektorType.Array -> LIST.plusParameter(generate(type.itemType))
            is SpektorType.MicroType -> {
                type.toTypeName().let {
                    if (type.isContextual) {
                        it.copy(
                            annotations = it.annotations + AnnotationSpec.builder(CONTEXTUAL_ANNOTATION).build()
                        )
                    } else {
                        it
                    }
                }
            }

            is SpektorType.Object.FreeForm -> JSON_OBJECT_CLASS
            is SpektorType.Object.AdditionalProperties -> {
                validatePropertyNamesKeyType(type.keyType)
                val keyTypeName = generate(type.keyType)
                MAP.plusParameter(keyTypeName).plusParameter(generate(type.valueType))
            }

            is SpektorType.Object.WithProperties -> error("Generating object directly is not supported $type")
            is SpektorType.Enum -> error("Generating enum directly is not supported $type")
            is SpektorType.OneOf -> error("Generating oneOf directly is not supported $type")
            is SpektorType.Ref -> generateRef(type)
        }.also { context.resolvedTypes[type] = it }
    }

    private fun SpektorType.Ref.traceRefs() = buildList {
        var current: SpektorType = this@traceRefs
        while (current is SpektorType.Ref) {
            add(current)
            current = context.refs[current] ?: error("Unable to resolve ref $current")
        }
    }

    private fun validatePropertyNamesKeyType(keyType: SpektorType) {
        require(keyType is SpektorType.Ref || keyType is SpektorType.MicroType.StringMicroType) {
            "propertyNames key must be a string-based type or a \$ref to one, but got $keyType"
        }
        if (keyType is SpektorType.Ref) {
            val target = context.refs.getValue(keyType.traceRefs().last())
            require(target is SpektorType.MicroType.StringMicroType || target is SpektorType.Enum) {
                "propertyNames key \$ref $keyType must resolve to a string-based type, but resolves to $target"
            }
        }
    }

    private fun generateRef(ref: SpektorType.Ref): TypeName {
        val refsTrace = ref.traceRefs()
        val lastRef = refsTrace.last()

        val target = context.refs.getValue(lastRef)
        if (target is SpektorType.Object.AdditionalProperties) {
            validatePropertyNamesKeyType(target.keyType)
            val keyTypeName = generate(target.keyType)
            val mapType = MAP.plusParameter(keyTypeName).plusParameter(generate(target.valueType))
            refsTrace.forEach { context.resolvedTypes[it] = mapType }
            return mapType
        }

        val typeSpec = when (target) {
            is SpektorType.Object.WithProperties -> classCodegen.generate(lastRef, target)
            is SpektorType.Enum -> classCodegen.generate(lastRef, target)
            is SpektorType.OneOf -> {
                val resolvedVariants = target.variants.map { (discriminatorValue, variantRef) ->
                    val variantTarget = context.refs[variantRef]
                        ?: error("Cannot resolve variant ref $variantRef for oneOf $lastRef")
                    require(variantTarget is SpektorType.Object.WithProperties) {
                        "oneOf variant must be an object with properties, but got $variantTarget for $variantRef"
                    }
                    SpektorType.OneOf.ResolvedVariant(variantRef, variantTarget, discriminatorValue)
                }
                classCodegen.generate(
                    ref = lastRef,
                    oneOfType = target,
                    resolvedVariants = resolvedVariants,
                )
            }

            is SpektorType.Array,
            is SpektorType.MicroType,
            is SpektorType.Object.AdditionalProperties,
            is SpektorType.Object.FreeForm,
            is SpektorType.Ref -> error(
                "Only object with properties, enum, and oneOf references are supported, but got $target for ref $lastRef"
            )
        }

        refsTrace.forEach {
            context.generatedTypeSpecs[it] = TypeAndClass(type = typeSpec, className = config.classNameFor(it))
        }

        return config.classNameFor(lastRef)
    }

    private fun SpektorType.MicroType.toTypeName(): TypeName = when (this) {
        is SpektorType.MicroType.BooleanMicroType -> BOOLEAN
        is SpektorType.MicroType.IntegerMicroType -> when (format) {
            SpektorType.MicroType.IntegerFormat.INT32 -> INT
            SpektorType.MicroType.IntegerFormat.INT64 -> LONG
        }

        is SpektorType.MicroType.NumberMicroType -> when (format) {
            SpektorType.MicroType.NumberFormat.FLOAT -> FLOAT
            SpektorType.MicroType.NumberFormat.DOUBLE -> DOUBLE
            SpektorType.MicroType.NumberFormat.BIG_DECIMAL -> BigDecimal::class.asClassName()
        }

        is SpektorType.MicroType.StringMicroType -> when (format) {
            SpektorType.MicroType.StringFormat.PLAIN -> STRING
            SpektorType.MicroType.StringFormat.UUID -> UUID::class.asClassName()
            SpektorType.MicroType.StringFormat.URI -> URI::class.asClassName()
            SpektorType.MicroType.StringFormat.YEAR_MONTH -> YearMonth::class.asClassName()
            SpektorType.MicroType.StringFormat.DATE_TIME -> Instant::class.asClassName()
            SpektorType.MicroType.StringFormat.DATE -> LocalDate::class.asClassName()
        }
    }

    companion object {
        private val JSON_OBJECT_CLASS = ClassName("kotlinx.serialization.json", "JsonObject")
        private val CONTEXTUAL_ANNOTATION = ClassName("kotlinx.serialization", "Contextual")
    }
}

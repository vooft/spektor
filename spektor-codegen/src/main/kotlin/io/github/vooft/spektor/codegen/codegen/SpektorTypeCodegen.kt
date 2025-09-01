package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.github.vooft.spektor.codegen.SpektorCodegenContext
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.SpektorType.MicroType.OpenApiMicroType

class SpektorTypeCodegen(
    private val config: SpektorCodegenConfig,
    private val context: SpektorCodegenContext
) {

    private val classCodegen = SpektorTypeObjectCodegen(config, this)

    fun generate(type: SpektorType): TypeName = when (type) {
        is SpektorType.List -> LIST.plusParameter(generate(type.itemType))
        is SpektorType.MicroType -> type.toTypeName()
        is SpektorType.Object -> error("Generating object directly is not supported $type")
        is SpektorType.Ref -> generateRef(type)
    }.also { context.resolvedTypes[type] = it }

    private fun SpektorType.Ref.traceRefs() = buildList {
        var current: SpektorType = this@traceRefs
        while (current is SpektorType.Ref) {
            add(current)
            current = context.refs.getValue(current)
        }
    }

    private fun generateRef(ref: SpektorType.Ref): TypeName {
        val refsTrace = ref.traceRefs()
        val lastRef = refsTrace.last()

        val target = context.refs.getValue(lastRef)
        val targetObject = target as? SpektorType.Object
            ?: error("Only object references are supported, but got $target for ref $lastRef")

        val typeSpec = classCodegen.generate(lastRef, targetObject)
        refsTrace.forEach {
            context.generatedTypeSpecs[it] = TypeAndClass(type = typeSpec, className = config.classNameFor(it))
        }

        return config.classNameFor(lastRef)
    }

    private fun SpektorType.MicroType.toTypeName(): TypeName = when (type) {
        OpenApiMicroType.STRING -> STRING
        OpenApiMicroType.INTEGER -> INT
        OpenApiMicroType.BOOLEAN -> BOOLEAN
        OpenApiMicroType.NUMBER -> DOUBLE
    }
}

package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.model.SpektorType

class SpektorTypeDtoCodegen(
    private val config: SpektorCodegenConfig,
    private val typeCodegen: SpektorTypeCodegen
) {

    fun generate(ref: SpektorType.Ref, objectType: SpektorType.Object): TypeSpec {
        val fields = objectType.properties.map { (name, wrapper) ->
            FieldInfo(
                name = name,
                typeName = typeCodegen.generate(wrapper.type),
                required = wrapper.required
            )
        }

        return TypeSpec.classBuilder(config.classNameFor(ref))
            .addAnnotation(SERIALIZABLE_ANNOTATION)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(fields)
                    .build()
            )
            .addProperties(fields)
            .build()
    }

    private fun FunSpec.Builder.addParameters(fields: List<FieldInfo>): FunSpec.Builder {
        for (field in fields) {
            val builder = ParameterSpec.builder(field.name, field.typeName.copy(nullable = !field.required))
            if (!field.required) {
                builder.defaultValue("null")
            }

            addParameter(builder.build())
        }

        return this
    }

    private fun TypeSpec.Builder.addProperties(fields: List<FieldInfo>): TypeSpec.Builder {
        for (field in fields) {
            addProperty(
                PropertySpec.builder(field.name, field.typeName.copy(nullable = !field.required))
                    .initializer(field.name)
                    .build()
            )
        }

        return this
    }

    companion object {
        private val SERIALIZABLE_ANNOTATION = ClassName("kotlinx.serialization", "Serializable")
    }
}

private data class FieldInfo(val name: String, val typeName: TypeName, val required: Boolean)

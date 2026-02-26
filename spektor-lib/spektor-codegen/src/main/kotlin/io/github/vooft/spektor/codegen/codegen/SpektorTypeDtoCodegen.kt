package io.github.vooft.spektor.codegen.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.SpektorPropertyRef
import io.github.vooft.spektor.model.SpektorType

class SpektorTypeDtoCodegen(
    private val config: SpektorCodegenConfig,
    private val typeCodegen: SpektorTypeCodegen,
) {

    fun generate(ref: SpektorType.Ref, objectType: SpektorType.Object.WithProperties): TypeSpec {
        val fields = objectType.properties.map { (name, wrapper) ->
            val substituted = config.microtypeSubstitutions[SpektorPropertyRef(ref, name)]?.let { ClassName.bestGuess(it) }

            FieldInfo(
                name = name,
                typeName = substituted ?: typeCodegen.generate(wrapper.type),
                required = wrapper.required,
                contextual = wrapper.type.isContextual
            )
        }

        return TypeSpec.classBuilder(config.classNameFor(ref))
            .addModifiers(KModifier.DATA)
            .addAnnotation(SERIALIZABLE_ANNOTATION)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(fields)
                    .build()
            )
            .addProperties(fields)
            .build()
    }

    fun generate(ref: SpektorType.Ref, enumType: SpektorType.Enum): TypeSpec = TypeSpec.enumBuilder(config.classNameFor(ref))
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("value", String::class)
                .build()
        )
        .addAnnotation(SERIALIZABLE_ANNOTATION)
        .also {
            for (value in enumType.values) {
                it.addEnumConstant(
                    name = value.uppercase().replace(ENUM_NAME_REGEX, "_"),
                    typeSpec = TypeSpec.anonymousClassBuilder()
                        .addSuperclassConstructorParameter("%S", value)
                        .addAnnotation(
                            AnnotationSpec.builder(SERIAL_NAME_ANNOTATION)
                                .addMember("%S", value)
                                .build()
                        )
                        .build()
                )
            }
        }
        .build()

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
        private val SERIAL_NAME_ANNOTATION = ClassName("kotlinx.serialization", "SerialName")
        private val ENUM_NAME_REGEX = Regex("[^A-Za-z0-9_]")
    }
}

private data class FieldInfo(val name: String, val typeName: TypeName, val required: Boolean, val contextual: Boolean)

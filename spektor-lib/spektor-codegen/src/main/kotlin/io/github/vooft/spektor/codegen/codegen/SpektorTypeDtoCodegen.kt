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

    fun generate(ref: SpektorType.Ref, oneOfType: SpektorType.OneOf, resolvedVariants: List<SpektorType.OneOf.ResolvedVariant>,): TypeSpec {
        val sealedClassName = config.classNameFor(ref)

        val builder = TypeSpec.interfaceBuilder(sealedClassName)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(SERIALIZABLE_ANNOTATION)
            .addAnnotation(
                AnnotationSpec.builder(OPT_IN_ANNOTATION)
                    .addMember("%T::class", EXPERIMENTAL_SERIALIZATION_API_ANNOTATION)
                    .build()
            )
            .addAnnotation(
                AnnotationSpec.builder(JSON_CLASS_DISCRIMINATOR_ANNOTATION)
                    .addMember("%S", oneOfType.discriminatorPropertyName)
                    .build()
            )

        for (variant in resolvedVariants) {
            builder.addType(generateVariantType(variant, sealedClassName, oneOfType.discriminatorPropertyName))
        }

        return builder.build()
    }

    private fun generateVariantType(
        variant: SpektorType.OneOf.ResolvedVariant,
        sealedParent: ClassName,
        discriminatorPropertyName: String,
    ): TypeSpec {
        val fields = variant.objectType.properties
            .filter { (name, _) -> name != discriminatorPropertyName }
            .map { (name, wrapper) ->
                val substituted = config.microtypeSubstitutions[SpektorPropertyRef(variant.ref, name)]?.let { ClassName.bestGuess(it) }
                FieldInfo(
                    name = name,
                    typeName = substituted ?: typeCodegen.generate(wrapper.type),
                    required = wrapper.required,
                    contextual = wrapper.type.isContextual,
                )
            }

        val variantName = config.classNameFor(variant.ref).simpleName
        val serialNameAnnotation = AnnotationSpec.builder(SERIAL_NAME_ANNOTATION)
            .addMember("%S", variant.discriminatorValue)
            .build()

        if (fields.isEmpty()) {
            return TypeSpec.objectBuilder(variantName)
                .addAnnotation(SERIALIZABLE_ANNOTATION)
                .addAnnotation(serialNameAnnotation)
                .addSuperinterface(sealedParent)
                .build()
        }

        return TypeSpec.classBuilder(variantName)
            .addModifiers(KModifier.DATA)
            .addAnnotation(SERIALIZABLE_ANNOTATION)
            .addAnnotation(serialNameAnnotation)
            .addSuperinterface(sealedParent)
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

    private data class FieldInfo(
        val name: String,
        val typeName: TypeName,
        val required: Boolean,
        val contextual: Boolean,
    )

    companion object {
        private val SERIALIZABLE_ANNOTATION = ClassName("kotlinx.serialization", "Serializable")
        private val SERIAL_NAME_ANNOTATION = ClassName("kotlinx.serialization", "SerialName")
        private val JSON_CLASS_DISCRIMINATOR_ANNOTATION = ClassName("kotlinx.serialization.json", "JsonClassDiscriminator")
        private val OPT_IN_ANNOTATION = ClassName("kotlin", "OptIn")
        private val EXPERIMENTAL_SERIALIZATION_API_ANNOTATION = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
        private val ENUM_NAME_REGEX = Regex("[^A-Za-z0-9_]")
    }
}

package io.github.vooft.spektor.codegen

import com.squareup.kotlinpoet.KModifier
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles.listOwnerFile
import io.github.vooft.spektor.test.TestFiles.rootPath
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

class SpektorCodegenOneOfTest {

    private val parser = SpektorParser()
    private val config = SpektorCodegenConfig(
        basePackage = "io.github.vooft.spektor.example",
        specRoot = rootPath,
    )

    private val codegen = SpektorCodegen(config)

    @Test
    fun `should generate sealed interface for oneOf`() {
        val schema = parser.parse(listOf(listOwnerFile))
        val context = codegen.generate(schema)

        val ownerType = context.generatedTypeSpecs.values
            .single { it.className.simpleName == "OwnerDto" }
            .type

        ownerType.modifiers shouldContain KModifier.SEALED
    }

    @Test
    fun `should generate nested variant data classes`() {
        val schema = parser.parse(listOf(listOwnerFile))
        val context = codegen.generate(schema)

        val ownerType = context.generatedTypeSpecs.values
            .single { it.className.simpleName == "OwnerDto" }
            .type

        val individual = ownerType.typeSpecs.find { it.name == "IndividualDto" }
        individual.shouldNotBeNull()
        individual.modifiers shouldContain KModifier.DATA

        val business = ownerType.typeSpecs.find { it.name == "BusinessDto" }
        business.shouldNotBeNull()
        business.modifiers shouldContain KModifier.DATA
    }

    @Test
    fun `should not include discriminator property in sealed interface or variants`() {
        val schema = parser.parse(listOf(listOwnerFile))
        val context = codegen.generate(schema)

        val ownerType = context.generatedTypeSpecs.values
            .single { it.className.simpleName == "OwnerDto" }
            .type

        // Discriminator is handled by @JsonClassDiscriminator, not as a property
        ownerType.propertySpecs.map { it.name } shouldNotContain "type"

        val individual = ownerType.typeSpecs.single { it.name == "IndividualDto" }
        individual.propertySpecs.map { it.name } shouldNotContain "type"

        val business = ownerType.typeSpecs.single { it.name == "BusinessDto" }
        business.propertySpecs.map { it.name } shouldNotContain "type"
    }

    @Test
    fun `should have Serializable and JsonClassDiscriminator annotations`() {
        val schema = parser.parse(listOf(listOwnerFile))
        val context = codegen.generate(schema)

        val ownerType = context.generatedTypeSpecs.values
            .single { it.className.simpleName == "OwnerDto" }
            .type

        val annotationNames = ownerType.annotations.map { it.typeName.toString() }
        annotationNames shouldContain "kotlinx.serialization.Serializable"
        annotationNames shouldContain "kotlinx.serialization.json.JsonClassDiscriminator"
    }

    @Test
    fun `should have SerialName annotation on variant classes`() {
        val schema = parser.parse(listOf(listOwnerFile))
        val context = codegen.generate(schema)

        val ownerType = context.generatedTypeSpecs.values
            .single { it.className.simpleName == "OwnerDto" }
            .type

        val individual = ownerType.typeSpecs.single { it.name == "IndividualDto" }
        val individualAnnotations = individual.annotations.map { it.typeName.toString() }
        individualAnnotations shouldContain "kotlinx.serialization.SerialName"

        val business = ownerType.typeSpecs.single { it.name == "BusinessDto" }
        val businessAnnotations = business.annotations.map { it.typeName.toString() }
        businessAnnotations shouldContain "kotlinx.serialization.SerialName"
    }
}

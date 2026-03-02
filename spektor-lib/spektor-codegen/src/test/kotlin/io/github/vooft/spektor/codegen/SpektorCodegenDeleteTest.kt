package io.github.vooft.spektor.codegen

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles.deleteBookFile
import io.github.vooft.spektor.test.TestFiles.rootPath
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorCodegenDeleteTest {

    private val parser = SpektorParser()
    private val config = SpektorCodegenConfig(
        basePackage = "io.github.vooft.spektor.example",
        specRoot = rootPath,
    )

    private val codegen = SpektorCodegen(config)

    @Test
    fun `should generate sealed interface for 204 response`() {
        val schema = parser.parse(listOf(deleteBookFile))
        val context = codegen.generate(schema)

        val serverApiType = context.generatedPathSpecs.values
            .single { it.className.simpleName == "BookServerApi" }
            .type

        val deleteResponse = serverApiType.typeSpecs.find { it.name == "DeleteResponse" }
        deleteResponse.shouldNotBeNull()
        deleteResponse.modifiers shouldContain KModifier.SEALED
    }

    @Test
    fun `should generate object for 204 no-body response`() {
        val schema = parser.parse(listOf(deleteBookFile))
        val context = codegen.generate(schema)

        val serverApiType = context.generatedPathSpecs.values
            .single { it.className.simpleName == "BookServerApi" }
            .type

        val deleteResponse = serverApiType.typeSpecs.single { it.name == "DeleteResponse" }
        val noContent = deleteResponse.typeSpecs.find { it.name == "NoContent" }
        noContent.shouldNotBeNull()
        noContent.kind shouldBe TypeSpec.Kind.OBJECT
    }

    @Test
    fun `should generate NoContent with null body and 204 statusCode`() {
        val schema = parser.parse(listOf(deleteBookFile))
        val context = codegen.generate(schema)

        val serverApiType = context.generatedPathSpecs.values
            .single { it.className.simpleName == "BookServerApi" }
            .type

        val noContent = serverApiType.typeSpecs
            .single { it.name == "DeleteResponse" }
            .typeSpecs
            .single { it.name == "NoContent" }

        val bodyProp = noContent.propertySpecs.single { it.name == "body" }
        bodyProp.initializer.toString() shouldBe "null"

        val statusCodeProp = noContent.propertySpecs.single { it.name == "statusCode" }
        statusCodeProp.initializer.toString() shouldBe "io.ktor.http.HttpStatusCode.fromValue(204)"
    }
}

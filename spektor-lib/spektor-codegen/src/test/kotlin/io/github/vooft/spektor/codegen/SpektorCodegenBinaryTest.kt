package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles.imageApiFile
import io.github.vooft.spektor.test.TestFiles.rootPath
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

class SpektorCodegenBinaryTest {

    private val parser = SpektorParser()
    private val config = SpektorCodegenConfig(
        basePackage = "io.github.vooft.spektor.example",
        specRoot = rootPath,
    )

    private val codegen = SpektorCodegen(config)

    @Test
    fun `should generate ByteArray request parameter for binary request body`() {
        val schema = parser.parse(listOf(imageApiFile))
        val context = codegen.generate(schema)

        val serverApiType = context.generatedPathSpecs.values
            .single { it.className.simpleName == "ImageServerApi" }
            .type

        for (operationId in listOf("uploadImage", "uploadImageRaw")) {
            val parameter = serverApiType.funSpecs
                .single { it.name == operationId }
                .parameters
                .single { it.name == "request" }
            parameter.type.toString() shouldBe "kotlin.ByteArray"
        }
    }

    @Test
    fun `should receive ByteArray for binary request body`() {
        val schema = parser.parse(listOf(imageApiFile))
        val context = codegen.generate(schema)

        val routesType = context.generatedRouteSpecs.values
            .single { it.className.simpleName == "ImageRoutes" }
            .type

        val code = routesType.funSpecs.single { it.name == "uploadImage" }.body.toString()
        code shouldContain "val request = call.receive<kotlin.ByteArray>()"
        code shouldNotContain "receiveMultipart"
        code shouldNotContain "receiveText"
    }
}

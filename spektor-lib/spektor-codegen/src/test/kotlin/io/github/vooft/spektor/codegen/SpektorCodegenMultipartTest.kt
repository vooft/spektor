package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles.multipartApiFile
import io.github.vooft.spektor.test.TestFiles.rootPath
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

class SpektorCodegenMultipartTest {

    private val parser = SpektorParser()
    private val config = SpektorCodegenConfig(
        basePackage = "io.github.vooft.spektor.example",
        specRoot = rootPath,
    )

    private val codegen = SpektorCodegen(config)

    @Test
    fun `should generate MultiPartData request parameter for multipart request body`() {
        val schema = parser.parse(listOf(multipartApiFile))
        val context = codegen.generate(schema)

        val serverApiType = context.generatedPathSpecs.values
            .single { it.className.simpleName == "MultipartServerApi" }
            .type

        val requiredParameter = serverApiType.funSpecs
            .single { it.name == "uploadFile" }
            .parameters
            .single { it.name == "request" }
        requiredParameter.type.toString() shouldBe "io.ktor.http.content.MultiPartData"

        val coercedParameter = serverApiType.funSpecs
            .single { it.name == "uploadFileOptional" }
            .parameters
            .single { it.name == "request" }
        coercedParameter.type.toString() shouldBe "io.ktor.http.content.MultiPartData"
    }

    @Test
    fun `should receive multipart for multipart request body`() {
        val schema = parser.parse(listOf(multipartApiFile))
        val context = codegen.generate(schema)

        val routesType = context.generatedRouteSpecs.values
            .single { it.className.simpleName == "MultipartRoutes" }
            .type

        val code = routesType.funSpecs.single { it.name == "uploadFile" }.body.toString()
        code shouldContain "val request = call.receiveMultipart()"
        code shouldNotContain "call.receive<"
        code shouldNotContain "receiveText"
    }

    @Test
    fun `should receive multipart for coerced optional multipart request body`() {
        val schema = parser.parse(listOf(multipartApiFile))
        val context = codegen.generate(schema)

        val routesType = context.generatedRouteSpecs.values
            .single { it.className.simpleName == "MultipartRoutes" }
            .type

        val code = routesType.funSpecs.single { it.name == "uploadFileOptional" }.body.toString()
        code shouldContain "val request = call.receiveMultipart()"
        code shouldNotContain "receiveNullable"
    }

    @Test
    fun `should not generate dto for multipart schema`() {
        val schema = parser.parse(listOf(multipartApiFile))
        val context = codegen.generate(schema)

        context.generatedTypeSpecs.values.map { it.className.simpleName } shouldContainExactly listOf("UploadedFileDto")
    }

    @Test
    fun `should import receiveMultipart in routes`() {
        val schema = parser.parse(listOf(multipartApiFile))
        val context = codegen.generate(schema)

        val routes = context.generatedRouteSpecs.values.single { it.className.simpleName == "MultipartRoutes" }
        routes.imports shouldContain TypeAndClass.Import("io.ktor.server.request", "receiveMultipart")
    }
}

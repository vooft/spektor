package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles.echoApiFile
import io.github.vooft.spektor.test.TestFiles.pingApiFile
import io.github.vooft.spektor.test.TestFiles.rootPath
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

class SpektorCodegenTextPlainTest {

    private val parser = SpektorParser()
    private val config = SpektorCodegenConfig(
        basePackage = "io.github.vooft.spektor.example",
        specRoot = rootPath,
    )

    private val codegen = SpektorCodegen(config)

    @Test
    fun `should generate String body for text plain response`() {
        val schema = parser.parse(listOf(pingApiFile))
        val context = codegen.generate(schema)

        val serverApiType = context.generatedPathSpecs.values
            .single { it.className.simpleName == "PingServerApi" }
            .type

        val okResponse = serverApiType.typeSpecs
            .single { it.name == "PingResponse" }
            .typeSpecs
            .single { it.name == "Ok" }

        val bodyProperty = okResponse.propertySpecs.single { it.name == "body" }
        bodyProperty.type.toString() shouldBe "kotlin.String"
    }

    @Test
    fun `should respond with text for text plain response`() {
        val schema = parser.parse(listOf(pingApiFile))
        val context = codegen.generate(schema)

        val routesType = context.generatedRouteSpecs.values
            .single { it.className.simpleName == "PingRoutes" }
            .type

        val pingFun = routesType.funSpecs.find { it.name == "ping" }
        pingFun.shouldNotBeNull()

        val code = pingFun.body.toString()
        code shouldContain "call.respondText(text = response.body, " +
            "contentType = io.ktor.http.ContentType.Text.Plain, status = response.statusCode)"
        code shouldNotContain "call.respond(response.statusCode, response.body)"
    }

    @Test
    fun `should generate String request parameter for text plain request body`() {
        val schema = parser.parse(listOf(echoApiFile))
        val context = codegen.generate(schema)

        val serverApiType = context.generatedPathSpecs.values
            .single { it.className.simpleName == "EchoServerApi" }
            .type

        val requiredParameter = serverApiType.funSpecs
            .single { it.name == "echo" }
            .parameters
            .single { it.name == "request" }
        requiredParameter.type.toString() shouldBe "kotlin.String"

        val optionalParameter = serverApiType.funSpecs
            .single { it.name == "echoOptional" }
            .parameters
            .single { it.name == "request" }
        optionalParameter.type.toString() shouldBe "kotlin.String?"
    }

    @Test
    fun `should receive text for required text plain request body`() {
        val schema = parser.parse(listOf(echoApiFile))
        val context = codegen.generate(schema)

        val routesType = context.generatedRouteSpecs.values
            .single { it.className.simpleName == "EchoRoutes" }
            .type

        val code = routesType.funSpecs.single { it.name == "echo" }.body.toString()
        code shouldContain "val request = call.receiveText()"
        code shouldNotContain "call.receive<"
    }

    @Test
    fun `should receive text for optional text plain request body`() {
        val schema = parser.parse(listOf(echoApiFile))
        val context = codegen.generate(schema)

        val routesType = context.generatedRouteSpecs.values
            .single { it.className.simpleName == "EchoRoutes" }
            .type

        val code = routesType.funSpecs.single { it.name == "echoOptional" }.body.toString()
        code shouldContain "call.receiveText()"
        code shouldContain "null"
        code shouldNotContain "call.receiveNullable<"
    }
}

package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.codegen.SpektorTypeCodegen
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles.plainPricesApiFile
import io.github.vooft.spektor.test.TestFiles.rootPath
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SpektorCodegenAdditionalPropertiesTest {

    private val parser = SpektorParser()
    private val config = SpektorCodegenConfig(
        basePackage = "io.github.vooft.spektor.example",
        specRoot = rootPath,
    )

    @Test
    fun `should generate Map with String key for additionalProperties without propertyNames`() {
        val schema = parser.parse(listOf(plainPricesApiFile))
        val context = SpektorCodegenContext(schema.paths, schema.refs)
        val typeCodegen = SpektorTypeCodegen(config, context)

        val plainPricesRef = schema.refs.keys.single { it.modelName == "PlainPrices" }
        val generated = typeCodegen.generate(plainPricesRef)

        generated.toString() shouldBe "kotlin.collections.Map<kotlin.String, io.github.vooft.spektor.example.models.money.MoneyDto>"
    }
}

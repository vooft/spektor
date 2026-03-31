package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.test.TestFiles.listOwnerFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class SpektorParserOneOfTest {

    private val parser = SpektorParser()

    @Test
    fun `should parse oneOf with discriminator`() {
        val schema = parser.parse(listOf(listOwnerFile))

        val ownerRef = schema.refs.keys.single { it.modelName == "Owner" }
        val ownerType = schema.refs[ownerRef]
        ownerType.shouldBeInstanceOf<SpektorType.OneOf>()
        ownerType.discriminatorPropertyName shouldBe "type"
        ownerType.variants.keys shouldBe setOf("INDIVIDUAL", "BUSINESS")
    }

    @Test
    fun `should resolve oneOf variant refs as objects`() {
        val schema = parser.parse(listOf(listOwnerFile))

        val individualRef = schema.refs.keys.single { it.modelName == "Individual" }
        val individualType = schema.refs[individualRef]
        individualType.shouldBeInstanceOf<SpektorType.Object.WithProperties>()
        individualType.properties.keys shouldBe setOf("type", "id", "firstName", "lastName")

        val businessRef = schema.refs.keys.single { it.modelName == "Business" }
        val businessType = schema.refs[businessRef]
        businessType.shouldBeInstanceOf<SpektorType.Object.WithProperties>()
        businessType.properties.keys shouldBe setOf("type", "id", "name")
    }

    @Test
    fun `should resolve OwnerType enum`() {
        val schema = parser.parse(listOf(listOwnerFile))

        val ownerTypeRef = schema.refs.keys.single { it.modelName == "OwnerType" }
        val ownerTypeType = schema.refs[ownerTypeRef]
        ownerTypeType.shouldBeInstanceOf<SpektorType.Enum>()
        ownerTypeType.values shouldBe listOf("BUSINESS", "INDIVIDUAL")
    }
}

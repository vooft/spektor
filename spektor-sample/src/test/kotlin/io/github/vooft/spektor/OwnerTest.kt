package io.github.vooft.spektor

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test

class OwnerTest {

    @Test
    fun `should list owners with discriminator`() = testClient("admin") { client ->
        val response = client.get("/owner")
        response.status shouldBe HttpStatusCode.OK

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val owners = body["owners"].shouldNotBeNull().jsonArray
        owners shouldHaveSize 2

        val individual = owners.first { it.jsonObject["type"].shouldNotBeNull().jsonPrimitive.content == "INDIVIDUAL" }.jsonObject
        individual["firstName"].shouldNotBeNull().jsonPrimitive.content shouldBe "John"
        individual["lastName"].shouldNotBeNull().jsonPrimitive.content shouldBe "Doe"

        val business = owners.first { it.jsonObject["type"].shouldNotBeNull().jsonPrimitive.content == "BUSINESS" }.jsonObject
        business["name"].shouldNotBeNull().jsonPrimitive.content shouldBe "Acme Corp"
    }
}

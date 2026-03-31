package io.github.vooft.spektor

import io.github.vooft.spektor.test.apis.OwnerTestApi
import io.github.vooft.spektor.test.infrastructure.ApiClient
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test

class OwnerTest {

    @Test
    fun `should list owners`() = testClient("admin") { client ->
        val api = OwnerTestApi(baseUrl = ApiClient.BASE_URL, httpClient = client)

        val response = api.list()
        response.status shouldBe 200

        val body = Json.parseToJsonElement(response.response.bodyAsText()).jsonObject
        val owners = body.getValue("owners").jsonArray
        owners.size shouldBe 2

        val individual = owners.single { it.jsonObject.getValue("type").jsonPrimitive.content == "INDIVIDUAL" }.jsonObject
        individual.getValue("firstName").jsonPrimitive.content shouldBe "John"
        individual.getValue("lastName").jsonPrimitive.content shouldBe "Doe"
        individual["id"] shouldNotBe null

        val business = owners.single { it.jsonObject.getValue("type").jsonPrimitive.content == "BUSINESS" }.jsonObject
        business.getValue("name").jsonPrimitive.content shouldBe "Acme Corp"
        business["id"] shouldNotBe null
    }
}

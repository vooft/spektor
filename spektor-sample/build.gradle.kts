
import io.github.vooft.spektor.gradle.SpektorGenerateTask
import io.github.vooft.spektor.gradle.createUnifiedSpec
import io.github.vooft.spektor.gradle.substitutions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    `spektor-jvm`
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.openapi)

    // dependency is already added in the root build.gradle.kts
    // without it the `spektor` accessor is not generated
    id("io.github.vooft.spektor")
}

application {
    mainClass = "io.ktor.server.cio.EngineMain"
}

spektor {
    specRoot = file("src/main/resources/openapi")

    createUnifiedSpec()

    substitutions {
        ref {
            file = file("src/main/resources/openapi/models/money.yaml")
            model = "Money"
        } with "io.github.vooft.spektor.sample.models.Money"

        microtype {
            file = file("src/main/resources/openapi/models/book.yaml")
            model = "Book"
            property = "id"
        } with "io.github.vooft.spektor.sample.models.BookId"

        microtype {
            file = file("src/main/resources/openapi/models/book.yaml")
            model = "Book"
            property = "authorId"
        } with "io.github.vooft.spektor.sample.models.AuthorId"

        microtype {
            file = file("src/main/resources/openapi/models/book.yaml")
            model = "BookRequest"
            property = "authorId"
        } with "io.github.vooft.spektor.sample.models.AuthorId"

        microtype {
            file = file("src/main/resources/openapi/models/author.yaml")
            model = "Author"
            property = "id"
        } with "io.github.vooft.spektor.sample.models.AuthorId"
    }
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.simple)
    implementation(libs.ktor.client.auth)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.junit.jupiter.engine)

    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.kotlinx.dateime)
}

tasks.withType<FormatTask> {
    dependsOn(tasks.withType<SpektorGenerateTask>())
}

listOf("src/main/resources/openapi/api/author.yaml", "src/main/resources/openapi/api/book.yaml").forEachIndexed { index, specPath ->
    val taskName = "generateTestClientOpenApi$index"
    tasks.register<GenerateTask>(taskName) {
        generatorName.set("kotlin")
        library.set("multiplatform")
        skipOverwrite.set(true)

        inputSpec.set(file(specPath).absolutePath)
        outputDir.set("${layout.buildDirectory.get().asFile.absolutePath}/test-generated")

        packageName.set("io.github.vooft.spektor.test")

        modelNameSuffix.set("TestDto")

        additionalProperties.set(
            mapOf(
                "dateLibrary" to "kotlinx-datetime",
            )
        )

        importMappings = mapOf(
            "AuthorRequestTestDto" to "io.github.vooft.spektor.models.AuthorRequestTestDto",
            "AuthorTestDto" to "io.github.vooft.spektor.models.AuthorTestDto",
        )

        configOptions.set(
            mapOf(
                "interfaceOnly" to "true",
                "useTags" to "true",
                "enumPropertyNaming" to "original",
                "skipDefaultInterface" to "true",
                "apiSuffix" to "TestApi"
            )
        )
    }

    tasks.withType<KotlinCompile> {
        dependsOn(taskName)
    }

    tasks.withType<FormatTask> {
        dependsOn(taskName)
    }
}

kotlin.sourceSets["test"].kotlin.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/test-generated/src/commonMain/kotlin")

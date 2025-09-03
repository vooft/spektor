
import io.github.vooft.spektor.gradle.SpektorGenerateTask
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
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.simple)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.junit.jupiter.engine)

    testImplementation(platform(libs.okhttp.bom))
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:logging-interceptor")
    testImplementation("com.squareup.okhttp3:okhttp-urlconnection")

    testImplementation(platform(libs.jackson.bom))
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

tasks.withType<FormatTask> {
    dependsOn(tasks.withType<SpektorGenerateTask>())
}

listOf("src/main/resources/openapi/api/author.yaml", "src/main/resources/openapi/api/book.yaml").forEachIndexed { index, specPath ->
    val taskName = "generateTestClientOpenApi$index"
    tasks.register<GenerateTask>(taskName) {
        generatorName.set("kotlin")
        skipOverwrite.set(true)

        inputSpec.set(file(specPath).absolutePath)
        outputDir.set("${layout.buildDirectory.get().asFile.absolutePath}/test-generated")

        packageName.set("io.github.vooft.spektor.test")

        typeMappings.put("DateTime", "java.time.Instant")
        typeMappings.put("java.time.OffsetDateTime", "java.time.Instant")
        importMappings.put("DateTime", "java.time.Instant")
        importMappings.put("java.time.OffsetDateTime", "java.time.Instant")

        modelNameSuffix.set("TestDto")

        configOptions.set(
            mapOf(
                "serializationLibrary" to "jackson",
                "interfaceOnly" to "true",
                "useTags" to "true",
                "enumPropertyNaming" to "original",
                "skipDefaultInterface" to "true",
                "apiSuffix" to "ClientApi"
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

kotlin.sourceSets["test"].kotlin.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/test-generated/src/main/kotlin")

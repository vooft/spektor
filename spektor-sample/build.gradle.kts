import io.github.vooft.spektor.gradle.SpektorGenerateTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask

plugins {
    `spektor-jvm`
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)

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
}

tasks.withType<FormatTask> {
    dependsOn(tasks.withType<SpektorGenerateTask>())
}

plugins {
    `spektor-jvm`
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
}

application {
    mainClass = "io.ktor.server.cio.EngineMain"
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

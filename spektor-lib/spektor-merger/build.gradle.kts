import org.gradle.kotlin.dsl.dependencies

plugins {
    `spektor-jvm`
    `spektor-publish`
}

dependencies {
    implementation(libs.swagger.parser)
    implementation(libs.kotlin.logging)

    testImplementation(project(":spektor-testdata"))
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.slf4j.simple)
}

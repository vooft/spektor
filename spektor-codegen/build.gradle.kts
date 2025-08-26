plugins {
    `spektor-jvm`
    `spektor-publish`
}

dependencies {
    implementation(project(":spektor-model"))
    implementation(project(":spektor-parser"))

    implementation(libs.kotlinpoet)
    implementation(libs.kotlin.logging)

    testImplementation(project(":spektor-testdata"))
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.slf4j.simple)
}

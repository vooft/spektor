plugins {
    `spektor-jvm`
    `spektor-publish`
}

dependencies {
    implementation(libs.kotlin.logging)

    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.slf4j.simple)
}

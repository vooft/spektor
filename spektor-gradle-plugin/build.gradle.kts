plugins {
    `spektor-jvm`
    `spektor-publish`
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.plugin.publish)
}

gradlePlugin {
    website.set("https://github.com/vooft/spektor")
    vcsUrl.set("https://github.com/vooft/spektor")
    plugins {
        create("spektorPlugin") {
            id = "io.github.vooft.spektor"
            implementationClass = "io.github.vooft.spektor.gradle.SpektorGradlePlugin"
            displayName = "Spektor plugin"
            description = "Spektor OpenAPI generator for Ktor Gradle plugin"
            tags.set(listOf("spektor", "kotlin", "openapi", "generator"))
        }
    }
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.vooft:spektor-codegen")
    implementation("io.github.vooft:spektor-model")
    implementation("io.github.vooft:spektor-parser")

    implementation(libs.kotlin.gradle.plugin.api)
}

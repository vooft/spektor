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
    implementation("io.github.vooft:spektor-codegen:${project.version}")
    implementation("io.github.vooft:spektor-model:${project.version}")
    implementation("io.github.vooft:spektor-parser:${project.version}")
    implementation("io.github.vooft:spektor-merger:${project.version}")

    implementation(libs.kotlin.gradle.plugin.api)
}

tasks.register("spektorPublishToMavenCentral") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishAndReleaseToMavenCentral") } +
            gradle.includedBuilds.map { it.task(":spektorPublishToMavenCentral") } +
            listOfNotNull(tasks.findByName("publishAndReleaseToMavenCentral"))
    )
}

tasks.register("spektorPublishToMavenLocal") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishToMavenLocal") } +
            gradle.includedBuilds.map { it.task(":spektorPublishToMavenLocal") } +
            listOfNotNull(tasks.findByName("publishToMavenLocal"))
    )
}

tasks.register("spektorPublishPlugins") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishPlugins") } +
            gradle.includedBuilds.map { it.task(":spektorPublishPlugins") } +
            listOfNotNull(tasks.findByName("publishPlugins"))
    )
}

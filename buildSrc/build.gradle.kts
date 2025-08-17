plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // kotlin
    implementation(libs.gradle.plugin.kotlin)

    // detekt / ktlint
    implementation(libs.gradle.plugin.detekt)
    implementation(libs.gradle.plugin.ktlint)

    // publishing
    implementation(libs.gradle.plugin.dokka)
    implementation(libs.gradle.plugin.maven.central.publish)
}

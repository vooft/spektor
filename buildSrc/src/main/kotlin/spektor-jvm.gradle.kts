
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("spektor-base")
    java
    kotlin("jvm")
}

group = "io.github.vooft"
version = System.getenv("TAG") ?: "1.0-SNAPSHOT"

dependencies {
    addPlatform(project, platform("org.jetbrains.kotlin:kotlin-bom:${getKotlinPluginVersion()}"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
        showExceptions = true
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xcontext-parameters")
        allWarningsAsErrors = false
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

private fun DependencyHandler.addPlatform(project: Project, platform: Dependency) {
    val availableConfigurations = project.configurations.map { it.name }.toSet()
    availableConfigurations.intersect(
        setOf("api", "implementation", "testImplementation")
    ).forEach { configuration ->
        add(configuration, platform)
    }
}

// not sure why, but this must be a separate task, otherwise it can't find the check tasks in subprojects
val checkAll = tasks.register("checkAll") {
    dependsOn(subprojects.map { it.tasks.getByName("check") }) // must be without a semicolon
    dependsOn(gradle.includedBuilds.map { it.task(":check") }) // must be with a semicolon
}

tasks.named("check") {
    dependsOn(checkAll)
}

val testAll = tasks.register("testAll") {
    dependsOn(subprojects.map { it.tasks.getByName("test") })
    dependsOn(gradle.includedBuilds.map { it.task(":test") })
}

tasks.named("test") {
    dependsOn(testAll)
}
//
//val cleanAll = tasks.register("cleanAll") {
//    dependsOn(subprojects.map { it.tasks.getByName("clean") })
//    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
//}
//
//tasks.named("clean") {
//    dependsOn(cleanAll)
//}

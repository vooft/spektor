
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("spektor-base")
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
        allWarningsAsErrors = true
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

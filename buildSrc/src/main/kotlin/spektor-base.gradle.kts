
import io.gitlab.arturbosch.detekt.Detekt
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jmailen.kotlinter")
}

group = "io.github.vooft"
version = System.getenv("TAG") ?: "1.0-SNAPSHOT"

detekt {
    buildUponDefaultConfig = true
    config.from(files("${rootDir.absolutePath}/detekt.yaml"))
    basePath = rootDir.absolutePath
}

tasks.withType<Detekt> {
    tasks.findByName("check")?.dependsOn(this)
    exclude { it.file.path.contains("generated/") }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType<LintTask> {
    source("build.gradle.kts", "settings.gradle.kts")
    exclude { it.file.path.contains("generated/") }
    dependsOn("formatKotlin")
}

tasks.withType<FormatTask> {
    source("build.gradle.kts", "settings.gradle.kts")
    exclude { it.file.path.contains("generated/") }
}

package io.github.vooft.spektor.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class SpektorGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Register extension
        val extension = target.extensions.create("spektor", SpektorExtension::class.java)

        // Add dependency to api configuration
//        target.dependencies.add("api", "io.github.vooft:spektor-api:${BuildConfig.VERSION}")

        val outputDirectory = target.layout.buildDirectory.dir("spektor-generated")

        // add generated classes to the source set
        target.extensions.getByType(KotlinJvmExtension::class.java).sourceSets.getByName("main").kotlin.srcDir(outputDirectory)

        // Run custom code after project is evaluated
        val spektorGenerate = target.tasks.register("spektorGenerate", SpektorGenerateTask::class.java) {
            it.outputPath.set(outputDirectory)
            it.basePackage.set(extension.basePackage)
            it.dtoSuffix.set(extension.dtoSuffix)
            it.serverApiSuffix.set(extension.serverApiSuffix)
            it.routesSuffix.set(extension.routesSuffix)
            it.specRoot.set(extension.requireSpecRoot())
            it.unifiedSpecName.set(extension.unifiedSpecName)

            it.dtoSubstitutions.set(extension.dtoSubstitutions)
            it.microtypeSubstitutions.set(extension.microtypeSubstitutions)

            it.substitutionFingerprint.set(extension.dtoSubstitutions.toString() + extension.microtypeSubstitutions.toString())
        }
        val spektorMerge = target.tasks.register("spektorMerge", SpektorMergeTask::class.java) {
            it.specRoot.set(extension.requireSpecRoot())
            it.unifiedSpecName.set(extension.unifiedSpecName)
            it.unifiedSpecTitle.set(extension.unifiedSpecTitle)
            it.unifiedSpecDescription.set(extension.unifiedSpecDescription)
            it.failOnUnifiedSpecError.set(extension.failOnUnifiedSpecError)
        }

        target.tasks.withType(KotlinCompilationTask::class.java) {
            it.finalizedBy(spektorMerge)
        }
        target.tasks.withType(KotlinCompilationTask::class.java) {
            it.dependsOn(spektorGenerate)
        }
    }
}

private fun SpektorExtension.requireSpecRoot() = specRoot ?: throw GradleException("spektor.specRoot is not set")

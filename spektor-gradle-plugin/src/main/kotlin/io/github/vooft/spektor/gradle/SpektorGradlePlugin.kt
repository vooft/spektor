package io.github.vooft.spektor.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class SpektorGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Register extension
        val extension = target.extensions.create("spektor", SpektorExtension::class.java)

        // Add dependency to api configuration
//        target.dependencies.add("api", "io.github.vooft:spektor-api:${BuildConfig.VERSION}")

        val spektorOutputDirectory = target.layout.buildDirectory.dir("spektor-generated")
        val generatedSourcesDirectory = spektorOutputDirectory.map { it.dir("kotlin") }
        val generatedResourcesDirectory = spektorOutputDirectory.map { it.dir("resources") }
        val unifiedSpecResourcesDirectory = generatedResourcesDirectory.map { it.dir("openapi") }

        // add generated classes to the source set
        target.extensions.getByType(KotlinJvmExtension::class.java).sourceSets.getByName("main").apply {
            kotlin.srcDir(generatedSourcesDirectory)
            resources.srcDir(generatedResourcesDirectory)
        }

        // Run custom code after project is evaluated
        val spektorMerge = target.tasks.register("spektorMerge", SpektorMergeTask::class.java) {
            it.specRoot.set(extension.requireSpecRoot())
            it.outputPath.set(unifiedSpecResourcesDirectory)
            it.unifiedSpecName.set(extension.unifiedSpecName)
            it.unifiedSpecTitle.set(extension.unifiedSpecTitle)
            it.unifiedSpecDescription.set(extension.unifiedSpecDescription)
            it.failOnMergeError.set(extension.failOnMergeError)
            it.unifiedSpecServers.set(extension.unifiedSpecServers)
        }
        val spektorGenerate = target.tasks.register("spektorGenerate", SpektorGenerateTask::class.java) {
            it.outputPath.set(generatedSourcesDirectory)
            it.basePackage.set(extension.basePackage)
            it.dtoSuffix.set(extension.dtoSuffix)
            it.serverApiSuffix.set(extension.serverApiSuffix)
            it.routesSuffix.set(extension.routesSuffix)
            it.specRoot.set(extension.requireSpecRoot())

            it.dtoSubstitutions.set(extension.dtoSubstitutions)
            it.microtypeSubstitutions.set(extension.microtypeSubstitutions)

            it.substitutionFingerprint.set(extension.dtoSubstitutions.toString() + extension.microtypeSubstitutions.toString())

            it.finalizedBy(spektorMerge)
        }

        target.tasks.withType(ProcessResources::class.java) {
            it.dependsOn(spektorMerge)
        }
        target.tasks.withType(KotlinCompilationTask::class.java) {
            it.dependsOn(spektorMerge)
            it.dependsOn(spektorGenerate)
        }
    }
}

private fun SpektorExtension.requireSpecRoot() = specRoot ?: throw GradleException("spektor.specRoot is not set")

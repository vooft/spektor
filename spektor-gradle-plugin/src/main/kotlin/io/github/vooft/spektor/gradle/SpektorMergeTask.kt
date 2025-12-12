package io.github.vooft.spektor.gradle

import io.github.vooft.spektor.merger.SpektorMerger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.pathString

abstract class SpektorMergeTask : DefaultTask() {

    @get:InputDirectory
    abstract val specRoot: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedResourcesPath: DirectoryProperty

    @get:Input
    abstract val unifiedSpecName: Property<String>

    @get:Input
    abstract val unifiedSpecTitle: Property<String>

    @get:Input
    abstract val unifiedSpecDescription: Property<String>

    @get:Input
    abstract val unifiedSpecServers: ListProperty<String>

    @get:Input
    abstract val failOnMergeError: Property<Boolean>

    @OptIn(ExperimentalPathApi::class)
    @TaskAction
    fun merge() {
        val specRoot = specRoot.asFile.get().toPath().toAbsolutePath()
        val outputPath = generatedResourcesPath.asFile.get().toPath().toAbsolutePath().let { path ->
            specRoot.pathString
                .substringAfterLast("resources")
                .split("/")
                .filterNot { it.isBlank() }
                .let { dirs ->
                    dirs.fold(path) { acc, name -> acc.resolve(name) }
                }
        }

        if (outputPath.exists()) {
            outputPath.deleteRecursively()
        }
        outputPath.createDirectories()

        val mergeResult = SpektorMerger(
            unifiedSpecName = unifiedSpecName.get(),
            unifiedSpecTitle = unifiedSpecTitle.get(),
            unifiedSpecDescription = unifiedSpecDescription.get(),
            servers = unifiedSpecServers.get(),
            specRoot = specRoot,
            outputPath = outputPath,
        ).merge()

        if (failOnMergeError.get()) {
            mergeResult.getOrThrow()
        } else {
            mergeResult.getOrDefault(Unit)
        }
    }
}

package io.github.vooft.spektor.gradle

import io.github.vooft.spektor.merger.SpektorMerger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

abstract class SpektorMergeTask : DefaultTask() {

    @get:InputDirectory
    abstract val specRoot: DirectoryProperty

    @get:OutputDirectory
    abstract val outputPath: DirectoryProperty

    @get:Input
    abstract val unifiedSpecName: Property<String>

    @get:Input
    abstract val unifiedSpecTitle: Property<String>

    @get:Input
    abstract val unifiedSpecDescription: Property<String>

    @get:Input
    abstract val failOnMergeError: Property<Boolean>

    @OptIn(ExperimentalPathApi::class)
    @TaskAction
    fun merge() {
        val specRoot = specRoot.asFile.get().toPath()
        val outputPath = outputPath.asFile.get().toPath()

        if (outputPath.exists()) {
            outputPath.deleteRecursively()
        }
        outputPath.createDirectories()

        specRoot.copyToRecursively(
            target = outputPath,
            overwrite = true,
            followLinks = true
        )

        val mergeResult = SpektorMerger(
            unifiedSpecName = unifiedSpecName.get(),
            unifiedSpecTitle = unifiedSpecTitle.get(),
            unifiedSpecDescription = unifiedSpecDescription.get(),
            specRoot = outputPath,
        ).merge()

        if (failOnMergeError.get()) {
            mergeResult.getOrThrow()
        } else {
            mergeResult.getOrDefault(Unit)
        }
    }
}

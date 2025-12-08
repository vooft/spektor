package io.github.vooft.spektor.gradle

import io.github.vooft.spektor.merger.SpektorMerger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

abstract class SpektorMergeTask : DefaultTask() {

    @get:InputDirectory
    abstract val specRoot: DirectoryProperty

    @get:Input
    abstract val unifiedSpecName: Property<String>

    @get:Input
    abstract val unifiedSpecTitle: Property<String>

    @get:Input
    abstract val unifiedSpecDescription: Property<String>

    @get:Input
    abstract val failOnUnifiedSpecError: Property<Boolean>

    @TaskAction
    fun merge() {
        val mergeResult = SpektorMerger(
            unifiedSpecName = unifiedSpecName.get(),
            unifiedSpecTitle = unifiedSpecTitle.get(),
            unifiedSpecDescription = unifiedSpecDescription.get(),
            specRoot = specRoot.asFile.get().toPath()
        ).merge()

        if (failOnUnifiedSpecError.get()) {
            mergeResult.getOrThrow()
        } else {
            mergeResult.getOrDefault(Unit)
        }
    }

}

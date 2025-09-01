package io.github.vooft.spektor.gradle

import io.github.vooft.spektor.codegen.SpektorCodegen
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class SpektorGenerateTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputPath: DirectoryProperty

    @get:InputDirectory
    abstract val specRoot: DirectoryProperty

    @get:Input
    abstract val basePackage: Property<String>

    @get:Input
    abstract val dtoSuffix: Property<String>

    @get:Input
    abstract val serverApiSuffix: Property<String>

    @get:Input
    abstract val routesSuffix: Property<String>

    @TaskAction
    fun generate() {
        val files = specRoot.asFile.get().walk()
            .filter { it.extension == "yaml" }
            .map { it.toPath().toAbsolutePath().normalize() }
            .toList()

        val parser = SpektorParser()
        val schema = parser.parse(files)

        val config = SpektorCodegenConfig(
            basePackage = basePackage.get(),
            specRoot = specRoot.asFile.get().toPath(),
            dtoSuffix = dtoSuffix.get(),
            serverApiSuffix = serverApiSuffix.get(),
            routesSuffix = routesSuffix.get()
        )

        val codegen = SpektorCodegen(config)
        val context = codegen.generate(schema)

        codegen.write(context, outputPath.asFile.get().toPath())
    }
}


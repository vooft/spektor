package io.github.vooft.spektor.gradle

import io.github.vooft.spektor.codegen.SpektorCodegen
import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.codegen.common.SpektorPropertyRef
import io.github.vooft.spektor.merger.SpektorMerger.Companion.isYaml
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.parser.SpektorParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
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

    @get:Internal
    abstract val dtoSubstitutions: MapProperty<ModelRef, String>

    @get:Internal
    abstract val microtypeSubstitutions: MapProperty<PropertyRef, String>

    @get:Input
    abstract val substitutionFingerprint: Property<String>

    @TaskAction
    fun generate() {
        val files = specRoot.asFile.get().walk()
            .map { it.toPath().toAbsolutePath().normalize() }
            .filter { it.isYaml() }
            .toList()

        val parser = SpektorParser()
        val schema = parser.parse(files)

        val config = SpektorCodegenConfig(
            basePackage = basePackage.get(),
            specRoot = specRoot.asFile.get().toPath(),
            dtoSuffix = dtoSuffix.get(),
            serverApiSuffix = serverApiSuffix.get(),
            routesSuffix = routesSuffix.get(),
            dtoSubstitutions = dtoSubstitutions.get().mapKeys { (ref, _) ->
                SpektorType.Ref(file = ref.file.toPath(), modelName = ref.modelName)
            }.toMap(),
            microtypeSubstitutions = microtypeSubstitutions.get().mapKeys { (ref, _) ->
                SpektorPropertyRef(
                    ref = SpektorType.Ref(ref.ref.file.toPath(), ref.ref.modelName),
                    propertyName = ref.propertyName
                )
            }.toMap()
        )

        val codegen = SpektorCodegen(config)
        val context = codegen.generate(schema)

        codegen.write(context, outputPath.asFile.get().toPath())
    }
}

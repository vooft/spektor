package io.github.vooft.spektor.gradle.substitution

import io.github.vooft.spektor.gradle.ModelRef
import io.github.vooft.spektor.gradle.PropertyRef
import java.io.File

internal class SubstitutionDslImpl : SubstitutionDsl {

    private val startedRefs = mutableListOf<RefSubstitutionDslImpl>()
    private val startedMicrotypes = mutableListOf<MicrotypeSubstitutionDslImpl>()

    private val refsSubstitutions = mutableMapOf<ModelRef, String>()
    private val microtypesSubstitutions = mutableMapOf<PropertyRef, String>()

    fun collectSubstitutions(): Pair<Map<ModelRef, String>, Map<PropertyRef, String>> {
        if (startedRefs.isNotEmpty()) {
            val dsl = startedRefs.first()
            throw IllegalStateException("Ref substitution is not completed. Missing 'with' for ref ${dsl.model} in file ${dsl.file}")
        }
        if (startedMicrotypes.isNotEmpty()) {
            val dsl = startedMicrotypes.first()
            throw IllegalStateException("Microtype substitution is not completed. Missing 'with' for property ${dsl.property} of model ${dsl.model} in file ${dsl.file}")
        }

        return refsSubstitutions to microtypesSubstitutions
    }

    override fun ref(block: RefSubstitutionDsl.() -> Unit): SubstituteWithDsl {
        val dsl = RefSubstitutionDslImpl().also(block)
        val modelRef = ModelRef(file = dsl.file, modelName = dsl.model)
        startedRefs.add(dsl)

        return object : SubstituteWithDsl {
            override fun with(type: String) {
                startedRefs.remove(dsl)
                refsSubstitutions[modelRef] = type
            }
        }
    }

    override fun microtype(block: MicrotypeSubstitutionDsl.() -> Unit): SubstituteWithDsl {
        val dsl = MicrotypeSubstitutionDslImpl().also(block)
        val propertyRef = PropertyRef(ref = ModelRef(file = dsl.file, modelName = dsl.model), propertyName = dsl.property)
        startedMicrotypes.add(dsl)

        return object : SubstituteWithDsl {
            override fun with(type: String) {
                startedMicrotypes.remove(dsl)
                microtypesSubstitutions[propertyRef] = type
            }
        }
    }

    class RefSubstitutionDslImpl : RefSubstitutionDsl {
        var fileField: File? = null
        var modelField: String? = null

        override var file: File
            get() = requireNotNull(fileField) { "File is not set" }
            set(value) { fileField = value }

        override var model: String
            get() = requireNotNull(modelField) { "Model is not set" }
            set(value) { modelField = value }
    }

    class MicrotypeSubstitutionDslImpl : MicrotypeSubstitutionDsl {
        var fileField: File? = null
        var modelField: String? = null
        var propertyField: String? = null

        override var file: File
            get() = requireNotNull(fileField) { "File is not set" }
            set(value) { fileField = value }

        override var model: String
            get() = requireNotNull(modelField) { "Model is not set" }
            set(value) { modelField = value }

        override var property: String
            get() = requireNotNull(propertyField) { "Property is not set" }
            set(value) { propertyField = value }
    }
}

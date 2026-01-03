package io.github.vooft.spektor.gradle

import io.github.vooft.spektor.gradle.substitution.SubstitutionDsl
import io.github.vooft.spektor.gradle.substitution.SubstitutionDslImpl
import io.github.vooft.spektor.gradle.unifiedspec.UnifiedSpecDsl
import io.github.vooft.spektor.gradle.unifiedspec.UnifiedSpecDslImpl
import java.io.File

open class SpektorExtension {
    var specRoot: File? = null

    internal var unifiedSpec: UnifiedSpec? = null

    var basePackage: String = "spektor.example"
    var enabled: Boolean = true
    var dtoSuffix: String = "Dto"
    var serverApiSuffix: String = "ServerApi"
    var routesSuffix: String = "Routes"

    val dtoSubstitutions: MutableMap<ModelRef, String> = mutableMapOf()
    val microtypeSubstitutions: MutableMap<PropertyRef, String> = mutableMapOf()
}

fun SpektorExtension.substitutions(block: SubstitutionDsl.() -> Unit) {
    val dsl = SubstitutionDslImpl()
    dsl.block()

    val (dtoSubs, microtypeSubs) = dsl.collectSubstitutions()
    dtoSubstitutions.putAll(dtoSubs)
    microtypeSubstitutions.putAll(microtypeSubs)
}

fun SpektorExtension.createUnifiedSpec(block: UnifiedSpecDsl.() -> Unit = {}) {
    val dsl = UnifiedSpecDslImpl()
    dsl.block()
    unifiedSpec = dsl.build()
}

data class ModelRef(val file: File, val modelName: String)

data class PropertyRef(val ref: ModelRef, val propertyName: String)

data class UnifiedSpec(
    val specName: String,
    val specTitle: String,
    val specDescription: String?,
    val specServers: List<String>,
    val failOnMergeError: Boolean,
)

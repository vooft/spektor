package io.github.vooft.spektor.gradle

import io.github.vooft.spektor.gradle.substitution.SubstitutionDsl
import io.github.vooft.spektor.gradle.substitution.SubstitutionDslImpl
import java.io.File

open class SpektorExtension {
    var specRoot: File? = null

    var unifiedSpecName: String = "openapi.yaml"
    var unifiedSpecTitle: String = "Unified API"
    var unifiedSpecDescription: String = "Unified API"
    var failOnUnifiedSpecError: Boolean = false

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

data class ModelRef(val file: File, val modelName: String)

data class PropertyRef(val ref: ModelRef, val propertyName: String)

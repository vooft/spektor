package io.github.vooft.spektor.gradle

import java.io.File

open class SpektorExtension {
    var specRoot: File? = null

    var basePackage: String = "spektor.example"
    var enabled: Boolean = true
    var dtoSuffix: String = "Dto"
    var serverApiSuffix: String = "ServerApi"
    var routesSuffix: String = "Routes"

    val dtoSubstitutions: MutableMap<ModelRef, String> = mutableMapOf()
    val microtypeSubstitutions: MutableMap<PropertyRef, String> = mutableMapOf()
}

data class ModelRef(val file: File, val modelName: String)

data class PropertyRef(val ref: ModelRef, val propertyName: String)


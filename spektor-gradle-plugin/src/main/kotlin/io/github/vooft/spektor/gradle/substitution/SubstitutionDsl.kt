package io.github.vooft.spektor.gradle.substitution

import java.io.File

interface SubstitutionDsl {
    fun ref(block: RefSubstitutionDsl.() -> Unit): SubstituteWithDsl
    fun microtype(block: MicrotypeSubstitutionDsl.() -> Unit): SubstituteWithDsl
}

interface RefSubstitutionDsl {
    var file: File
    var model: String
}

interface MicrotypeSubstitutionDsl {
    var file: File
    var model: String
    var property: String
}

interface SubstituteWithDsl {
    infix fun with(type: String)
}

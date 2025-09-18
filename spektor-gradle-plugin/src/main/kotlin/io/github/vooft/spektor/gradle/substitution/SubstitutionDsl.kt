package io.github.vooft.spektor.gradle.substitution

import java.io.File

interface SubstitutionDsl {
    fun ref(block: RefSubstitutionDsl.() -> Unit): WithSubstitutionDsl
    fun microtype(block: MicrotypeSubstitutionDsl.() -> Unit): WithSubstitutionDsl
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

interface WithSubstitutionDsl {
    infix fun with(type: String)
}


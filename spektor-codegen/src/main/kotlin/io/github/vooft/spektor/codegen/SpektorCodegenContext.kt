package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType

class SpektorCodegenContext(
    val paths: List<SpektorPath>,
    val refs: Map<SpektorType.Ref, SpektorType>,
    val generatedTypeSpecs: MutableMap<SpektorType.Ref, TypeAndClass> = mutableMapOf(),
    val generatedPathSpecs: MutableList<TypeAndClass> = mutableListOf(),
)

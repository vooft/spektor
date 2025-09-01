package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile

class SpektorCodegenContext(
    val paths: Map<TagAndFile, List<SpektorPath>>,
    val refs: Map<SpektorType.Ref, SpektorType>,
    val generatedTypeSpecs: MutableMap<SpektorType.Ref, TypeAndClass> = mutableMapOf(),
    val generatedPathSpecs: MutableMap<TagAndFile, TypeAndClass> = mutableMapOf(),
    val generatedRouteSpecs: MutableMap<TagAndFile, TypeAndClass> = mutableMapOf(),
)

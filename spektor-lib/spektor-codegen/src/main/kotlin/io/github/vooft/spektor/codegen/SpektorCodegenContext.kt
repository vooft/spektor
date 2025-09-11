package io.github.vooft.spektor.codegen

import com.squareup.kotlinpoet.TypeName
import io.github.vooft.spektor.codegen.common.TypeAndClass
import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.github.vooft.spektor.model.TagAndFile

data class SpektorCodegenContext(
    val paths: Map<TagAndFile, List<SpektorPath>>,
    val refs: Map<SpektorType.Ref, SpektorType>,
    val resolvedTypes: MutableMap<SpektorType, TypeName> = mutableMapOf(),
    val generatedTypeSpecs: MutableMap<SpektorType.Ref, TypeAndClass> = mutableMapOf(),
    val generatedPathSpecs: MutableMap<TagAndFile, TypeAndClass> = mutableMapOf(),
    val generatedRouteSpecs: MutableMap<TagAndFile, TypeAndClass> = mutableMapOf(),
)

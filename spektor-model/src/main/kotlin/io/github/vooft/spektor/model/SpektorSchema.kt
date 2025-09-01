package io.github.vooft.spektor.model

data class SpektorSchema(val paths: Map<TagAndFile, List<SpektorPath>>, val refs: Map<SpektorType.Ref, SpektorType>)

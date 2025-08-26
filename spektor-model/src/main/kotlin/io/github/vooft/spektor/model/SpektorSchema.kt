package io.github.vooft.spektor.model

data class SpektorSchema(val paths: List<SpektorPath>, val refs: Map<SpektorType.Ref, SpektorType>)

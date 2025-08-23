package io.github.vooft.spektor.parser

data class SpektorSchema(private val paths: List<SpektorPath>, private val refs: Map<SpektorType.Ref, SpektorType>)

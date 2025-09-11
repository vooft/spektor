package io.github.vooft.spektor.sample.models

import kotlinx.serialization.Serializable

@Serializable
data class Money(val minorUnits: Int, val currency: Currency) {
    enum class Currency {
        EUR,
        USD,
        JPY
    }
}

package io.github.vooft.spektor.gradle.unifiedspec

import io.github.vooft.spektor.gradle.UnifiedSpec

class UnifiedSpecDslImpl : UnifiedSpecDsl {

    override var specName: String = "openapi"
    override var specTitle: String = "Unified API"
    override var specDescription: String? = "Unified API"
    override val specServers: MutableList<String> = mutableListOf()
    override var failOnMergeError: Boolean = true

    fun build(): UnifiedSpec = UnifiedSpec(
        specName = specName,
        specTitle = specTitle,
        specDescription = specDescription,
        specServers = specServers,
        failOnMergeError = failOnMergeError,
    )
}

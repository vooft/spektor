package io.github.vooft.spektor.gradle.unifiedspec

interface UnifiedSpecDsl {
    var specName: String
    var specTitle: String
    var specDescription: String?
    val specServers: MutableList<String>
    var failOnMergeError: Boolean
}

import org.gradle.plugin.use.PluginDependenciesSpec

val PluginDependenciesSpec.`spektor-base` get() = id("spektor-base")
val PluginDependenciesSpec.`spektor-jvm` get() = id("spektor-jvm")
val PluginDependenciesSpec.`spektor-publish` get() = id("spektor-publish")

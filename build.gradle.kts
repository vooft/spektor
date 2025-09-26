plugins {
    `spektor-jvm`
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.vooft:spektor-gradle-plugin")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.getByName("test") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("test") })
    dependsOn(gradle.includedBuilds.map { it.task(":test") })
}

tasks.getByName("check") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("check") })
    dependsOn(gradle.includedBuilds.map { it.task(":check") })
}

tasks.register("spektorPublishToMavenCentral") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishAndReleaseToMavenCentral") } +
            gradle.includedBuilds.map { it.task(":spektorPublishToMavenCentral") } +
            listOfNotNull(tasks.findByName("publishAndReleaseToMavenCentral"))
    )
}

tasks.register("spektorPublishToMavenLocal") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishToMavenLocal") } +
            gradle.includedBuilds.map { it.task(":spektorPublishToMavenLocal") } +
            listOfNotNull(tasks.findByName("publishToMavenLocal"))
    )
}

tasks.register("spektorPublishPlugins") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishPlugins") } +
            gradle.includedBuilds.map { it.task(":spektorPublishPlugins") } +
            listOfNotNull(tasks.findByName("publishPlugins"))
    )
}

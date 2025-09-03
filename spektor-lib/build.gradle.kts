plugins {
    `spektor-jvm`
}

allprojects {
    repositories {
        mavenCentral()
    }
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

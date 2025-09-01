rootProject.name = "spektor"

include(
    ":spektor-sample",
)

includeBuild("spektor-gradle-plugin") {
    dependencySubstitution {
        substitute(module("io.github.vooft:spektor-gradle-plugin")).using(project(":"))
    }
}

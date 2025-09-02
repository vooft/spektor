rootProject.name = "spektor"

include(
    ":spektor-sample",
)

includeBuild("spektor-lib") {
    dependencySubstitution {
        listOf("spektor-codegen", "spektor-model", "spektor-parser", "spektor-testdata").forEach {
            substitute(module("io.github.vooft:$it")).using(project(":$it"))
        }
    }
}

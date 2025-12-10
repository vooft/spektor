dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../spektor-lib") {
    dependencySubstitution {
        listOf(
            "spektor-codegen",
            "spektor-model",
            "spektor-parser",
            "spektor-testdata",
            "spektor-merger",
        ).forEach {
            substitute(module("io.github.vooft:$it")).using(project(":$it"))
        }
    }
}

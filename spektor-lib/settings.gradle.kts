rootProject.name = "spektor-lib"

include(
    ":spektor-model",
    ":spektor-testdata",
    ":spektor-parser",
    ":spektor-codegen",
    ":spektor-merger",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

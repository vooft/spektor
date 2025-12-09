plugins {
//    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    pom {
        name = "spektor"
        description = "Kotlin BDD testing framework"
        url = "https://github.com/vooft/spektor"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        scm {
            connection = "https://github.com/vooft/spektor"
            url = "https://github.com/vooft/spektor"
        }
        developers {
            developer {
                name = "spektor team"
            }
        }
    }
}

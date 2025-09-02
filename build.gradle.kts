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

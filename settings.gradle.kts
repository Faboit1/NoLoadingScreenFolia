plugins {
    // Lets Gradle fetch the Java 25 toolchain itself when the machine running
    // the build does not already have one.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "NoLoadingScreenFolia"

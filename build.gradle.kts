plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

val foliaApiVersion = project.property("foliaApiVersion") as String
val packetEventsVersion = project.property("packetEventsVersion") as String

group = "dev.cheesesmp"
version = "2.0.0"
description = "Removes the 'Loading terrain' screen when teleporting between same-environment worlds on Folia."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly("dev.folia:folia-api:$foliaApiVersion")
    implementation("com.github.retrooper:packetevents-spigot:$packetEventsVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version, "description" to project.description))
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    // PacketEvents must be relocated when shaded so this plugin's copy cannot
    // clash with a standalone PacketEvents install or another shading plugin.
    relocate("com.github.retrooper.packetevents", "dev.cheesesmp.noloadingscreen.shaded.packetevents.api")
    relocate("io.github.retrooper.packetevents", "dev.cheesesmp.noloadingscreen.shaded.packetevents.impl")

    // PacketEvents is built against Adventure 4.x while Folia 26.2 ships 5.x,
    // and it does not expose adventure-nbt to plugins at all. Carrying a
    // relocated 4.x keeps PacketEvents on the version it was compiled for and
    // leaves the server's copy untouched. Nothing here hands Adventure objects
    // across that boundary, so the two never have to agree.
    relocate("net.kyori", "dev.cheesesmp.noloadingscreen.shaded.kyori")

    // No minimize(): PacketEvents resolves large parts of itself reflectively,
    // so static reachability analysis strips classes it needs at runtime.
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}

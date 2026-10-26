plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("ploceus") version "1.17.4"
}

fun prop(name: String) = property(name) as String

version = "${prop("mod_version")}+${prop("minecraft_version")}"
group = prop("maven_group")
base.archivesName = prop("mod_id")

ploceus {
    setIntermediaryGeneration(2)
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
}

repositories {
    maven("https://maven.cloverclient.com/releases") {
        content { includeGroup("pl.tomgirl") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")
    mappings(ploceus.layeredMappings {
        mappings("net.ornithemc:feather-gen2:${prop("minecraft_version")}+build.${prop("feather_build")}:v2")
    })
    modImplementation("net.fabricmc:fabric-loader:${prop("loader_version")}")
    modImplementation("pl.tomgirl:pylon:${prop("pylon_version")}")
}

loom {
    runConfigs.all {
        preferGradleTask = true
        runDirectory = file("run")
    }
    runConfigs.named("client") {
        programArgs("--uuid", "00000000000000000000000000000000")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 25
        options.encoding = "UTF-8"
    }

    processResources {
        val props = mapOf("version" to project.version, "loader_version" to prop("loader_version"))
        inputs.properties(props)
        filesMatching("fabric.mod.json") { expand(props) }
    }
}

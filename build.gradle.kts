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
    maven("https://repo.polyfrost.org/releases") {
        content { includeGroup("org.polyfrost.oneconfig") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")
    mappings(ploceus.layeredMappings {
        mappings("net.ornithemc:feather-gen2:${prop("minecraft_version")}+build.${prop("feather_build")}:v2")
    })
    modImplementation("net.fabricmc:fabric-loader:${prop("loader_version")}")
    modImplementation("pl.tomgirl:pylon:${prop("pylon_version")}")
    implementation("java3d:vecmath:1.5.2")
    for (module in listOf("config", "config-impl", "hud", "ui", "utils", "events")) {
        compileOnly("org.polyfrost.oneconfig:$module:${prop("oneconfig_version")}") { isTransitive = false }
    }
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.4.20")
    include("java3d:vecmath:1.5.2")
}

val stubs = file("src/stub").listFiles()!!.map { dir ->
    tasks.register<Zip>("${dir.name}Stub") {
        archiveFileName = "${dir.name}-stub.jar"
        destinationDirectory = layout.buildDirectory.dir("stub")
        from(dir)
    }
}

loom {
    mods {
        register("forgecompat") { sourceSet(sourceSets.main.get()) }
    }
    runConfigs.all {
        preferGradleTask = true
        runDirectory = file("run")
    }
    runConfigs.named("client") {
        programArgs("--uuid", "00000000000000000000000000000000")
        vmArg("-Dfabric.addMods=" + stubs.joinToString(File.pathSeparator) { it.get().archiveFile.get().asFile.path })
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

    named("runClient") { dependsOn(stubs) }

    processResources {
        val props = mapOf("version" to project.version, "loader_version" to prop("loader_version"))
        inputs.properties(props)
        filesMatching("fabric.mod.json") { expand(props) }
        from(stubs) { into("META-INF/jars") }
    }
}

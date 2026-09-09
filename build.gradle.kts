plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
    `maven-publish`
}

group = property("maven_group") as String
version = "${property("mod_version")}+fabric-mc${property("minecraft_version")}"

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
    maven("https://maven.modmuss50.me/") {
        content { includeGroup("teamreborn") }
    }
    maven("https://maven.squiddev.cc") {
        content { includeGroup("cc.tweaked") }
    }
    maven("https://maven.blamejared.com/") {
        content { includeGroup("mezz.jei") }
    }
    maven("https://maven.shedaniel.me/") {
        content {
            includeGroup("me.shedaniel.cloth")
            includeGroup("me.shedaniel.cloth.api")
        }
    }
}

// The recipe viewer is an optional compile-time neighbour: CI downloads its release jar into
// libs/, a local checkout uses the sibling repo's build output, whichever exists.
val recipeViewerJar = listOf(file("libs"), file("../../create-rei/CreateReiViewer-Fly/build/libs"))
    .flatMap { dir -> fileTree(dir) { include("CreateReiViewer-*.jar"); exclude("*-sources.jar") }.files }
    .maxByOrNull { it.lastModified() }
    ?: error("No Create Fly Recipe Viewer jar in libs/ or ../../create-rei/CreateReiViewer-Fly/build/libs")

loom {
    splitEnvironmentSourceSets()
    mods {
        create("createaddition") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
}

sourceSets {
    main {
        java.exclude(
            "com/mrh0/createaddition/index/CAAdvancements.java",
        )
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    implementation("maven.modrinth:create-fly:${property("create_fabric_version")}")
    implementation(include("teamreborn:energy:${property("team_reborn_energy_version")}")!!)

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("cc.tweaked:cc-tweaked-26.2-fabric-api:1.120.2")
    "clientCompileOnly"("mezz.jei:jei-26.2-fabric:30.24.0.165")
    compileOnly("maven.modrinth:rei:${property("rei_version")}")
    compileOnly("maven.modrinth:architectury-api:${property("architectury_version")}")
    compileOnly("me.shedaniel.cloth:basic-math:${property("basic_math_version")}")
    compileOnly(files(recipeViewerJar))
    "clientCompileOnly"("maven.modrinth:rei:${property("rei_version")}")
    "clientCompileOnly"("maven.modrinth:architectury-api:${property("architectury_version")}")
    "clientCompileOnly"("me.shedaniel.cloth:basic-math:${property("basic_math_version")}")
    "clientCompileOnly"(files(recipeViewerJar))
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.processResources {
    val modMetadata = mapOf(
        "version" to project.version.toString(),
        "minecraft_dependency_version" to project.property("minecraft_dependency_version") as String,
        "fabric_loader_version" to project.property("fabric_loader_version") as String,
        "create_fabric_version_range" to project.property("create_fabric_version_range") as String,
    )
    inputs.properties(modMetadata)
    filesMatching("fabric.mod.json") {
        expand(modMetadata)
    }
}

tasks.jar {
    from("LICENSE")
    from("NOTICE")
}

tasks.named<Jar>("sourcesJar") {
    from("LICENSE")
    from("NOTICE")
}

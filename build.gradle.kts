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
}

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
    inputs.property("version", version)
    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version.toString(),
            "minecraft_dependency_version" to project.property("minecraft_dependency_version") as String,
            "fabric_loader_version" to project.property("fabric_loader_version") as String,
            "create_fabric_version_range" to project.property("create_fabric_version_range") as String,
        )
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

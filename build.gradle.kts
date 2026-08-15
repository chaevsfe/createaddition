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
}

loom {
    mods {
        create("createaddition") {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    main {
        java.exclude(
            "com/mrh0/createaddition/blocks/**",
            "com/mrh0/createaddition/commands/**",
            "com/mrh0/createaddition/compat/**",
            "com/mrh0/createaddition/debug/**",
            "com/mrh0/createaddition/effect/**",
            "com/mrh0/createaddition/energy/**",
            "com/mrh0/createaddition/event/**",
            "com/mrh0/createaddition/index/CAAdvancements.java",
            "com/mrh0/createaddition/index/CAArmInteractions.java",
            "com/mrh0/createaddition/index/CABlockEntities.java",
            "com/mrh0/createaddition/index/CABlocks.java",
            "com/mrh0/createaddition/index/CACapabilities.java",
            "com/mrh0/createaddition/index/CADamageTypes.java",
            "com/mrh0/createaddition/index/CADisplaySources.java",
            "com/mrh0/createaddition/index/CAEffects.java",
            "com/mrh0/createaddition/index/CAEntities.java",
            "com/mrh0/createaddition/index/CAFluids.java",
            "com/mrh0/createaddition/index/CAItems.java",
            "com/mrh0/createaddition/index/CALang.java",
            "com/mrh0/createaddition/index/CAPartials.java",
            "com/mrh0/createaddition/index/CAPonders.java",
            "com/mrh0/createaddition/index/CARecipes.java",
            "com/mrh0/createaddition/index/CASounds.java",
            "com/mrh0/createaddition/index/CASpriteShifts.java",
            "com/mrh0/createaddition/item/**",
            "com/mrh0/createaddition/mixin/**",
            "com/mrh0/createaddition/network/**",
            "com/mrh0/createaddition/ponder/**",
            "com/mrh0/createaddition/recipe/**",
            "com/mrh0/createaddition/rendering/**",
            "com/mrh0/createaddition/shapes/**",
            "com/mrh0/createaddition/sound/**",
            "com/mrh0/createaddition/trains/**",
            "com/mrh0/createaddition/util/**",
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
}

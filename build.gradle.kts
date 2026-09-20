import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.imageio.ImageIO

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

// The recipe viewer is nested into this jar and compiled against: CI downloads its release
// jar into libs/, a local checkout uses the sibling repo's build output.
repositories {
    flatDir {
        dirs("libs", "../../create-rei/CreateReiViewer-Fly/build/libs")
    }
}
val recipeViewer = ":CreateReiViewer:${property("createreiviewer_version")}+fabric-mc${property("minecraft_version")}"

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
    compileOnly(recipeViewer)
    include(recipeViewer)
    "clientCompileOnly"("maven.modrinth:rei:${property("rei_version")}")
    "clientCompileOnly"("maven.modrinth:architectury-api:${property("architectury_version")}")
    "clientCompileOnly"("me.shedaniel.cloth:basic-math:${property("basic_math_version")}")
    "clientCompileOnly"(recipeViewer)
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

val generatedConnectedTextureResources = layout.buildDirectory.dir("generated/connected-texture-resources")

val omniTileIndexes = listOf(
    1, 2, 3, 8, 9, 10, 11, 12, 13, 16, 17, 18, 19, 20, 21, 24, 25, 26, 27, 28, 29, 30,
    32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 46, 48, 49, 50, 51, 52, 53, 54, 56, 57, 58,
)
val rectangleTileIndexes = (0..11).toList() + (13..15).toList()

val connectedTextureSheets = mapOf(
    "assets/createaddition/textures/block/modular_accumulator/block_connected.png" to (4 to rectangleTileIndexes),
    "assets/createaddition/textures/block/modular_accumulator/block_top_connected.png" to (4 to rectangleTileIndexes),
    "assets/createaddition/textures/block/copper_wire_casing/block_connected.png" to (8 to omniTileIndexes),
)

val generateConnectedTextureSprites = tasks.register("generateConnectedTextureSprites") {
    val resourceRoot = file("src/main/resources")
    inputs.files(fileTree(resourceRoot) { include(connectedTextureSheets.keys) })
        .withPropertyName("connectedTextureSheets")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("connectedTextureLayout", "create-fly-26.2-v1")
    outputs.dir(generatedConnectedTextureResources)
    doLast {
        val outputRoot = generatedConnectedTextureResources.get().asFile
        delete(outputRoot)
        var sheetCount = 0
        var spriteCount = 0
        connectedTextureSheets.forEach { (pattern, layout) ->
            val (gridSize, tileIndexes) = layout
            fileTree(resourceRoot) { include(pattern) }.files.sortedBy { it.invariantSeparatorsPath }.forEach { sheetFile ->
                val sheet = ImageIO.read(sheetFile) ?: throw GradleException("Could not decode $sheetFile")
                if (sheet.width != sheet.height || sheet.width % gridSize != 0) {
                    throw GradleException("$sheetFile must be a $gridSize x $gridSize grid of square tiles, but is ${sheet.width} x ${sheet.height}")
                }
                val tileSize = sheet.width / gridSize
                val relativeSheet = resourceRoot.toPath().relativize(sheetFile.toPath()).toString()
                val spriteDirectory = outputRoot.resolve(relativeSheet.removeSuffix(".png"))
                spriteDirectory.mkdirs()
                tileIndexes.forEachIndexed { index, sourceTileIndex ->
                    val tile = sheet.getSubimage(sourceTileIndex % gridSize * tileSize, sourceTileIndex / gridSize * tileSize, tileSize, tileSize)
                    if (!ImageIO.write(tile, "png", spriteDirectory.resolve("${index + 1}.png"))) {
                        throw GradleException("No PNG writer is available for $spriteDirectory")
                    }
                    spriteCount++
                }
                sheetCount++
            }
        }
        if (sheetCount != connectedTextureSheets.size) {
            throw GradleException("Expected ${connectedTextureSheets.size} connected-texture sheets, found $sheetCount")
        }
        logger.lifecycle("Generated $spriteCount connected-texture sprites from $sheetCount sheets")
    }
}

tasks.processResources {
    dependsOn(generateConnectedTextureSprites)
    from(generatedConnectedTextureResources)
    exclude(connectedTextureSheets.keys)
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

val allowedJarPrefixes = listOf(
    "com/mrh0/createaddition/",
    "assets/createaddition/",
    "data/createaddition/",
    "data/c/",
    "data/minecraft/",
    "data/create/recipe/",
    "META-INF/jars/",
)

val allowedJarFiles = listOf(
    "META-INF/MANIFEST.MF",
    "fabric.mod.json",
    "createaddition.mixins.json",
    "createaddition.accesswidener",
    "icon_fly_port.png",
    "assets/minecraft/atlases/blocks.json",
    "LICENSE",
    "NOTICE",
)

val foreignTag = Regex("^data/[a-z0-9_.-]+/tags/.+\\.json$")

afterEvaluate {
    val checkForeignNamespaces = tasks.register("checkForeignNamespaces") {
        val jarNames = listOf("remapJar", "remapSourcesJar", "jar", "sourcesJar")
            .filter { tasks.names.contains(it) }
            .let { names -> if (names.contains("remapJar")) names.filter { it.startsWith("remap") } else names }
        require(jarNames.isNotEmpty()) { "checkForeignNamespaces found no jar task to inspect" }
        val jarTasks = jarNames.map { tasks.named<Jar>(it).get() }
        dependsOn(jarTasks)
        val archives: List<Provider<RegularFile>> = jarTasks.map { it.archiveFile }
        val prefixes = allowedJarPrefixes
        val files = allowedJarFiles
        doLast {
            val bad = mutableListOf<String>()
            var checked = 0
            for (provider in archives) {
                val jar: File = provider.get().asFile
                if (!jar.exists()) continue
                checked++
                val zip = ZipFile(jar)
                try {
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry: ZipEntry = entries.nextElement()
                        if (entry.isDirectory) continue
                        val name: String = entry.name
                        if (name in files) continue
                        if (prefixes.any { p -> name.startsWith(p) }) continue
                        if (foreignTag.matches(name)) continue
                        bad.add(jar.name + "!" + name)
                    }
                } finally {
                    zip.close()
                }
            }
            if (checked == 0) {
                throw GradleException("checkForeignNamespaces inspected no archives")
            }
            if (bad.isNotEmpty()) {
                throw GradleException("Foreign namespace entries in published artifacts:\n" + bad.joinToString("\n"))
            }
        }
    }
    tasks.named("check") { dependsOn(checkForeignNamespaces) }
}

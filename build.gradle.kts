import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.publish.maven.MavenPublication
import java.io.ByteArrayOutputStream

plugins {
    base
    id("maven-publish")
    id("java-library")
    id("crucible") // Assuming this is a custom plugin still available
}

buildscript {
    repositories {
        mavenCentral()
        // mavenLocal() // for testing plugin pre release
        maven {
            name = "juanmuscaria"
            url = uri("https://github.com/juanmuscaria/maven/raw/master")
        }
        maven {
            name = "forge"
            url = uri("https://maven.minecraftforge.net/")
        }
    }
    dependencies {
        classpath("com.anatawa12.forge:ForgeGradle:1.2-1.1.0")
    }
}

repositories {
    maven {
        name = "juanmuscaria"
        url = uri("https://github.com/juanmuscaria/maven/raw/master")
    }
    maven {
        name = "thermos"
        url = uri("https://github.com/juanmuscaria/maven/raw/master/ThermosLibs")
    }
    maven {
        name = "forge"
        url = uri("https://maven.minecraftforge.net/")
    }
    maven {
        name = "minecraft"
        url = uri("https://libraries.minecraft.net/")
    }
    maven {
        name = "gtnh"
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }
}

configure<io.github.cruciblemc.forgegradle.DevExtension> {
    version = "1.7.10"
    mcpVersion = "9.08"
    mainClass = "cpw.mods.fml.relauncher.ServerLaunchWrapper"
    tweakClass = "cpw.mods.fml.common.launcher.FMLTweaker"
    installerVersion = "1.4"

    // Repos used on the generated subprojects
    repos = arrayOf("https://github.com/juanmuscaria/maven/raw/master/ThermosLibs",
        "https://github.com/juanmuscaria/maven/raw/master",
        "https://maven.minecraftforge.net/",
        "https://oss.sonatype.org/content/repositories/snapshots/",
        "https://nexus.gtnewhorizons.com/repository/public/",
        "https://libraries.minecraft.net/")
}

extensions.configure<ExtraPropertiesExtension>("ext") {
    set("mcVersion", "1.7.10")
    set("forgeVersion", "1614")
    set("revision", "1.0.7")
}

group = "io.github.cruciblemc"

val crucibleVersion = "5.4" // TODO: Not hardcode
version = if (gitInfo("branch") != "master") {
    "${extra["mcVersion"]}-${gitInfo("branch")}-${gitInfo("hash")}"
} else {
    "${extra["mcVersion"]}-$crucibleVersion"
}

configurations {
    create("libraries")
}

dependencies {
    "libraries"("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.0.10") {
        exclude(group = "org.apache.logging.log4j")
    }
    "libraries"("org.apache.commons:commons-lang3:3.12.0")
    "libraries"("org.apache.commons:commons-compress:1.21")
    "libraries"("org.ow2.asm:asm:9.8")
    "libraries"("org.ow2.asm:asm-commons:9.8")
    "libraries"("org.ow2.asm:asm-tree:9.8")
    "libraries"("org.ow2.asm:asm-analysis:9.8")
    "libraries"("org.ow2.asm:asm-util:9.8")
    "libraries"("org.ow2.asm:asm-deprecated:7.1")
    "libraries"("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    "libraries"("javax.servlet:javax.servlet-api:4.0.1")
    "libraries"("com.sun.xml.bind:jaxb-impl:3.0.2")
    "libraries"("org.openjdk.nashorn:nashorn-core:15.4")
    "libraries"("it.unimi.dsi:fastutil:8.5.12")
    // ... continue with the rest of your long libraries list
}

tasks.register<Jar>("packageJavadoc") {
    dependsOn(":eclipse:cauldron:javadoc")
    archiveClassifier.set("javadoc")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.named<Jar>("jar").configure {
    manifest {
        attributes(
            mapOf(
                "Thermos-Git-Branch" to gitInfo("branch"),
                "Thermos-Git-Hash" to gitInfo("fullHash"),
                "Thermos-Group" to project.group,
                "Thermos-Channel" to project.name,
                "Thermos-Version" to project.version,
                "Thermos-Legacy" to true,
                "Implementation-Vendor" to "CrucibleMC Team",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Specification-Vendor" to "Bukkit Team",
                "Specification-Title" to "Bukkit",
                "Specification-Version" to "1.7.10-R0.1-SNAPSHOT",
                "Forge-Version" to "10.13.4.1614",
                "TweakClass" to "cpw.mods.fml.common.launcher.FMLTweaker",
                "Main-Class" to "cpw.mods.fml.relauncher.ServerLaunchWrapper",
                "Class-Path" to generateClasspath(),
                "Crucible-Libs" to generateMavenLibs()
            )
        )
    }
}

tasks.register<Zip>("packageLibraries") {
    archiveFileName.set("libraries.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    outputs.upToDateWhen { false }

    from(configurations.named("libraries").get().resolvedConfiguration.resolvedArtifacts.map { art ->
        val id = art.moduleVersion.id
        val intoPath = "${id.group.replace('.', '/')}/${id.name}/${id.version}/"
        zipTree(art.file).matching { }.let { copySpec ->
            //copySpec.into(intoPath)
        }
        art.file
    })

    group = "crucible"
    description = "Package all necessary libraries to run Crucible, in case the server cannot download them at runtime"
}

publishing {
    publications {
        create<MavenPublication>("crucible") {
            artifact(tasks.named("packageApi"))
            artifact(tasks.named("packageJavadoc"))
        }
    }
    repositories {
        maven {
            name = "filesystem"
            url = uri("${buildDir}/repo")
        }
    }
}

// Git helper
fun gitInfo(key: String): String {
    val cache = project.extra.properties.getOrPut("gitInfoCached") {
        if (file(".git").exists()) {
            mapOf(
                "hash" to runGit("log", "--format=%h", "-n", "1"),
                "fullHash" to runGit("log", "--format=%H", "-n", "1"),
                "branch" to runGit("symbolic-ref", "--short", "HEAD"),
                "message" to runGit("log", "--format=%B", "-n", "1")
            )
        } else {
            mapOf(
                "hash" to "NOT_A_GIT",
                "fullHash" to "NOT_A_GIT",
                "branch" to "NOT_A_GIT",
                "message" to "NOT_A_GIT"
            )
        }
    } as Map<*, *>
    return cache[key] as String
}

fun runGit(vararg args: String): String {
    val stdout = ByteArrayOutputStream()
    project.exec {
        commandLine("git", *args)
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

fun generateClasspath(): String =
    configurations["libraries"].resolvedConfiguration.resolvedArtifacts.joinToString(" ") { art ->
        val id = art.moduleVersion.id
        "libraries/${id.group.replace('.', '/')}/${id.name}/${id.version}/${id.name}-${id.version}.jar"
    }

fun generateMavenLibs(): String =
    configurations["libraries"].resolvedConfiguration.resolvedArtifacts.joinToString(" ") { art ->
        val id = art.moduleVersion.id
        "${id.group}:${id.name}:${id.version}"
    }
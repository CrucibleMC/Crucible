import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.publish.maven.MavenPublication
import io.github.cruciblemc.forgegradle.tasks.DelayedJar
import org.gradle.api.artifacts.ModuleVersionIdentifier
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
    repos = listOf("https://github.com/juanmuscaria/maven/raw/master/ThermosLibs",
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

val libraries by configurations.creating {
    isCanBeConsumed = false; isCanBeResolved = true;
}

// Libraries kept out of the jar's Class-Path, mapped to the lowest Java major that can load them.
// A jar the running JVM cannot read is not inert on the Class-Path, and the JVM reads that line
// before any of our code runs, so the only way to keep it away from an old JVM is to never name it
// there; DeferredLibraries attaches these at runtime once the Java version is known.
val deferredLibraries = mapOf("org.openjdk.nashorn:nashorn-core" to 11)

configurations["implementation"].extendsFrom(libraries)

dependencies {
    "libraries"("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.1.0") {
        exclude(group = "org.apache.logging.log4j")
    }
    "libraries"("com.google.code.gson:gson:2.14.0")
    "libraries"("org.apache.commons:commons-lang3:3.17.0")
    "libraries"("org.apache.commons:commons-compress:1.27.1")
    "libraries"("org.ow2.asm:asm:9.9.1")
    "libraries"("org.ow2.asm:asm-commons:9.9.1")
    "libraries"("org.ow2.asm:asm-tree:9.9.1")
    "libraries"("org.ow2.asm:asm-analysis:9.9.1")
    "libraries"("org.ow2.asm:asm-util:9.9.1")
    "libraries"("org.ow2.asm:asm-deprecated:7.1")
    "libraries"("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    "libraries"("javax.servlet:javax.servlet-api:4.0.1")
    "libraries"("com.sun.xml.bind:jaxb-impl:3.0.2")
    "libraries"("org.openjdk.nashorn:nashorn-core:15.4")
    "libraries"("it.unimi.dsi:fastutil:8.5.18")

    // Other libs
    "libraries"("commons-cli:commons-cli:1.3@jar")
    "libraries"("org.slf4j:slf4j-simple:1.6.2@jar")
    "libraries"("org.eclipse.jetty:jetty-servlet:9.0.3.v20130506@jar")
    "libraries"("commons-io:commons-io:2.18.0")
    "libraries"("net.sf.opencsv:opencsv:2.0@jar")
    "libraries"("com.beust:jcommander:1.30@jar")
    "libraries"("io.github.cruciblemc:launchwrapper:1.13@jar")
    "libraries"("com.typesafe.akka:akka-actor_2.11:2.3.3")
    "libraries"("com.typesafe:config:1.2.1")
    "libraries"("org.scala-lang:scala-actors-migration_2.11:1.1.0")
    "libraries"("org.scala-lang:scala-compiler:2.11.7")
    "libraries"("org.scala-lang.plugins:scala-continuations-library_2.11:1.0.2")
    "libraries"("org.scala-lang.plugins:scala-continuations-plugin_2.11.2:1.0.2")
    "libraries"("org.scala-lang:scala-library:2.11.7")
    "libraries"("org.scala-lang:scala-parser-combinators:2.11.0-M4")
    "libraries"("org.scala-lang:scala-reflect:2.11.7")
    "libraries"("org.scala-lang:scala-swing:2.11.0-M7")
    "libraries"("org.scala-lang:scala-xml:2.11.0-M4")
    "libraries"("net.sf.jopt-simple:jopt-simple:5.0.1")
    "libraries"("lzma:lzma:0.0.1")
    "libraries"("org.yaml:snakeyaml:1.9")
    "libraries"("commons-lang:commons-lang:2.6")
    "libraries"("org.avaje:ebean:2.7.3")
    "libraries"("jline:jline:2.6")
    "libraries"("net.md-5:SpecialSource:1.10.0")
    "libraries"("net.sourceforge.argo:argo:2.25")
    "libraries"("com.googlecode.json-simple:json-simple:1.1")
    "libraries"("org.xerial:sqlite-jdbc:3.7.2")
    "libraries"("mysql:mysql-connector-java:5.1.14")
    "libraries"("javax.persistence:persistence-api:1.0.2")
    "libraries"("pw.prok:KImagine:0.2.0@jar")
    "libraries"("org.apache.httpcomponents:httpclient:4.4.1")
    "libraries"("net.openhft:affinity:3.0.1")
    "libraries"("org.fusesource.jansi:jansi:1.11")
    "libraries"("com.koloboke:koloboke-impl-jdk8:1.0.0")
    "libraries"("java3d:vecmath:1.3.1")
    "libraries"("net.minecraft:server:1.7.10")
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
                "Crucible-Libs" to generateMavenLibs(),
                "Crucible-Deferred-Libs" to generateDeferredLibs()
            )
        )
    }
}

tasks.named<DelayedJar>("packageServer").configure {
    doFirst {
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
                    "Crucible-Libs" to generateMavenLibs(),
                    "Crucible-Deferred-Libs" to generateDeferredLibs()
                )
            )
        }
    }
}

tasks.register<Zip>("packageLibraries") {
    archiveFileName.set("libraries.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    outputs.upToDateWhen { false }

    // Laid out exactly like generateClasspath(), because that is what reads it back: the server
    // jar's own Class-Path resolves every library at
    // libraries/<group as path>/<name>/<version>/<name>-<version>.jar, relative to the server
    // directory. A flat archive resolves none of them, so the offline case this task exists to
    // cover ends with the server on an empty classpath, dying on the first class a library owns.
    configurations["libraries"].resolvedConfiguration.resolvedArtifacts.forEach { art ->
        val id = art.moduleVersion.id
        from(art.file) {
            into("libraries/${id.group.replace('.', '/')}/${id.name}/${id.version}")
            // A published file name can carry a classifier; the Class-Path entry never does.
            rename { "${id.name}-${id.version}.jar" }
        }
    }

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
                "branch" to runGit("rev-parse", "--abbrev-ref", "HEAD"),
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

fun isDeferred(id: ModuleVersionIdentifier): Boolean =
    deferredLibraries.containsKey("${id.group}:${id.name}")

fun generateClasspath(): String =
    configurations["libraries"].resolvedConfiguration.resolvedArtifacts
        .filterNot { isDeferred(it.moduleVersion.id) }
        .joinToString(" ") { art ->
            val id = art.moduleVersion.id
            "libraries/${id.group.replace('.', '/')}/${id.name}/${id.version}/${id.name}-${id.version}.jar"
        }

fun generateMavenLibs(): String =
    configurations["libraries"].resolvedConfiguration.resolvedArtifacts.joinToString(" ") { art ->
        val id = art.moduleVersion.id
        "${id.group}:${id.name}:${id.version}"
    }

// Every deferred library is still downloaded and verified like the rest - only the Class-Path skips
// it - so the same install serves both an old and a new JVM without fetching anything on the swap.
fun generateDeferredLibs(): String =
    configurations["libraries"].resolvedConfiguration.resolvedArtifacts
        .filter { isDeferred(it.moduleVersion.id) }
        .joinToString(" ") { art ->
            val id = art.moduleVersion.id
            "${id.group}:${id.name}:${id.version}:${deferredLibraries["${id.group}:${id.name}"]}"
        }
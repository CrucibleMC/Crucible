plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.cruciblemc"
version = "1.2-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net/") {
        name = "forge"
    }
}

dependencies {
    // TODO? figure a way to use the runtime dependencies of FG?
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-tree:9.6")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.opencsv:opencsv:5.7.0")
    implementation("com.cloudbees:diff4j:1.3")
    implementation("com.github.abrarsyed.jastyle:jAstyle:1.2")
    implementation("net.sf.trove4j:trove4j:2.1.0")
    implementation("com.github.jponge:lzma-java:1.3")
    implementation("com.nothome:javaxdelta:2.0.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.anatawa12.forge:SpecialSource:1.11.1")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("de.oceanlabs.mcp:RetroGuard:3.6.6")
    implementation("de.oceanlabs.mcp:mcinjector:3.2-SNAPSHOT")
    implementation("net.minecraftforge:Srg2Source:4.2.7")

    api("com.anatawa12.forge:ForgeGradle:1.2-1.1.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.github.tony19:named-regexp:0.2.3")

    // Java 9+ syntax
    annotationProcessor("com.github.bsideup.jabel:jabel-javac-plugin:0.4.2")
    compileOnly("com.github.bsideup.jabel:jabel-javac-plugin:0.4.2")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("compileJava").configure {
    this as JavaCompile
    sourceCompatibility = "17" // for the IDE support
    options.release.set(8)

    javaCompiler.set(
            javaToolchains.compilerFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
    )
}

gradlePlugin {
    plugins {
        create("crucible") {
            id = "crucible"
            implementationClass = "io.github.cruciblemc.forgegradle.CrucibleDevPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = base.archivesName.get()

            pom {
                name.set(project.base.archivesName.get())
                description.set("Gradle plugin for Crucible")
                url.set("https://github.com/CrucibleMC/CrucibleGradle")

                scm {
                    url.set("https://github.com/CrucibleMC/CrucibleGradle")
                    connection.set("scm:git:git://github.com/CrucibleMC/CrucibleGradle.git")
                    developerConnection.set("scm:git:git@github.com:CrucibleMC/CrucibleGradle.git")
                }

                issueManagement {
                    system.set("github")
                    url.set("https://github.com/CrucibleMC/CrucibleGradle/issues")
                }

                licenses {
                    license {
                        name.set("Lesser GNU Public License, Version 2.1")
                        url.set("https://www.gnu.org/licenses/lgpl-2.1.html")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("juanmuscaria")
                        name.set("juanmuscaria")
                    }
                }
            }
        }
        repositories {
            maven(buildDir.absolutePath + "/repo") {
                name = "filesystem"
            }
        }
    }
}

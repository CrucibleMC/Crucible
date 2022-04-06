![](https://img.shields.io/badge/Minecraft%20Forge-v10.13.4.1614-orange?style=flat-square)
![](https://img.shields.io/badge/Minecraft-1.7.10-orange?style=flat-square)
![](https://img.shields.io/badge/Bukkit--1.7.9--R0.3--SNAPSHOT-orange?style=flat-square)
![](https://img.shields.io/badge/Java%20JDK-v1.8-blue?style=flat-square)
![](https://img.shields.io/github/v/release/CrucibleMC/Crucible?color=sucess&style=flat-square)
![](https://img.shields.io/discord/682358465175355393?color=blue&label=Discord&logo=Discord&style=flat-square)
![Crucible](logo.png)
### What's Crucible?

Crucible is a continuation of [Thermos](https://github.com/CyberdyneCC/Thermos) as it has been discontinued.

Advantages over Thermos:
+ It is now possible to build. :)
+ Fixed several bugs.
+ Performance Improvements.
+ Updated some libraries.
+ Works with luckperms out of the box. No need to replace files!
+ TimingsV2 implemented.
+ You can see more changes in the [releases](https://github.com/CrucibleMC/Crucible/releases) changelog.

## Build Requirements
* Java 8u141 JDK or higher
* Linux (apparently the project breaks on Windows).
* `JAVA_HOME` defined on your OS

## Setup the Workspace
* Checkout project
  * You can use IDE or clone from console:
  `git clone https://github.com/CrucibleMC/Crucible.git`
* Creating the workspace
  * To create the workspace just run the command: `./gradlew setupCrucible`
  * To create the patches with the changes made just run: `./gradlew genPatches`
* Building
  * Before you can build you must first setup the workspace!
  * To build just run the command: `./gradlew jar`
  * All builds will be in `build/distributions`
* Updating sources
  * Update sources: `git pull origin master`
  * Recreate the workspace: `./gradlew clean setupCrucible`
## Know incompatibilities and bugs
* Some coremods may crash with `java.lang.VerifyError: Expecting a stackmap frame`, we still don't know what causes that, but adding -noverify to your JVM arguments seems to fix the problem.

## Useful links
+ [Spark](https://github.com/lucko/spark) "Spark is a performance profiling plugin based on sk89q's WarmRoast profiler."
+ [Grimoire](https://github.com/CrucibleMC/Grimoire) "A core mod that [Grimoire-Mixins](https://github.com/CrucibleMC/Grimoire-Mixins) Modules use to be loaded up."
+ [Discord](https://discord.gg/jWSTJ4d) "Our support Discord."

## Roadmap
You can check our Roadmap on our [wiki](https://github.com/CrucibleMC/Crucible/wiki/Roadmap).

[forge]: https://img.shields.io/badge/Minecraft%20Forge-v10.13.4.1614-green.svg "Minecraft Forge v10.13.4.1614"
[mc]: https://img.shields.io/badge/Minecraft-v1.7.10-green.svg "Minecraft 1.7.10"
[java]: https://img.shields.io/badge/Java%20JDK-v1.8-blue.svg "Java JDK 8"
[spigot]: https://img.shields.io/badge/Spigot-v1.7.10--R0.1--SNAPSHOT-lightgrey.svg "Spigot R0.1 Snapshot"

## Working Mods and Plugins

### Soon THESE BELLOW will be on the https://cruciblemc.github.io/

### ForgeRestrictor
Author: [KaiKikuchi](https://github.com/KaiKikuchi) <br>
Original GitHub URL: _Not available, Only on wayback machine [here](https://web.archive.org/web/20180611114227/https://github.com/KaiKikuchi/ForgeRestrictor)_ <br>
Download: [ForgeRestrictor-1.4-DEV-11.jar](https://cdn.discordapp.com/attachments/899002842847797258/899030688710418452/ForgeRestrictor-1.4-DEV-11.jar) _(last known working file uplaoded to discord server)_

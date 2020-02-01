![](https://img.shields.io/badge/Minecraft%20Forge-v10.13.4.1614-orange?style=flat-square)
![](https://img.shields.io/badge/Minecraft-1.7.10-orange?style=flat-square)
![](https://img.shields.io/badge/Spigot-1.7.10--R0.1--SNAPSHOT-orange?style=flat-square)
![](https://img.shields.io/badge/PaperMC-1.14.4--R0.1--SNAPSHOT-orange?style=flat-square)
![](https://img.shields.io/badge/CrucibleAPI-Not%20Implemented%20Yet-orange?style=flat-square)
![](https://img.shields.io/badge/Java%20JDK-v1.8-blue?style=flat-square)
![](https://img.shields.io/github/v/release/CrucibleMC/Crucible?color=sucess&style=flat-square)
![Crucible](logo.png)
### What's Crucible?

Crucible is a continuation of [Thermos](https://github.com/CyberdyneCC/Thermos) as it has been discontinued.

Advantages over Thermos:
+ It is now possible to build. :)
+ Fixed several bugs.
+ Performance Improvements.
+ Updated some libraries.
+ You can see more changes in the [releases](https://github.com/CrucibleMC/Crucible/releases) changelog.

## Build Requirements
* Java 8u101 JDK or higher
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
* Industrial Craft 2 has some problems with the crucible internal chunkmap, we have a fix via [Mixins](https://github.com/CrucibleMC/Grimoire-Mixins) using [Grimoire](https://github.com/CrucibleMC/Grimoire).
## Useful links
+ [Fakeplayer List](FakeplayerList.md) "A list of all mod fakeplayers."
+ [Spark](https://github.com/lucko/spark) "Spark is a performance profiling plugin based on sk89q's WarmRoast profiler."
+ [Grimoire](https://github.com/CrucibleMC/Grimoire) "A core mod that [Grimoire-Mixins](https://github.com/CrucibleMC/Grimoire-Mixins) Modules use to be loaded up."
## TODO List
- [ ] Create Crucible API.
- [X] Implement the PaperMC API.
- [ ] Implement the Glowstone API.
- [X] Rewrite most of Cauldron/Thermos code.
- [ ] Fix most remaining bugs and incompatibilities.
- [ ] Update the Spigot and Bukkit API.
- [X] Performance Improvements.
- [ ] Rewrite parts of the project build process for easier maintenance.
- [ ] Fixes for better support [Travertine](https://github.com/PaperMC/Travertine).
- [ ] Make the project compatible with Windows.
- [ ] Backport useful things.
- [ ] Isolate the PluginClassloader and create a special library injection and remapper.
- [ ] Add @Mod(bukkitplugin) Support.
- [ ] implement [Timings](https://github.com/PaperMC/Paper/blob/ver/1.14/Spigot-API-Patches/0004-Timings-v2.patch).

[forge]: https://img.shields.io/badge/Minecraft%20Forge-v10.13.4.1614-green.svg "Minecraft Forge v10.13.4.1614"
[mc]: https://img.shields.io/badge/Minecraft-v1.7.10-green.svg "Minecraft 1.7.10"
[java]: https://img.shields.io/badge/Java%20JDK-v1.8-blue.svg "Java JDK 8"
[spigot]: https://img.shields.io/badge/Spigot-v1.7.10--R0.1--SNAPSHOT-lightgrey.svg "Spigot R0.1 Snapshot"

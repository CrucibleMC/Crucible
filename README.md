![](https://img.shields.io/github/v/release/CrucibleMC/Crucible?color=sucess&style=flat-square)
![](https://img.shields.io/github/actions/workflow/status/CrucibleMC/Crucible/staging-build.yml?style=flat-square)
![](https://img.shields.io/discord/682358465175355393?color=blue&label=Discord&logo=Discord&style=flat-square)
![Crucible](logo.png)
### What's Crucible?

Crucible, a fork of [Thermos](https://github.com/CyberdyneCC/Thermos),
is a CraftBukkit and Forge server implementation for 1.7.10,
providing the ability to load both Forge mods and Bukkit plugins alongside each other.

We aim to close the gaps left by Thermos and extend the support for those still using 1.7.10 by adding support for long
broken mods and plugins and fix serious bugs as they appear.

Advantages over Thermos:
+ Working/stable build across systems
+ Several bugfixes
+ Performance improvements
+ Updated libraries for newer plugin support
* Implemented TimingsV2
* Java 8–21 supported (using an integrated version of [lwjgl3ify](https://github.com/GTNewHorizons/lwjgl3ify))
+ Backported Bukkit APIs (With some APIs requiring the companion mod [NecroTempus](https://github.com/CrucibleMC/NecroTempus))
+ You can see more changes in the [releases](https://github.com/CrucibleMC/Crucible/releases) changelog.

## Build Requirements
* Java 8 JDK
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
  * To build the distribution packages run the command: `./gradlew buildPackages`
  * The distribution package will be in `build/distributions`
* Updating sources
  * Update sources: `git pull origin master`
  * Recreate the workspace: `./gradlew clean setupCrucible`

## Useful links
+ [Crucible Documentation](https://cruciblemc.github.io/docs/) - Place for everything about crucible.
+ [Discord](https://discord.gg/jWSTJ4d) - Join our support discord if you need help with server setup, or if you just want to hang out.

## Credits
* [Thermos](https://github.com/CyberdyneCC/Thermos) - Original project
* [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse) - Several improvements over Bukkit
* [Paper](https://github.com/PaperMC/Paper) - Several improvements over Spigot
* [lwjgl3ify](https://github.com/GTNewHorizons/lwjgl3ify) - Java 9+ support

## Special Thanks To

### JetBrains
[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" alt="JetBrains Logo (Main) logo." width="100">](https://www.jetbrains.com)

For supporting Crucible development with access to their [Open Source License](https://jb.gg/OpenSourceSupport).
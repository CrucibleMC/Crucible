![](https://img.shields.io/badge/Minecraft%20Forge-v10.13.4.1614-orange?style=flat-square)
![](https://img.shields.io/badge/Minecraft-1.7.10-orange?style=flat-square)
![](https://img.shields.io/badge/Bukkit--1.7.9--R0.3--SNAPSHOT-orange?style=flat-square)
![](https://img.shields.io/badge/Java%20JDK-v1.8-blue?style=flat-square)
![](https://img.shields.io/github/v/release/CrucibleMC/Crucible?color=sucess&style=flat-square)
![](https://img.shields.io/github/actions/workflow/status/CrucibleMC/Crucible/staging-build.yml?style=flat-square)
![](https://img.shields.io/discord/682358465175355393?color=blue&label=Discord&logo=Discord&style=flat-square)
![Crucible](logo.png)
### What's Crucible?

Crucible is a [Thermos](https://github.com/CyberdyneCC/Thermos) with several improvements.
We aim to close the gaps left by Thermos and extend the support for those still on 1.7.10 by adding support for long
broken mods and plugins and fix serious bugs as they appear.

Advantages over Thermos:
+ Working/stable build across systems
+ Fixed several bugs
+ Performance Improvements
+ Updated some libraries
+ TimingsV2 implemented
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

## Roadmap
You can check our Roadmap on our [docs](https://cruciblemc.github.io/docs/crucible/roadmap).
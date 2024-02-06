# CrucibleGradle
Make ForgeGradle do more

## About
This is Crucible's extension over anatawa12's [ForgeGradle-1.2](https://github.com/anatawa12/ForgeGradle-1.2/).
Here we keep all necessary machinery for building Crucible.

This project still needs a lot of proper cleanup since it was more or less a copy/paste of old Cauldron dev plugin. 
A lot of tasks still does not cache properly, and we don't have much of an idea of what is going on certain parts of the
FG workflow.

This is basically the bare minimum to get Crucible to build on modern Gradle.

## TODO
* [ ] Document all tasks/add descriptions
* [ ] Fix caching issues
* [ ] Trim down unneeded tasks
* [ ] Clean up old code and deprecated usages
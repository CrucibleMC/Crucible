package io.github.crucible.bootstrap;

import cpw.mods.fml.common.launcher.FMLTweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;

public class CrucibleCoremodHook {
    // Too lazy for a coremod
    public static void coremodHandleLaunch(File mcDir, LaunchClassLoader classLoader, FMLTweaker tweaker) {
        classLoader.addClassLoaderExclusion("io.github.crucible.bootstrap.");
        try {
            // Ensure our config is loaded way before everything that may need it
            Class.forName("io.github.crucible.CrucibleConfigs", true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Lwjgl3ifyGlue.doCoremodWork(classLoader);
    }
}

package io.github.crucible;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import org.bukkit.plugin.Plugin;

import java.util.Properties;

public class Crucible {
    public static final String CRUCIBLE_VERSION;
    public static final int FORGE_BUILD_VERSION;
    public static final boolean IS_DEV_BUILD;
    static {
        String parsedVersion = "unknown";
        boolean parsedIsDevBuild = false;
        int forgeBuild = 0;
        try {
            Properties manifest = new Properties();
            manifest.load(Crucible.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
            parsedVersion = manifest.getProperty("Implementation-Version");
            parsedIsDevBuild = parsedVersion.contains("dev");
            Properties fmlversion = new Properties();
            fmlversion.load(Crucible.class.getResourceAsStream("/fmlversion.properties"));
            forgeBuild = Integer.parseInt(String.valueOf(fmlversion.getProperty(
                    "fmlbuild.build.number", "0")));
        } catch (Exception e) {
            FMLLog.severe("[Crucible] Unable to parse metadata.");
            e.printStackTrace();
        }
        CRUCIBLE_VERSION = parsedVersion;
        IS_DEV_BUILD = parsedIsDevBuild;
        FORGE_BUILD_VERSION = forgeBuild;
    }

    private Crucible() {
    }

    public static boolean isModPlugin(Plugin plugin) {
        return plugin.getClass().getClassLoader().equals(Loader.instance().getModClassLoader()) ||
                plugin.getClass().getClassLoader().equals(Crucible.class.getClassLoader());
    }
}

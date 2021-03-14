package io.github.crucible;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import org.bukkit.plugin.Plugin;

import java.net.URL;
import java.util.Enumeration;
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
            Enumeration<URL> resources = Crucible.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties manifest = new Properties();
                manifest.load(url.openStream());
                if (manifest.getProperty("Forge-Version") == null)
                    continue;
                parsedVersion = manifest.getProperty("Implementation-Version", parsedVersion);
                parsedIsDevBuild = parsedVersion.contains("dev");
                forgeBuild = Integer.parseInt(System.getProperty("thermos.forgeRevision", "0"));
                if (forgeBuild == 0) {
                    Properties fmlversion = new Properties();
                    fmlversion.load(Crucible.class.getResourceAsStream("/fmlversion.properties"));
                    forgeBuild = Integer.parseInt(String.valueOf(fmlversion.getProperty(
                            "fmlbuild.build.number", "0")));
                }
                break;
            }
        } catch (Exception e) {
            FMLLog.severe("[Crucible] Unable to parse metadata.");
            e.printStackTrace();
        }
        if (forgeBuild == 0)
            throw new RuntimeException("Unable to parse forge version");
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

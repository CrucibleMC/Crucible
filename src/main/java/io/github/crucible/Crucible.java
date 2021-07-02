package io.github.crucible;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import org.bukkit.plugin.Plugin;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class Crucible {
    public static final String CRUCIBLE_VERSION;
    public static final int FORGE_BUILD_VERSION;
    public static final boolean IS_DEV_BUILD;
    private static final Interner<String> pool = Interners.newWeakInterner();
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

    public static String deduplicate(String original) {
        return (CrucibleConfigs.configs.crucible_performance_deduplicateStrings && original != null) ? pool.intern(original) : original;
    }

    public static <T extends List<String>> T deduplicate (T list) {
        if (CrucibleConfigs.configs.crucible_performance_deduplicateStrings) {
            for (int i = 0; i < list.size(); i++) {
                list.set(i, deduplicate(list.get(i)));
            }
        }
        return list;
    }

    public static String[] deduplicate(String[] array) {
        if (CrucibleConfigs.configs.crucible_performance_deduplicateStrings) {
            for (int i = 0; i < array.length; i++) {
                array[i] = deduplicate(array[i]);
            }
        }
        return array;
    }
}

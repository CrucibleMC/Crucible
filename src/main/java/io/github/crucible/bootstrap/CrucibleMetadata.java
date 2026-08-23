package io.github.crucible.bootstrap;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class CrucibleMetadata {

    public static final String CRUCIBLE_VERSION;
    public static final int FORGE_BUILD_VERSION;
    public static final boolean IS_DEV_BUILD;
    /** Every shipped library, as {@code group:artifact:version} - what this server must have on disk. */
    public static final String[] NEEDED_LIBRARIES;
    /**
     * The shipped libraries the jar's {@code Class-Path} deliberately leaves out, as
     * {@code group:artifact:version:minimumJavaMajor}.
     *
     * @see DeferredLibraries
     */
    public static final String[] DEFERRED_LIBRARIES;
    public static final String NECRO_TEMPUS_REQUIRED = "This method requires Crucible NecroTempus to work, you can get it at https://github.com/CrucibleMC/NecroTempus.";

    static {
        String parsedVersion = "unknown";
        boolean parsedIsDevBuild = false;
        int forgeBuild = 0;
        String[] libraries = new String[0];
        String[] deferred = new String[0];
        try {
            Enumeration<URL> resources = CrucibleMetadata.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Manifest manifest = new Manifest();
                manifest.read(url.openStream());
                Attributes attributes = manifest.getMainAttributes();
                if (attributes.getValue("Forge-Version") == null)
                    continue;
                parsedVersion = Optional.ofNullable(attributes.getValue("Implementation-Version")).orElse(parsedVersion);
                parsedIsDevBuild = parsedVersion.contains("dev");
                forgeBuild = Integer.parseInt(System.getProperty("thermos.forgeRevision", "0"));
                libraries = splitList(attributes.getValue("Crucible-Libs"));
                deferred = splitList(attributes.getValue("Crucible-Deferred-Libs"));
                if (forgeBuild == 0) {
                    Properties fmlversion = new Properties();
                    fmlversion.load(CrucibleMetadata.class.getResourceAsStream("/fmlversion.properties"));
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
        NEEDED_LIBRARIES = libraries;
        DEFERRED_LIBRARIES = deferred;
    }

    /** Reads a space separated manifest attribute; an absent or empty one yields no entries. */
    private static String[] splitList(String attribute) {
        if (attribute == null) {
            return new String[0];
        }
        String flattened = attribute.replace("\n", "").trim();
        return flattened.isEmpty() ? new String[0] : flattened.split(" ");
    }

    private CrucibleMetadata() {
    }
}

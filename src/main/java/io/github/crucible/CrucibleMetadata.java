package io.github.crucible;

import cpw.mods.fml.common.FMLLog;

import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class CrucibleMetadata {

    public static final String CRUCIBLE_VERSION;
    public static final String[] NEEDED_LIBRARIES;

    static {
        String parsedVersion = "unknown";
        String[] libraries = new String[0];
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
                parsedVersion = Optional.ofNullable(attributes.getValue("Implementation-Version")).orElse(parsedVersion) ;
                libraries = attributes.getValue("Crucible-Libs").replace("\n","").split(" ");
                break;
            }
        } catch (Exception e) {
            FMLLog.severe("[Crucible] Unable to parse metadata.");
            e.printStackTrace();
        }
        CRUCIBLE_VERSION = parsedVersion;
        NEEDED_LIBRARIES = libraries;
    }

    private CrucibleMetadata() {
    }
}

package me.eigenraven.lwjgl3ify.api;

import io.github.crucible.CrucibleConfigs;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Proxy everything to Crucible
 * TODO: perhaps we need to ensure this class is loaded by us?
 */
public class ConfigUtils {

    public ConfigUtils(Logger logger) {
        // NO-OP
    }

    public boolean isLwjgl3ifyLoaded() {
        return true;
    }

    public Set<String> getExtensibleEnums() {
        return new HashSet<>(CrucibleConfigs.configs.lwjgl3ify_extensibleEnums);
    }

    public void addExtensibleEnum(String className) {
        CrucibleConfigs.configs.lwjgl3ify_extensibleEnums.add(className);
    }

    public boolean isConfigLoaded() {
        return true;
    }
}

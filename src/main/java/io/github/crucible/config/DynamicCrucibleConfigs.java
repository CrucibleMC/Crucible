package io.github.crucible.config;

import io.github.crucible.CrucibleConfigs;

import java.io.File;

public class DynamicCrucibleConfigs {
    public static final DynamicCrucibleConfigs configs = new DynamicCrucibleConfigs();
    private static final File configDir = new File("config" + File.separator + "crucible");
    private static final File pluginConfigs = new File(configDir, "plugin-tweaks");
    public static final PluginConfig defaultPluginConfig;
    static {
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new IllegalStateException("Unable to create " + configDir.getAbsolutePath());
        }
        if (configDir.isFile())
            throw new IllegalStateException(configDir.getAbsolutePath() + " is a file");

        if (!pluginConfigs.exists() && !pluginConfigs.mkdirs()) {
            throw new IllegalStateException("Unable to create " + pluginConfigs.getAbsolutePath());
        }
        if (pluginConfigs.isFile())
            throw new IllegalStateException(pluginConfigs.getAbsolutePath() + " is a file");
        defaultPluginConfig = new PluginConfig(new File(pluginConfigs, "default.yml"), true);
    }
    private DynamicCrucibleConfigs() {

    }

    public PluginConfig getPluginConfig(String name){
        File pluginConfig = new File(pluginConfigs, name + ".yml");
        if (pluginConfig.isFile() || CrucibleConfigs.configs.crucible_tweaks_generatePluginTweakOverwrites) {
            return new PluginConfig(pluginConfig, false);
        } else  {
            return defaultPluginConfig;
        }
    }

}

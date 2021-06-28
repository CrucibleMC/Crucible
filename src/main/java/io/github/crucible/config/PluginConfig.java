package io.github.crucible.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;

import java.io.File;

public class PluginConfig extends YamlConfig {

    @Comment("Floods your console with detailed information about plugin class loading.")
    public boolean classloader_debug = false;
    @Comment("If true it will use our enhanced classloader remapping and fixes for plugins. Setting to false will defaults to bukkit's original behavior and may break most plugins.")
    public boolean classloader_custom = true;
    public boolean classloader_remap_NMS1710 = true;
    public boolean classloader_remap_OBC1710 = true;
    public boolean classloader_remap_NMS179 = true;
    public boolean classloader_remap_OBC179 = true;
    public boolean classloader_remap_NMS172 = true;
    public boolean classloader_remap_OBC172 = true;
    public boolean classloader_remap_NMS164 = true;
    public boolean classloader_remap_OBC164 = true;
    public boolean classloader_remap_NMS152 = true;
    public boolean classloader_remap_OBC152 = true;
    public String classloader_remap_NMSPre = "";
    public boolean classloader_remap_OBCPre = false;
    public boolean classloader_remap_reflectFields = true;
    public boolean classloader_remap_reflectClass = true;
    @Comment("Unused cauldron configuration, it does nothing.")
    public boolean classloader_remap_allowFuture = false;
    public boolean classloader_globalInherit = true;
    public boolean classloader_pluginInherit = true;

    public PluginConfig(File myFile, boolean defaultFile) {
        CONFIG_MODE = ConfigMode.PATH_BY_UNDERSCORE;
        CONFIG_FILE = myFile;
        if (defaultFile) {
            CONFIG_HEADER = new String[]{"Default plugin tweaking file",
                    "This file will apply to all plugins unless they have a specific overwrite.",
                    "To overwrite a specific plugin tweak create a new file alongside this file named as <plugin id>.yml and paste the contents of this file there (or leave it empty to be auto generated during plugin loading).",
                    "Need some help? Join our discord https://discord.gg/jWSTJ4d"};
        } else {
            CONFIG_HEADER = new String[]{"Plugin specific tweaking file",
                    "Here you can fine tune your plugin loading for plugin specific fixes.",
                    "Need some help? Join our discord https://discord.gg/jWSTJ4d"};
        }

        try {
            init();
            save(); //Update old configs.
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addComments() {
        addComment("classloader.remap", "Here you can tweak some remapping behaviors.");
        addComment("classloader.remap", "Most of the time you will not need to change anything here, if some plugin is broken but can be fixed by changing some setting here open an issue on our github for it to be integrated in future crucible releases!");
    }

    public void save() {
        try {
            super.save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}

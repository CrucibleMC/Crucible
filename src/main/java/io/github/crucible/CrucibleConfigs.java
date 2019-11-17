package io.github.crucible;

import java.io.File;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;

public class CrucibleConfigs extends YamlConfig {
    public static final CrucibleConfigs configs = new CrucibleConfigs();
    
    @Comment("Hide your plugins from /plugins")
    public boolean hidePluginList = true;
    
    private CrucibleConfigs() {
        CONFIG_FILE = new File("Crucible.yml");
        CONFIG_MODE = ConfigMode.PATH_BY_UNDERSCORE;
        try
        {
            init();
        }
        catch (InvalidConfigurationException e)
        {
            e.printStackTrace();
            save();
        }
    }
    
    public void save() {
        try {
            super.save();
        }
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}

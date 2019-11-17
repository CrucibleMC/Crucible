package io.github.crucible;

import java.io.File;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;

public class CrucibleConfigs extends YamlConfig {
    public static final CrucibleConfigs configs = new CrucibleConfigs();
    
    @Comment("Enable Thermos command")
    public boolean thermos_commandEnable = true;
    
    @Comment("Set the OP command to only be allowed to run in console")
    public boolean thermos_opConsoleOnly = false;
    
    @Comment("Log material injection event")
    public boolean thermos_logging_materialInjection = false;
    
    @Comment("Print client's mod list during attempt to join")
    public boolean thermos_logging_clientModList = true;
    
    @Comment("Allow nether portals in dimensions besides overworld")
    public boolean thermos_allowNetherPortal = false;
    
    @Comment("Hide your plugins from /plugins")
    public boolean crucible_hidePluginList = true;
    
    private CrucibleConfigs() 
    {
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
    
    public void save() 
    {
        try 
        {
            super.save();
        }
        catch (InvalidConfigurationException e) 
        {
            e.printStackTrace();
        }
    }
}

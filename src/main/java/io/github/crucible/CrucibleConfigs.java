package io.github.crucible;

import java.io.File;
import java.time.ZoneId;
import java.util.TimeZone;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;

public class CrucibleConfigs extends YamlConfig {
    public static final CrucibleConfigs configs = new CrucibleConfigs();
    
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
    
    @Comment("Log stub calls.")
    public boolean crucible_logging_logStubs = false;
    
    @Comment("Log Material injections.")
    public boolean crucible_logging_logMaterialInjection = false;
    
    @Comment("Dump all item registry to <minecraftDir>/itemStackRegistry.csv.")
    public boolean crucible_logging_dumpRegistry = false;
    
    @Comment("Attempts to reduce console spam by removing \"useless\" logs.")
    public boolean crucible_logging_reduceSpam = false;
    
    @Comment("Force a specific timezone.")
    public boolean crucible_vmTimeZone_forceTimeZone = false;
    
    @Comment("The timezone id to set.")
    public String crucible_vmTimeZone_timeZoneId = TimeZone.getDefault().getID();
    
    private CrucibleConfigs() {
        CONFIG_FILE = new File("Crucible.yml");
        CONFIG_MODE = ConfigMode.PATH_BY_UNDERSCORE;
        
        try {
            init();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            save();
        }
        
        if (crucible_vmTimeZone_forceTimeZone) {
            try {
                String timezone = ZoneId.of(crucible_vmTimeZone_timeZoneId).getId();
                TimeZone.setDefault(TimeZone.getTimeZone(timezone));
                System.setProperty("user.timezone", timezone);
            } catch (Exception e) {
                System.out.println("[Crucible] Invalid timezone id:" + crucible_vmTimeZone_timeZoneId);
                crucible_vmTimeZone_timeZoneId = TimeZone.getDefault().getID();
            }
        }
    }
    
    public void save() {
        try {
            super.save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
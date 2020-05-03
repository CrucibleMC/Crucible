package io.github.crucible;

import co.aikar.timings.Timings;
import co.aikar.timings.TimingsManager;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;

import java.io.File;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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

    @Comment("If true, all entities and tiles in forced chunks will be ticked even without nearby players, experimental, can cause problems!!!!")
    public boolean crucible_tickHandler_forcedChunkTick = false;

    @Comment("Sets the server max tps, experimental, can cause problems!")
    public int crucible_tickHandler_serverTickRate = 20;

    @Comment("Sets the server max tick time, experimental, can cause problems!")
    public int crucible_tickHandler_serverTickTime = 1000000000;

    @Comment("List of dimension IDs that should never be unloaded.")
    public List<Integer> crucible_misc_worldUnloadBlacklist = Collections.singletonList(-73);

    @Comment("Let timings be turned on since the server statup!")
    public boolean timings_enabledSinceServerStartup = false;

    @Comment("Make timings Verbose! (http://tinyurl.com/wtf-is-verbose)")
    public boolean timings_verbose = true;

    @Comment("Make timings Utra-Verbose! (Needs 'timings_verbonse=true' - This cause cause LAG, and depending on how many tiles, will not even work! Don't use always!")
    public boolean timings_ultra_verbose = true;

    public boolean timings_serverNamePrivacy = false;

    public List<String> timings_hiddenConfigEntries = Arrays.asList("database", "settings.bungeecord-addresses");

    public int timings_historyInterval = 300;

    public int timings_historyLength = 3600;

    public String timings_serverName = "Crucible Server";

    private CrucibleConfigs() {
        CONFIG_FILE = new File("Crucible.yml");
        CONFIG_MODE = ConfigMode.PATH_BY_UNDERSCORE;

        try {
            init();
            save(); //Update old configs.
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            save(); //Replace broken configs.
        }

        if (crucible_vmTimeZone_forceTimeZone) {
            try {
                String timezone = ZoneId.of(crucible_vmTimeZone_timeZoneId).getId();
                TimeZone.setDefault(TimeZone.getTimeZone(timezone));
                System.setProperty("user.timezone", timezone);
            } catch (Exception e) {
                System.out.println("[Crucible] Invalid timezone id:" + crucible_vmTimeZone_timeZoneId);
                crucible_vmTimeZone_timeZoneId = TimeZone.getDefault().getID();
                save(); //Replace old timezone id.
            }
        }
        timings();
    }

    public void save() {
        try {
            super.save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public int getTickTime() {
        return crucible_tickHandler_serverTickTime / crucible_tickHandler_serverTickRate;
    }

    private void timings() {
        TimingsManager.privacy = timings_serverNamePrivacy;
        TimingsManager.hiddenConfigs = timings_hiddenConfigEntries;
        Timings.setVerboseTimingsEnabled(timings_verbose);
        Timings.setHistoryInterval(timings_historyInterval * 20);
        Timings.setHistoryLength(timings_historyLength * 20);
        Timings.setTimingsEnabled(timings_enabledSinceServerStartup);
    }
}

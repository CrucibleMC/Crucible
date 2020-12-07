package io.github.crucible;

import co.aikar.timings.Timings;
import co.aikar.timings.TimingsManager;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CrucibleConfigs extends YamlConfig {
    public static final CrucibleConfigs configs = new CrucibleConfigs();

    @Comment("Dumps all materials with their corresponding id's")
    public boolean cauldron_settings_dumpMaterials = false;

    @Comment("Log worlds that appear to be leaking (buggy)")
    public boolean cauldron_logging_worldLeakDebug = false;

    @Comment("Log when chunks are loaded (dev)")
    public boolean cauldron_logging_chunkLoad = false;

    @Comment("Log when chunks are unloaded (dev)")
    public boolean cauldron_logging_chunkUnload = false;

    @Comment("Log when living entities are spawned (dev)")
    public boolean cauldron_logging_entitySpawn = false;

    @Comment("Log when living entities are despawned (dev)")
    public boolean cauldron_logging_entityDespawn = false;

    @Comment("Add stack traces to dev logging")
    public boolean cauldron_logging_logWithStackTraces = false;

    @Comment("Dump chunks in the event of a deadlock (helps to debug the deadlock)")
    public boolean cauldron_logging_dumpChunksOnDeadlock = false;

    @Comment("Dump the heap in the event of a deadlock (helps to debug the deadlock)")
    public boolean cauldron_logging_dumpHeapOnDeadlock = false;

    @Comment("Dump the the server thread on deadlock warning (delps to debug the deadlock)")
    public boolean cauldron_logging_dumpThreadsOnWarn = false;

    @Comment("Whether to log entity collision/count checks")
    public boolean cauldron_logging_entityCollisionChecks = false;

    @Comment("Whether to log entity removals due to speed")
    public boolean cauldron_logging_entitySpeedRemoval = false;

    @Comment("Number of colliding entities in one spot before logging a warning. Set to 0 to disable")
    public int cauldron_logging_largeCollisionWarnSize = 200;

    @Comment("Set true to enable debuggin user's login process")
    public boolean cauldron_logging_userLogin = false;

    @Comment("Set the OP command to only be allowed to run in console")
    public boolean thermos_opConsoleOnly = false;

    @Comment("Log material injection event")
    public boolean thermos_logging_materialInjection = false;

    @Comment("Print client's mod list during attempt to join")
    public boolean thermos_logging_clientModList = true;

    @Comment("Allow nether portals in dimensions besides overworld")
    public boolean thermos_allowNetherPortal = false;

    @Comment("Enable Oversized Chunk to be saved")
    public boolean crucible_enableOversizedChunk = true;

    @Comment("Size of cached chunk")
    public int crucible_chunkCacheSize = 256;

    @Comment("Log Material injections.")
    public boolean crucible_logging_logMaterialInjection = false;

    @Comment("Attempts to reduce console spam by removing \"useless\" logs.")
    public boolean crucible_logging_reduceSpam = false;

    @Comment("Sets the server max tps, experimental, can cause problems!")
    public int crucible_tickHandler_serverTickRate = 20;

    @Comment("Sets the server max tick time, experimental, can cause problems!")
    public int crucible_tickHandler_serverTickTime = 1000000000;

    @Comment("Let timings be turned on since the server statup!")
    public boolean timings_enabledSinceServerStartup = false;

    @Comment("Make timings Verbose! (http://tinyurl.com/wtf-is-verbose)")
    public boolean timings_verbose = false;

    @Comment("Make timings Utra-Verbose! (Needs 'timings_verbose=true') This can cause LAG, and depending on how many tiles loaded on your server, will not even work! Don't use always!")
    public boolean timings_ultraverbose_enabled = false;

    @Comment("Only tiles that cost more than this limiar of time in nano-seconds will be sent to timings paste. One tick has 50000 nano-seconds, so 2500 means 5% o the tick!")
    public int timings_ultraverbose_limiar = 2500;

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
        }
        configureTimings();
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

    private void configureTimings() {
        TimingsManager.privacy = timings_serverNamePrivacy;
        TimingsManager.hiddenConfigs = timings_hiddenConfigEntries;
        Timings.setVerboseTimingsEnabled(timings_verbose);
        Timings.setHistoryInterval(timings_historyInterval * 20);
        Timings.setHistoryLength(timings_historyLength * 20);
        Timings.setTimingsEnabled(timings_enabledSinceServerStartup);
    }
}

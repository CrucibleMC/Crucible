package io.github.crucible;

import co.aikar.timings.Timings;
import co.aikar.timings.TimingsManager;
import net.cubespace.Yamler.Config.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CrucibleConfigs extends YamlConfig {
    public static final CrucibleConfigs configs = new CrucibleConfigs();

    @Comment("Dumps all materials with their corresponding id's")
    public boolean cauldron_settings_dumpMaterials = false;

    @Comment("Forces Chunk Loading on 'Provide' requests (speedup for mods that don't check if a chunk is loaded)")
    public boolean cauldron_settings_loadChunkOnRequest = true;

    @Comment("Forces Chunk Loading during Forge Server Tick events")
    public boolean cauldron_settings_loadChunkOnForgeTick = false;

    @Comment("Removes a living entity that exceeds the max bounding box size.")
    public boolean cauldron_settings_checkEntityBoundingBoxes = true;

    @Comment("Removes any entity that exceeds max speed.")
    public boolean cauldron_settings_checkEntityMaxSpeeds = false;

    @Comment("Max size of an entity's bounding box before removing it (either being too large or bugged and 'moving' too fast)")
    public int cauldron_settings_largeBoundingBoxLogSize = 1000;

    @Comment("Square of the max speed of an entity before removing it")
    public int cauldron_settings_entityMaxSpeed = 100;

    @Comment("Grace period of no-ticks before unload")
    public int cauldron_settings_chunkGCGracePeriod = 0;

    @Comment("Vanilla water source behavior - is infinite")
    public boolean cauldron_settings_infiniteWaterSource = true;

    @Comment("Lava behaves like vanilla water when source block is removed")
    public boolean cauldron_settings_flowingLavaDecay = false;

    @Comment("TNT ability to push other entities (including other TNTs)")
    public boolean cauldron_settings_allowTntPushing = true;

    @Comment("How many players will visible in the tab list (negative to use server's max players)")
    public int cauldron_settings_maxPlayersVisible = -1;

    @Comment("Instead of DIM##, use the world name prescribed by the mod! Be careful with this one, could create incompatibilities with existing setups!")
    public boolean cauldron_settings_useWorldRealNames = false;

    @Comment("How many milliseconds the server must ignore before trying repeater updates")
    public int cauldron_optimization_redstoneRepeaterUpdateSpeed = -1;

    @Comment("How many milliseconds the server must ignore before trying redstone torch updates")
    public int cauldron_optimization_redstoneTorchUpdateSpeed = -1;

    @Comment("Whether to enable affinity locking. Very technical usage, recommended for dedicated hosts only. Ask on Discord or GitHub for info on how to set this up properly.")
    public boolean cauldron_optimization_affinityLocking = false;

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

    @Comment("Set true to enable Java's thread contention monitoring for thread dumps")
    public boolean cauldron_debug_enableThreadContentionMonitoring = false;

    //TODO: Deprecate this option and change to something like <modid:item:meta>
    @Comment("Contains Block IDs that you want to NEVER exist in the world i.e. world anchors (just in case) (e.g. instantRemoval: 1,93,56,24)")
    public List<Integer> cauldron_protection_instantRemoval = Collections.emptyList();

    //TODO: Deprecate this option and let a plugin do the work of filtering commands.
    @Comment("Contains commands you want to block from being used in-game, you must also include command aliases (e.g. blockedCommands: /op,/deop,/stop,/restart .")
    public List<String> cauldron_protection_blockedCommands = Collections.emptyList();

    //TODO: Deprecate this option and let a plugin do the work of filtering commands.
    @Comment("Don't allow commands of the format plugin:cmd, the plugin: will be removed (recommended to keep at true)")
    public boolean cauldron_protection_noFallbackAlias = true;

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

    @Comment("List of world names where the usage of modded itens and blocks will be disabled for ")
    public List<String> crucible_protectedWorld = Collections.singletonList("spawn");

    @Comment("List of numeric item IDs for modded items that can be used in protected worlds")
    public List<Integer> crucible_protectedWorldWhitelist = Collections.emptyList();

    @Comment("Invert the protection whitelist and use it as a blacklist.")
    public boolean crucible_protectedWorldWhitelistInvert = false;

    @Comment("Attempts to reduce console spam by removing \"useless\" logs.")
    public boolean crucible_logging_reduceSpam = false;

    @Comment("Sets the server max tps, it will break plugins and other things that requires a normal tickrate!")
    public int crucible_tickHandler_serverTickRate = 20;

    @Comment("Sets the server max tick time, it will break plugins and other things that requires a normal tickrate!")
    public int crucible_tickHandler_serverTickTime = 1000000000;

    @Comment("If true, crucible will try to limit the ticking of tiles to prevent massive server lag.")
    public boolean crucible_tickHandler_tickSkip = false;

    @Comment("Maximum time a region can spend ticking in ms. When a region reaches this threshold all other tiles will be skipped.")
    public int crucible_tickHandler_regionMeanTime = 3;

    @Comment("The minimum time the server must be spending between ticks to enable the tick skip. Value must be in ms")
    public int crucible_tickHandler_meantimeThreshold = 40;

    @Comment("Tries to free up memory for long running servers by trimming arrays and cleaning up unused things.")
    public boolean crucible_performance_cleanUpTask = true;

    @Comment("Delay in ticks between each cleanup.")
    public int crucible_performance_cleanUpTaskDelay = 36000;

    @Comments({"Allow Crucible to serialize NBT of ItemStacks on Bukkit YML Serialization!",
            "This might help some plugins work properly with Modded Items when they have important NBT data.",
            "This might as well break some other plugins that suppose there is no NBT Data being loaded on the default Bukkit deserialization"
    })
    public boolean crucible_itemStackNBTSerialize = true;

    @Comments({"Removes some restrictions and safety checks, we will not offer support for this setting and it may cause problems.",
            "Use it at your own risk!",
            "Currently disabled checks by this:",
            " * Server Icon max size check"})
    public boolean crucible_unsafe = false;

    @Comment("Prevents grass tick from loading Chunks!")
    public boolean crucible_noGrassChunkLoading = true;

    @Comments({"Let you change what timings frontend to use.",
            "Available frontends:",
            " - https://timin.gs/",
            " - http://timings.aikar.co/"
    })
    public String timings_url = "https://timin.gs/";

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

    @Override
    public void reload() {
        try {
            super.reload();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    //Mimics cauldron toggle behavior setting all fields fields that are not updated with the configs with the new values.
    public void reapplyConfigs() {
        for (WorldServer world : MinecraftServer.getServer().worlds) {
            world.theChunkProviderServer.loadChunkOnProvideRequest = cauldron_settings_loadChunkOnRequest;
        }

        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        if (mbean.isThreadContentionMonitoringSupported())
            mbean.setThreadContentionMonitoringEnabled(cauldron_debug_enableThreadContentionMonitoring);
        else
            CrucibleModContainer.logger.warn("Thread monitoring is not supported!");
    }
}

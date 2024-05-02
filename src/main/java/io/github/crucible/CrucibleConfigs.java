package io.github.crucible;

import io.github.crucible.bootstrap.Lwjgl3ifyGlue;
import io.github.crucible.util.config.*;

import java.io.File;
import java.util.ArrayList;
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

    @Comment("Dump the the server thread on deadlock warning (helps to debug the deadlock)")
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

    @Comments({"Enable Oversized Chunk to be saved",
            "What is an oversized chunk?",
            " - Oversized chunks are chunks over 1 mb (exceeding the 255 sections limit) which by default is just",
            "   discarted by vanilla mc rolling back the chunk to the previous valid disk version.",
            "Why is it enabled by default?",
            " - Oversized chunks are abused by dupers to possibly damage your server and economy, having it enabled by default",
            "   will prevent the headache of having to deal with duped items later.",
            "   It also might save some player's bases with a big applied energistics system"})
    public boolean crucible_enableOversizedChunk = true;

    @Comments({"Warn about Oversized Chunks",
            "Log in the console when an oversized chunk is saved helping you to find it later."})
    public boolean crucible_warnOversizedChunk = true;

    @Comment("Size of cached chunk")
    public int crucible_chunkCacheSize = 256;

    @Comment("Log Material injections.")
    public boolean crucible_logging_logMaterialInjection = false;

    @Comments({"Log the plugin/caller as prefix on System.out usages, no more mysterious console usages.",
            "Be warned that looking up the caller can have a noticeable performance impact."})
    public boolean crucible_logging_logStdOutCaller = false;

    @Comment("Dump packet information whenever packet processing of a player takes too long.")
    public boolean crucible_logging_packetTimeout = false;

    @Comment("Maximum time (in milliseconds) a packet can take before getting dumped.")
    public long crucible_logging_packetTimeoutMs = 500;

    @Comment("List of world names where the usage of modded itens and blocks will be disabled for ")
    public List<String> crucible_protectedWorld = Collections.singletonList("spawn");

    @Comment("List of numeric item IDs for modded items that can be used in protected worlds")
    public List<Integer> crucible_protectedWorldWhitelist = Collections.emptyList();

    @Comment("Invert the protection whitelist and use it as a blacklist.")
    public boolean crucible_protectedWorldWhitelistInvert = false;

    @Comments({"Attempts to reduce console spam by removing \"useless\" logs.",
            "What is removed?",
            " - \"The mcmod.info file in modfile cannot be parsed as valid JSON. It will be ignored\" spam",
            " - FileNotFoundException spam when some core user list (like whitelist.json) does not exist."})
    public boolean crucible_logging_reduceSpam = false;

    @Comment("Sets the server max tps, it will break plugins and other things that requires a normal tickrate!")
    public int crucible_tickHandler_serverTickRate = 20;

    @Comment("Sets the server max tick time, it will break plugins and other things that requires a normal tickrate!")
    public int crucible_tickHandler_serverTickTime = 1000000000;

    @Comments({"Removes some restrictions and safety checks, we will not offer support for this setting and it may cause problems.",
            "Use it at your own risk!",
            "Currently disabled checks by this:",
            " * Server Icon max size check"})
    public boolean crucible_unsafe = false;

    @Comment("Prevents grass tick from loading Chunks!")
    public boolean crucible_noGrassChunkLoading = true;

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

    @Comment("Enums to make extensible at runtime")
    public List<String> lwjgl3ify_extensibleEnums = new ArrayList<>(Arrays.asList(Lwjgl3ifyGlue.DEFAULT_EXTENSIBLE_ENUMS));

    private CrucibleConfigs() {
        CONFIG_FILE = new File("Crucible.yml");
        CONFIG_MODE = ConfigMode.PATH_BY_UNDERSCORE;

        try {
            init();
            save(); //Update old configs.
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
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

    @Override
    public void reload() {
        try {
            super.reload();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}

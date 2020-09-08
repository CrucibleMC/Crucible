package io.github.crucible;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.YamlConfig;

import java.io.File;
import java.util.Collections;
import java.util.List;

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

    @Comment("Log Material injections.")
    public boolean crucible_logging_logMaterialInjection = false;

    @Comment("Attempts to reduce console spam by removing \"useless\" logs.")
    public boolean crucible_logging_reduceSpam = false;

    @Comment("If true, all entities and tiles in forced chunks will be ticked even without nearby players, experimental, it will cause a lot of lag!")
    public boolean crucible_tickHandler_forcedChunkTick = false;

    @Comment("Sets the server max tps, experimental, can cause problems!")
    public int crucible_tickHandler_serverTickRate = 20;

    @Comment("Sets the server max tick time, experimental, can cause problems!")
    public int crucible_tickHandler_serverTickTime = 1000000000;

    @Comment("List of dimension IDs that should never be unloaded.")
    public List<Integer> crucible_misc_worldUnloadBlacklist = Collections.singletonList(0);

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
}

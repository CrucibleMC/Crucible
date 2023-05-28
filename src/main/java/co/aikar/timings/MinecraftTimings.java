package co.aikar.timings;

import com.google.common.collect.MapMaker;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import org.bukkit.craftbukkit.v1_7_R4.scheduler.CraftTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

// TODO: Re-implement missing timers
public final class MinecraftTimings {

    public static final Timing serverOversleep = Timings.ofSafe("Server Oversleep");
    public static final Timing playerListTimer = Timings.ofSafe("Player List");
    public static final Timing commandFunctionsTimer = Timings.ofSafe("Command Functions");
    public static final Timing connectionTimer = Timings.ofSafe("Connection Handler");
    public static final Timing tickablesTimer = Timings.ofSafe("Tickables");
    public static final Timing minecraftSchedulerTimer = Timings.ofSafe("Minecraft Scheduler");
    public static final Timing bukkitSchedulerTimer = Timings.ofSafe("Bukkit Scheduler");
    public static final Timing bukkitSchedulerPendingTimer = Timings.ofSafe("Bukkit Scheduler - Pending");
    public static final Timing bukkitSchedulerFinishTimer = Timings.ofSafe("Bukkit Scheduler - Finishing");
    public static final Timing chunkIOTickTimer = Timings.ofSafe("ChunkIOTick");
    public static final Timing timeUpdateTimer = Timings.ofSafe("Time Update");
    public static final Timing serverCommandTimer = Timings.ofSafe("Server Command");
    public static final Timing savePlayers = Timings.ofSafe("Save Players");

    public static final Timing tickEntityTimer = Timings.ofSafe("## tickEntity");
    public static final Timing tickTileEntityTimer = Timings.ofSafe("## tickTileEntity");
    public static final Timing packetProcessTimer = Timings.ofSafe("## Packet Processing");
    public static final Timing scheduledBlocksTimer = Timings.ofSafe("## Scheduled Blocks");
    public static final Timing structureGenerationTimer = Timings.ofSafe("Structure Generation");

    public static final Timing processQueueTimer = Timings.ofSafe("processQueue");
    public static final Timing processTasksTimer = Timings.ofSafe("processTasks");

    public static final Timing playerCommandTimer = Timings.ofSafe("playerCommand");

    public static final Timing entityActivationCheckTimer = Timings.ofSafe("entityActivationCheck");

    public static final Timing antiXrayUpdateTimer = Timings.ofSafe("anti-xray - update");
    public static final Timing antiXrayObfuscateTimer = Timings.ofSafe("anti-xray - obfuscate");

    // Crucible start
    public static final Timing entityMoveTimer = Timings.ofSafe("## entityMove");
    public static final Timing  timerEntityBaseTick = Timings.ofSafe("## livingEntityBaseTick");
    public static final Timing  timerEntityAI = Timings.ofSafe("## livingEntityAI");
    public static final Timing  timerEntityAICollision = Timings.ofSafe("## livingEntityAICollision");
    public static final Timing  timerEntityAIMove = Timings.ofSafe("## livingEntityAIMove");
    public static final Timing  timerEntityTickRest = Timings.ofSafe("## livingEntityTickRest");
    //Crucible end

    private static final Map<Class<?>, String> taskNameCache = new MapMaker().weakKeys().makeMap();

    private MinecraftTimings() {}

    /**
     * Gets a timer associated with a plugins tasks.
     * @param bukkitTask
     * @param period
     * @return
     */
    public static Timing getPluginTaskTimings(BukkitTask bukkitTask, long period) {
        if (!bukkitTask.isSync()) {
            return NullTimingHandler.NULL;
        }
        Plugin plugin;

        CraftTask craftTask = (CraftTask) bukkitTask;

        final Class<?> taskClass = craftTask.getTaskClass();
        if (bukkitTask.getOwner() != null) {
            plugin = bukkitTask.getOwner();
        } else {
            plugin = TimingsManager.getPluginByClassloader(taskClass);
        }

        final String taskname = taskNameCache.computeIfAbsent(taskClass, clazz -> {
            try {
                return clazz.isAnonymousClass() || clazz.isLocalClass()
                           ? clazz.getName()
                           : clazz.getCanonicalName();
            } catch (Throwable ex) {
                new Exception("Error occurred detecting class name", ex).printStackTrace();
                return "MangledClassFile";
            }
        });

        StringBuilder name = new StringBuilder(64);
        name.append("Task: ").append(taskname);
        if (period > 0) {
            name.append(" (interval:").append(period).append(")");
        } else {
            name.append(" (Single)");
        }

        if (plugin == null) {
            return Timings.ofSafe(null, name.toString());
        }

        return Timings.ofSafe(plugin, name.toString());
    }

    /**
     * Get a named timer for the specified entity type to track type specific timings.
     * @param entity
     * @return
     */
    public static Timing getEntityTimings(Entity entity) {
        String entityType = entity.getClass().getName();
        return Timings.ofSafe("Minecraft", "## tickEntity - " + entityType, tickEntityTimer);
    }

    /**
     * Get a named timer for the specified tile entity type to track type specific timings.
     * @param entity
     * @return
     */
    public static Timing getTileEntityTimings(TileEntity entity) {
        String entityType = entity.getClass().getName();
        return Timings.ofSafe("Minecraft", "## tickTileEntity - " + entityType, tickTileEntityTimer);
    }
    public static Timing getTileEntityPersonalTimings(TileEntity entity, Timing tileClassTiming) {
        return Timings.ofSafe("Minecraft", "## !tileSpawnedAt - [world=" + entity.worldObj.getSaveHandler().getWorldDirectoryName() + ", x=" + entity.xCoord + ", y=" + entity.yCoord + ", z=" + entity.zCoord + "]", tileClassTiming);
    }
    public static Timing getCancelTasksTimer() {
        return Timings.ofSafe("Cancel Tasks");
    }
    public static Timing getCancelTasksTimer(Plugin plugin) {
        return Timings.ofSafe(plugin, "Cancel Tasks");
    }

    public static void stopServer() {
        TimingsManager.stopServer();
    }

    public static Timing getBlockTiming(Block block) {
        return Timings.ofSafe("## Scheduled Block: " + block.toString(), scheduledBlocksTimer);
    }
/*
    public static Timing getStructureTiming(StructureGenerator structureGenerator) {
        return Timings.ofSafe("Structure Generator - " + structureGenerator.getName(), structureGenerationTimer);
    }*/

    public static Timing getPacketTiming(Packet packet) {
        return Timings.ofSafe("## Packet - " + packet.getClass().getSimpleName(), packetProcessTimer);
    }

    //public static Timing getCommandFunctionTiming(CustomFunction function) {
    //    return Timings.ofSafe("Command Function - " + function.getMinecraftKey().toString());
    //}
}

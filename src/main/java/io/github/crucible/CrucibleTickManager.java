package io.github.crucible;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CrucibleTickManager {
    private final World myWorld;
    private final Long2ObjectMap<TileStopwatch> stopwatches = new Long2ObjectOpenHashMap<>();
    private static boolean isLaggy = false;
    public CrucibleTickManager(World myWorld) {
        this.myWorld = myWorld;
    }

    public void tickTiles() {

    }

    public int hashTilePosition(int x, int z) {
        x = x >> 4; // to chunk coord
        x = x >> 5; // to region coord
        z = z >> 4;
        z = z >> 5;
        int hash = 17;
        hash = hash * 31 + x;
        hash = hash * 31 + z;
        return hash;
    }

    public TileStopwatch of(TileEntity tile) {
        int hash = hashTilePosition(tile.xCoord,tile.zCoord);
        return stopwatches.computeIfAbsent(hash, (key) -> new TileStopwatch());
    }

    public static boolean isLaggy() {
        return isLaggy;
    }

    public void tickEnd() {
        for (Long2ObjectMap.Entry<TileStopwatch> tileStopwatchEntry : Long2ObjectMaps.fastIterable(stopwatches)) {
            tileStopwatchEntry.getValue().reset();
        }
    }

    public static void serverTickEnd() {
       if (MinecraftServer.getServer().getTickCounter() % 4 == 0) {
           isLaggy = (mean(MinecraftServer.getServer().tickTimeArray) * 1.0E-6D) > CrucibleConfigs.configs.crucible_tickHandler_meantimeThreshold;
       }
    }

    private static long mean(long[] values) {
        long sum = 0l;
        for (long v : values) {
            sum += v;
        }

        return sum / values.length;
    }
    public static class TileStopwatch {
        private long elapsedTime = 0;
        private long start = 0;
        TileStopwatch() {
        }

        public void tickStart() {
            start = System.nanoTime();
        }

        public void tickStop() {
            elapsedTime = elapsedTime + (System.nanoTime() - start);
        }

        public boolean canTick() {
            return elapsedTime < (CrucibleConfigs.configs.crucible_tickHandler_regionMeanTime * 1000000L);
        }

        void reset() {
            elapsedTime = 0;
        }
    }
}

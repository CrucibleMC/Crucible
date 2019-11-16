package org.bukkit.craftbukkit.chunkio;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.bukkit.Server;
import org.bukkit.craftbukkit.util.AsynchronousExecutor;
import org.bukkit.craftbukkit.util.LongHash;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

class ChunkIOProvider implements AsynchronousExecutor.CallBackProvider<QueuedChunk, Chunk, Runnable, RuntimeException> {
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    // async stuff
    public Chunk callStage1(QueuedChunk queuedChunk) throws RuntimeException {
        AnvilChunkLoader loader = queuedChunk.loader;
        Object[] data = new Object[0];
        try {
            data = loader.loadChunk__Async(queuedChunk.world, queuedChunk.x, queuedChunk.z);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (data != null) {
            queuedChunk.compound = (NBTTagCompound) data[1];
            return (Chunk) data[0];
        }

        return null;
    }

    // sync stuff
    public void callStage2(QueuedChunk queuedChunk, Chunk chunk) throws RuntimeException {
        if (chunk == null) {
            // If the chunk loading failed just do it synchronously (may generate)
            queuedChunk.provider.originalLoadChunk(queuedChunk.x, queuedChunk.z);
            return;
        }

        queuedChunk.loader.loadEntities( queuedChunk.world, queuedChunk.compound.getCompoundTag("Level"),chunk);
        //chunk.lastSaved = queuedChunk.provider.world.getTime();
        chunk.lastSaveTime = queuedChunk.provider.worldObj.getTotalWorldTime();
        //queuedChunk.provider.chunks.put(LongHash.toLong(queuedChunk.x, queuedChunk.z), chunk);
        queuedChunk.provider.loadedChunkHashMap.add(LongHash.toLong(queuedChunk.x, queuedChunk.z), chunk);
        chunk.onChunkLoad();

        if (queuedChunk.provider.currentChunkProvider != null) {
            queuedChunk.provider.currentChunkProvider.recreateStructures(queuedChunk.x, queuedChunk.z);
        }

        Server server = queuedChunk.provider.worldObj.getServer();
        if (server != null) {
            server.getPluginManager().callEvent(new org.bukkit.event.world.ChunkLoadEvent(chunk.bukkitChunk, false));
        }

        // Update neighbor counts
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                Chunk neighbor = queuedChunk.provider.getChunkIfLoaded(chunk.xPosition + x, chunk.zPosition + z);
                if (neighbor != null) {
                    neighbor.setNeighborLoaded(-x, -z);
                    chunk.setNeighborLoaded(x, z);
                }
            }
        }

        chunk.populateChunk(queuedChunk.provider, queuedChunk.provider, queuedChunk.x, queuedChunk.z);
    }

    public void callStage3(QueuedChunk queuedChunk, Chunk chunk, Runnable runnable) throws RuntimeException {
        runnable.run();
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "Chunk I/O Executor Thread-" + threadNumber.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}

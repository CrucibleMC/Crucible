package io.github.crucible.util;

import io.github.crucible.CrucibleModContainer;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlockState;

import java.util.ArrayList;
import java.util.List;

// Our best attempt to replace the janky cauldron tree generation capture
// There's still quite a bit of edge cases it does not cover however for tree generation it seems fine
public class WorldChangeHolder {
    private final World world;
    // Linked map so operation order is preserved. Re-setting a position moves it back to the end (last-write order),
    // matching the old capture list which removed and re-appended the entry.
    private final Long2ObjectLinkedOpenHashMap<BlockChange> changes = new Long2ObjectLinkedOpenHashMap<>(); // Should we tweak the load factor?

    public WorldChangeHolder(World world) {
        this.world = world;
    }

    public CaptureContext startCapture() {
        if (this.world.captureAndProxyBlocks || this.world.captureBlockSnapshots) {
            // I have a feeling we may catch some odd undiscovered bugs with this (possible recursion during capture)
            // Future proofing if we need to make changes, we can keep the same usage but change how startCapture works internally.
            throw new IllegalStateException("Capture already in progress!");
        }

        this.world.captureAndProxyBlocks = true;
        return new SimpleCaptureContext();
    }

    // Should we add some safety checks here eventually?
    public void setBlock(int x, int y, int z, Block block, int meta, int flags) {
        long key = BlockCoords.asLong(x, y, z);
        // Move an already-captured position to the end so apply order follows last-write, like the old capture list did.
        BlockChange change = this.changes.getAndMoveToLast(key);
        if (change == null) {
            change = new BlockChange();
            this.changes.putAndMoveToLast(key, change);
        }
        change.update(block, meta, flags);
    }

    public Block getBlock(int x, int y, int z) {
        BlockChange change = this.changes.get(BlockCoords.asLong(x, y, z));
        return change == null ? null : change.block;
    }

    // Returns -1 when the position was not captured. Should be safe as a sentinel, block metadata is a 4-bit value (0-15)
    // everywhere this path can reach, so a real captured meta can never be negative. Should check if endless IDs does anything weird with meta though.
    public int getMetadata(int x, int y, int z) {
        BlockChange change = this.changes.get(BlockCoords.asLong(x, y, z));
        return change == null ? -1 : change.meta;
    }

    public boolean isCaptured(int x, int y, int z) {
        return this.changes.containsKey(BlockCoords.asLong(x, y, z));
    }

    public TileEntity getTileEntity(int x, int y, int z) {
        BlockChange change = this.changes.get(BlockCoords.asLong(x, y, z));
        if (change == null) {
            return null;
        }

        if (change.tile != null) {
            return change.tile;
        }

        TileEntity tile = change.block.createTileEntity(world, change.meta);
        if (tile != null) {
            // Initialize our proxy tile. Otherwise, writeToNBT would serialize (0, 0, 0) and every restore path
            // (applyChanges, CraftBlockState.update, forge BlockSnapshot) would teleport the real tile to the world origin.
            // Also solves the edge case of when writing the tile it expects a valid world object
            tile.setWorldObj(world);
            tile.xCoord = x;
            tile.yCoord = y;
            tile.zCoord = z;
        }
        return change.tile = tile;
    }

    public void applyChanges() {
        // Sanity check just in case...
        if (this.world.captureAndProxyBlocks || this.world.captureBlockSnapshots) {
            throw new IllegalStateException("Trying to apply changes while still capturing blocks!!!");
        }

        for (Long2ObjectMap.Entry<BlockChange> changeEntry : this.changes.long2ObjectEntrySet()) {
            BlockChange change = changeEntry.getValue();
            long pos = changeEntry.getLongKey();
            int x = BlockCoords.getX(pos), y = BlockCoords.getY(pos), z = BlockCoords.getZ(pos);
            change.ensureValid();
            this.world.setBlock(x, y, z, change.block, change.meta, change.flags);

            if (change.tile != null) {
                // Is it safe to use world.setTileEntity?
                // I'll bite the performance cost and do something that seems safer, similar to forge's block snapshot.
                TileEntity te = this.world.getTileEntity(x, y, z);
                if (te == null) {
                    // The final block/meta produced no tile entity (e.g. a multi-meta tile, or the chunk-level set failed).
                    // Nothing to restore into so we log and carry on.
                    CrucibleModContainer.logger.debug("No tile entity to restore captured data into at {}, {}, {}", x, y, z);
                    continue;
                }

                try {
                    NBTTagCompound data = new NBTTagCompound();
                    change.tile.writeToNBT(data);
                    te.readFromNBT(data);
                } catch (RuntimeException e) {
                    CrucibleModContainer.logger.warn("Failed to restore proxied tile entity at {}, {}, {}", x, y, z, e);
                }
            }
        }

        this.changes.clear();
    }

    public void discardChanges() {
        this.changes.clear();
    }

    public boolean isCapturing() {
        return this.world.captureAndProxyBlocks;
    }

    /**
     * @deprecated Currently this method acts as a glue to keep compat with forge events in ForgeHooks, should be replaced eventually.
     */
    @Deprecated
    public void transferToForgeBlockSnapshot() {
        for (Long2ObjectMap.Entry<BlockChange> changeEntry : this.changes.long2ObjectEntrySet()) {
            BlockChange change = changeEntry.getValue();
            long pos = changeEntry.getLongKey();
            NBTTagCompound nbt = null;
            if (change.tile != null) {
                nbt = new NBTTagCompound();
                change.tile.writeToNBT(nbt);
            }
            BlockSnapshot snapshot = new BlockSnapshot(this.world, BlockCoords.getX(pos), BlockCoords.getY(pos), BlockCoords.getZ(pos),
                change.block, change.meta, nbt);
            // The (Block, meta, NBT) constructor hardcodes flag = 3; carry the generator's flags like applyChanges does.
            snapshot.flag = change.flags;
            this.world.capturedBlockSnapshots.add(snapshot);
        }

        this.discardChanges();
    }

    public static class BlockChange {
        private TileEntity tile;
        private Block block;
        private int meta;
        private int flags;

        public void update(Block block, int meta, int flags) {
            if (this.block != block || this.meta != meta) {
                // We probably should check if the tile would be valid during meta changes in case a single tile supports multiple metas.
                // This also drops any proxy tile already handed out through getTileEntity. A generator still holding that reference
                // would keep mutating an orphaned tile. Acceptable for tree generation, which never re-sets a position it took the tile of.
                this.tile = null;
            }
            this.block = block;
            this.meta = meta;
            this.flags = flags;
        }

        public void ensureValid() {
            if (this.block == null)  {
                throw new IllegalStateException("Block change was not properly initialized! Block is null");
            }
        }

        @Override
        public String toString() {
            return "BlockChange{" +
                "tile=" + tile +
                ", block=" + block +
                ", meta=" + meta +
                ", flags=" + flags +
                '}';
        }
    }

    public interface CaptureContext extends AutoCloseable {
        void close();
        void commit();
        void discardChanges();
        void notifyDelegate(BlockChangeDelegate delegate);
        void pauseCapture();
        void resumeCapture();
        boolean hasChanges();
        List<BlockState> asBukkitBlockState();
    }

    private class SimpleCaptureContext implements CaptureContext {
        private boolean commit;
        private BlockChangeDelegate delegate;

        @Override
        public void close() {
            WorldChangeHolder.this.world.captureAndProxyBlocks = false;

            if (this.delegate != null) {
                for (Long2ObjectMap.Entry<BlockChange> changeEntry : WorldChangeHolder.this.changes.long2ObjectEntrySet()) {
                    BlockChange change = changeEntry.getValue();
                    long pos = changeEntry.getLongKey();
                    //noinspection deprecation
                    delegate.setTypeIdAndData(BlockCoords.getX(pos), BlockCoords.getY(pos), BlockCoords.getZ(pos),
                        Block.getIdFromBlock(change.block), change.meta);
                }
            }

            // Only an explicit commit() writes captured changes into the world. This keeps generateTree
            // from mirroring a capture-only delegate's changes into the world, and leaves no partial state if a generator
            // throws or fails halfway through.
            if (this.commit) {
                WorldChangeHolder.this.applyChanges();
            } else {
                WorldChangeHolder.this.discardChanges();
            }

            if (!WorldChangeHolder.this.world.capturedBlockSnapshots.isEmpty()) {
                // Should never happen. Log loudly and recover rather than throwing out of close(), which inside
                // try-with-resources would suppress the real exception and turn a diagnostic into a crashed tick.
                CrucibleModContainer.logger.warn("capturedBlockSnapshots was not empty after a capture ({} entries)! This is a bug!",
                    WorldChangeHolder.this.world.capturedBlockSnapshots.size());
                WorldChangeHolder.this.world.capturedBlockSnapshots.clear();
            }
        }

        @Override
        public void commit() {
            this.commit = true;
        }

        @Override
        public void discardChanges() {
            this.commit = false;
        }

        @Override
        public void notifyDelegate(BlockChangeDelegate delegate) {
            this.delegate = delegate;
        }

        @Override
        public void pauseCapture() {
            WorldChangeHolder.this.world.captureAndProxyBlocks = false;
        }

        @Override
        public void resumeCapture() {
            WorldChangeHolder.this.world.captureAndProxyBlocks = true;
        }

        @Override
        public boolean hasChanges() {
            return !WorldChangeHolder.this.changes.isEmpty();
        }

        @Override
        public List<BlockState> asBukkitBlockState() {
            List<BlockState> blocks = new ArrayList<>(WorldChangeHolder.this.changes.size());
            for (Long2ObjectMap.Entry<BlockChange> changeEntry : WorldChangeHolder.this.changes.long2ObjectEntrySet()) {
                BlockChange change = changeEntry.getValue();
                long pos = changeEntry.getLongKey();
                blocks.add(new CraftBlockState(WorldChangeHolder.this.world, BlockCoords.getX(pos), BlockCoords.getY(pos), BlockCoords.getZ(pos),
                    change.block, (byte) change.meta, change.tile));
            }
            return blocks;
        }
    }
}

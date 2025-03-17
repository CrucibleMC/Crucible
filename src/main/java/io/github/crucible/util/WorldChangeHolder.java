package io.github.crucible.util;

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
// There's still quite a bit of edge cases it does not cover however for tree generation it seems file
public class WorldChangeHolder {
    private final World world;
    private final Long2ObjectMap<BlockChange> changes = new Long2ObjectLinkedOpenHashMap<>(); // Should we tweak the load factor? Using linked map since operation order may be important!

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
        this.changes.computeIfAbsent(BlockCoords.asLong(x, y, z), __ -> new BlockChange()).update(block, meta, flags);
    }

    public Block getBlock(int x, int y, int z) {
        BlockChange change = this.changes.get(BlockCoords.asLong(x, y, z));
        return change == null ? null : change.block;
    }

    // How safe it is to assume -1 will never be a valid metadata value?
    public int getMetadata(int x, int y, int z) {
        BlockChange change = this.changes.get(BlockCoords.asLong(x, y, z));
        return change == null ? -1 : change.meta;
    }

    public TileEntity getTileEntity(int x, int y, int z) {
        BlockChange change = this.changes.get(BlockCoords.asLong(x, y, z));
        if (change == null) {
            return null;
        }

        if (change.tile != null) {
            return change.tile;
        } else {
            return change.tile = change.block.createTileEntity(world, change.meta);
        }
    }

    public void applyChanges() {
        // Sanity check just in case...
        if (this.world.captureAndProxyBlocks || this.world.captureBlockSnapshots) {
            throw new IllegalStateException("Trying to apply changes while still capturing blocks!!!");
        }

        for (Long2ObjectMap.Entry<BlockChange> changeEntry : this.changes.long2ObjectEntrySet()) {
            BlockChange change = changeEntry.getValue();
            long pos = changeEntry.getLongKey();
            change.ensureValid();
            this.world.setBlock(BlockCoords.getX(pos), BlockCoords.getY(pos), BlockCoords.getZ(pos),
                change.block, change.meta, change.flags);

            if (change.tile != null) {
                // Is it safe to use world.setTileEntity?
                // I'll bite the performance cost and so something that seems safer similar to forge's block snapshot.

                TileEntity te = this.world.getTileEntity(BlockCoords.getX(pos), BlockCoords.getY(pos), BlockCoords.getZ(pos));
                NBTTagCompound data = new NBTTagCompound();
                change.tile.writeToNBT(data);
                te.readFromNBT(data);
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
        for (Long2ObjectMap.Entry<BlockChange> changeEntry : WorldChangeHolder.this.changes.long2ObjectEntrySet()) {
            BlockChange change = changeEntry.getValue();
            long pos = changeEntry.getLongKey();
            NBTTagCompound nbt = null;
            if (change.tile != null) {
                nbt = new NBTTagCompound();
                change.tile.writeToNBT(nbt);
            }
            this.world.capturedBlockSnapshots.add(new BlockSnapshot(this.world, BlockCoords.getX(pos), BlockCoords.getY(pos), BlockCoords.getZ(pos),
                change.block, change.meta, nbt));

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
                // We probably should check if the tile would be valid during meta changes in case a single tile supports multiple metas
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
        void discardChanges();
        void notifyDelegate(BlockChangeDelegate delegate);
        void pauseCapture();
        void resumeCapture();
        boolean hasChanges();
        List<BlockState> asBukkitBlockState();
    }

    private class SimpleCaptureContext implements CaptureContext {
        private boolean discard;
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

            if (this.discard) {
                WorldChangeHolder.this.discardChanges();
            } else {
                WorldChangeHolder.this.applyChanges();
            }

            if (!WorldChangeHolder.this.world.capturedBlockSnapshots.isEmpty()) {
                throw new IllegalStateException("capturedBlockSnapshots is not empty! This is a bug!");
            }
        }

        @Override
        public void discardChanges() {
            this.discard = true;
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

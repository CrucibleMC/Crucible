package org.bukkit.craftbukkit.v1_7_R4.block;

import net.minecraft.block.BlockDropper;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityDropper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

public class CraftDropper extends CraftBlockState implements Dropper {
    private final CraftWorld world;
    private final TileEntityDropper dropper;

    public CraftDropper(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        dropper = (TileEntityDropper) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getInventory() {
        return new CraftInventory(dropper);
    }

    public void drop() {
        Block block = getBlock();

        if (block.getType() == Material.DROPPER) {
            BlockDropper drop = (BlockDropper) Blocks.dropper;

            drop.func_149941_e(world.getHandle(), getX(), getY(), getZ());
        }
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result) {
            dropper.markDirty();
        }

        return result;
    }
}

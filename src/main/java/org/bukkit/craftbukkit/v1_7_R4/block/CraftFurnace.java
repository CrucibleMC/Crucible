package org.bukkit.craftbukkit.v1_7_R4.block;

import net.minecraft.tileentity.TileEntityFurnace;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventoryFurnace;
import org.bukkit.inventory.FurnaceInventory;

public class CraftFurnace extends CraftBlockState implements Furnace {
    private final TileEntityFurnace furnace;

    public CraftFurnace(final Block block) {
        super(block);

        furnace = (TileEntityFurnace) ((CraftWorld) block.getWorld()).getTileEntityAt(getX(), getY(), getZ());
    }

    public FurnaceInventory getInventory() {
        return new CraftInventoryFurnace(furnace);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result) {
            furnace.markDirty();
        }

        return result;
    }

    public short getBurnTime() {
        return (short) furnace.furnaceBurnTime;
    }

    public void setBurnTime(short burnTime) {
        furnace.furnaceBurnTime = burnTime;
    }

    public short getCookTime() {
        return (short) furnace.furnaceCookTime;
    }

    public void setCookTime(short cookTime) {
        furnace.furnaceCookTime = cookTime;
    }
}

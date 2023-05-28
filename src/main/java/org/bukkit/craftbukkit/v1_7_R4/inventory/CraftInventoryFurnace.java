package org.bukkit.craftbukkit.v1_7_R4.inventory;

import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;


public class CraftInventoryFurnace extends CraftInventory implements FurnaceInventory {
    public CraftInventoryFurnace(net.minecraft.tileentity.TileEntityFurnace inventory) {
        super(inventory);
    }

    public ItemStack getResult() {
        return getItem(2);
    }

    public void setResult(ItemStack stack) {
        setItem(2, stack);
    }

    public ItemStack getFuel() {
        return getItem(1);
    }

    public void setFuel(ItemStack stack) {
        setItem(1, stack);
    }

    public ItemStack getSmelting() {
        return getItem(0);
    }

    public void setSmelting(ItemStack stack) {
        setItem(0, stack);
    }

    @Override
    public Furnace getHolder() {
        return (Furnace) inventory.getOwner();
    }
}

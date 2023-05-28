package org.bukkit.craftbukkit.v1_7_R4.inventory;

import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;


public class CraftInventoryEnchanting extends CraftInventory implements EnchantingInventory {
    public CraftInventoryEnchanting(net.minecraft.inventory.ContainerEnchantTableInventory inventory) {
        super(inventory);
    }

    public ItemStack getItem() {
        return getItem(0);
    }

    public void setItem(ItemStack item) {
        setItem(0, item);
    }

    @Override
    public net.minecraft.inventory.ContainerEnchantTableInventory getInventory() {
        return (net.minecraft.inventory.ContainerEnchantTableInventory) inventory;
    }
}

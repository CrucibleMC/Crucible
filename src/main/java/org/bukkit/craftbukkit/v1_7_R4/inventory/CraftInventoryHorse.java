package org.bukkit.craftbukkit.v1_7_R4.inventory;

import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryHorse extends CraftInventory implements HorseInventory {

    public CraftInventoryHorse(net.minecraft.inventory.IInventory inventory) {
        super(inventory);
    }

    public ItemStack getSaddle() {
        return getItem(0);
    }

    public void setSaddle(ItemStack stack) {
        setItem(0, stack);
    }

    public ItemStack getArmor() {
        return getItem(1);
    }

    public void setArmor(ItemStack stack) {
        setItem(1, stack);
    }
}

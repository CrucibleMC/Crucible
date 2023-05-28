package org.bukkit.craftbukkit.v1_7_R4.inventory;

import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryBeacon extends CraftInventory implements BeaconInventory {
    public CraftInventoryBeacon(net.minecraft.tileentity.TileEntityBeacon beacon) {
        super(beacon);
    }

    public ItemStack getItem() {
        return getItem(0);
    }

    public void setItem(ItemStack item) {
        setItem(0, item);
    }
}

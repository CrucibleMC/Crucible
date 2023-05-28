package org.bukkit.craftbukkit.v1_7_R4.inventory;

import org.bukkit.inventory.MerchantInventory;

public class CraftInventoryMerchant extends CraftInventory implements MerchantInventory {
    public CraftInventoryMerchant(net.minecraft.inventory.InventoryMerchant merchant) {
        super(merchant);
    }
}

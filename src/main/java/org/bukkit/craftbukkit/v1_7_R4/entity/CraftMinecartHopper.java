package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.Inventory;

final class CraftMinecartHopper extends CraftMinecart implements HopperMinecart {
    private final CraftInventory inventory;

    CraftMinecartHopper(CraftServer server, net.minecraft.entity.item.EntityMinecartHopper entity) {
        super(server, entity);
        inventory = new CraftInventory(entity);
    }

    @Override
    public String toString() {
        return "CraftMinecartHopper{" + "inventory=" + inventory + '}';
    }

    public EntityType getType() {
        return EntityType.MINECART_HOPPER;
    }

    public Inventory getInventory() {
        return inventory;
    }
}

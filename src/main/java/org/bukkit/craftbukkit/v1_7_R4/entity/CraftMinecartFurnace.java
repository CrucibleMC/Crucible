package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PoweredMinecart;

@SuppressWarnings("deprecation")
public class CraftMinecartFurnace extends CraftMinecart implements PoweredMinecart {
    public CraftMinecartFurnace(CraftServer server, net.minecraft.entity.item.EntityMinecartFurnace entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftMinecartFurnace";
    }

    public EntityType getType() {
        return EntityType.MINECART_FURNACE;
    }
}

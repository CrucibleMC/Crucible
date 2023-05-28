package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.WaterMob;

public class CraftWaterMob extends CraftCreature implements WaterMob {

    public CraftWaterMob(CraftServer server, net.minecraft.entity.passive.EntityWaterMob entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.passive.EntityWaterMob getHandle() {
        return (net.minecraft.entity.passive.EntityWaterMob) entity;
    }

    @Override
    public String toString() {
        return this.entityName; // Cauldron
    }
}

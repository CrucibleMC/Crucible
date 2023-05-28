package org.bukkit.craftbukkit.v1_7_R4.entity;

import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownExpBottle;

public class CraftThrownExpBottle extends CraftProjectile implements ThrownExpBottle {
    public CraftThrownExpBottle(CraftServer server, net.minecraft.entity.item.EntityExpBottle entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.item.EntityExpBottle getHandle() {
        return (net.minecraft.entity.item.EntityExpBottle) entity;
    }

    @Override
    public String toString() {
        return "EntityThrownExpBottle";
    }

    public EntityType getType() {
        return EntityType.THROWN_EXP_BOTTLE;
    }
}

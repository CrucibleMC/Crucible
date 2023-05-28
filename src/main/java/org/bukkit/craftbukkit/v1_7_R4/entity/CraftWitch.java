package org.bukkit.craftbukkit.v1_7_R4.entity;

import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Witch;

public class CraftWitch extends CraftMonster implements Witch {
    public CraftWitch(CraftServer server, net.minecraft.entity.monster.EntityWitch entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.monster.EntityWitch getHandle() {
        return (net.minecraft.entity.monster.EntityWitch) entity;
    }

    @Override
    public String toString() {
        return "CraftWitch";
    }

    public EntityType getType() {
        return EntityType.WITCH;
    }
}

package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Monster;

public class CraftMonster extends CraftCreature implements Monster {

    public CraftMonster(CraftServer server, net.minecraft.entity.monster.EntityMob entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.monster.EntityMob getHandle() {
        return (net.minecraft.entity.monster.EntityMob) entity;
    }

    @Override
    public String toString() {
        return this.entityName; // Cauldron
    }
}

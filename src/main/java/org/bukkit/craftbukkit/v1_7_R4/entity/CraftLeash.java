package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;

public class CraftLeash extends CraftHanging implements LeashHitch {
    public CraftLeash(CraftServer server, net.minecraft.entity.EntityLeashKnot entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.EntityLeashKnot getHandle() {
        return (net.minecraft.entity.EntityLeashKnot) entity;
    }

    @Override
    public String toString() {
        return "CraftLeash";
    }

    public EntityType getType() {
        return EntityType.LEASH_HITCH;
    }
}

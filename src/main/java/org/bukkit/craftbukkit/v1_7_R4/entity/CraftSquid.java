package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Squid;

public class CraftSquid extends CraftWaterMob implements Squid {

    public CraftSquid(CraftServer server, net.minecraft.entity.passive.EntitySquid entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.passive.EntitySquid getHandle() {
        return (net.minecraft.entity.passive.EntitySquid) entity;
    }

    @Override
    public String toString() {
        return "CraftSquid";
    }

    public EntityType getType() {
        return EntityType.SQUID;
    }
}

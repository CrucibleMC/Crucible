package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Spider;

public class CraftSpider extends CraftMonster implements Spider {

    public CraftSpider(CraftServer server, net.minecraft.entity.monster.EntitySpider entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.monster.EntitySpider getHandle() {
        return (net.minecraft.entity.monster.EntitySpider) entity;
    }

    @Override
    public String toString() {
        return "CraftSpider";
    }

    public EntityType getType() {
        return EntityType.SPIDER;
    }
}

package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Silverfish;

public class CraftSilverfish extends CraftMonster implements Silverfish {
    public CraftSilverfish(CraftServer server, net.minecraft.entity.monster.EntitySilverfish entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.monster.EntitySilverfish getHandle() {
        return (net.minecraft.entity.monster.EntitySilverfish) entity;
    }

    @Override
    public String toString() {
        return "CraftSilverfish";
    }

    public EntityType getType() {
        return EntityType.SILVERFISH;
    }
}

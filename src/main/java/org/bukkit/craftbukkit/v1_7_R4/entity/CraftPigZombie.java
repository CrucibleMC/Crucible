package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;

public class CraftPigZombie extends CraftZombie implements PigZombie {

    public CraftPigZombie(CraftServer server, net.minecraft.entity.monster.EntityPigZombie entity) {
        super(server, entity);
    }

    public int getAnger() {
        return getHandle().angerLevel;
    }

    public void setAnger(int level) {
        getHandle().angerLevel = level;
    }

    public boolean isAngry() {
        return getAnger() > 0;
    }

    public void setAngry(boolean angry) {
        setAnger(angry ? 400 : 0);
    }

    @Override
    public net.minecraft.entity.monster.EntityPigZombie getHandle() {
        return (net.minecraft.entity.monster.EntityPigZombie) entity;
    }

    @Override
    public String toString() {
        return "CraftPigZombie";
    }

    public EntityType getType() {
        return EntityType.PIG_ZOMBIE;
    }
}

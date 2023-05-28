package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;

public class CraftTNTPrimed extends CraftEntity implements TNTPrimed {

    public CraftTNTPrimed(CraftServer server, net.minecraft.entity.item.EntityTNTPrimed entity) {
        super(server, entity);
    }

    public float getYield() {
        return getHandle().yield;
    }

    public void setYield(float yield) {
        getHandle().yield = yield;
    }

    public boolean isIncendiary() {
        return getHandle().isIncendiary;
    }

    public void setIsIncendiary(boolean isIncendiary) {
        getHandle().isIncendiary = isIncendiary;
    }

    public int getFuseTicks() {
        return getHandle().fuse;
    }

    public void setFuseTicks(int fuseTicks) {
        getHandle().fuse = fuseTicks;
    }

    @Override
    public net.minecraft.entity.item.EntityTNTPrimed getHandle() {
        return (net.minecraft.entity.item.EntityTNTPrimed) entity;
    }

    @Override
    public String toString() {
        return "CraftTNTPrimed";
    }

    public EntityType getType() {
        return EntityType.PRIMED_TNT;
    }

    public Entity getSource() {
        net.minecraft.entity.EntityLivingBase source = getHandle().getTntPlacedBy();

        if (source != null) {
            Entity bukkitEntity = source.getBukkitEntity();

            if (bukkitEntity.isValid()) {
                return bukkitEntity;
            }
        }

        return null;
    }
}

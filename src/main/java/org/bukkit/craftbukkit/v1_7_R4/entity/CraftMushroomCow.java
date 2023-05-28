package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;

public class CraftMushroomCow extends CraftCow implements MushroomCow {
    public CraftMushroomCow(CraftServer server, net.minecraft.entity.passive.EntityMooshroom entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.passive.EntityMooshroom getHandle() {
        return (net.minecraft.entity.passive.EntityMooshroom) entity;
    }

    @Override
    public String toString() {
        return "CraftMushroomCow";
    }

    public EntityType getType() {
        return EntityType.MUSHROOM_COW;
    }
}

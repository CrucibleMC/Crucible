package org.bukkit.craftbukkit.v1_7_R4.entity;

import net.minecraft.entity.projectile.EntityEgg;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;

public class CraftEgg extends CraftProjectile implements Egg {
    public CraftEgg(CraftServer server, EntityEgg entity) {
        super(server, entity);
    }

    @Override
    public EntityEgg getHandle() {
        return (EntityEgg) entity;
    }

    @Override
    public String toString() {
        return "CraftEgg";
    }

    public EntityType getType() {
        return EntityType.EGG;
    }
}

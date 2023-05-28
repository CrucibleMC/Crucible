package org.bukkit.craftbukkit.v1_7_R4.entity;

import net.minecraft.entity.monster.EntityGolem;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Golem;

public class CraftGolem extends CraftCreature implements Golem {
    public CraftGolem(CraftServer server, EntityGolem entity) {
        super(server, entity);
    }

    @Override
    public EntityGolem getHandle() {
        return (EntityGolem) entity;
    }

    @Override
    public String toString() {
        return "CraftGolem";
    }
}

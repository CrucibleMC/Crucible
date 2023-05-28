package org.bukkit.craftbukkit.v1_7_R4.entity;

import net.minecraft.entity.passive.EntityAnimal;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Animals;

public class CraftAnimals extends CraftAgeable implements Animals {

    public CraftAnimals(CraftServer server, EntityAnimal entity) {
        super(server, entity);
    }

    @Override
    public EntityAnimal getHandle() {
        return (EntityAnimal) entity;
    }

    @Override
    public String toString() {
        return this.entityName; // Cauldron
    }
}

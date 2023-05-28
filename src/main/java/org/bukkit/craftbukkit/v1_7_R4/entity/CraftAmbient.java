package org.bukkit.craftbukkit.v1_7_R4.entity;

import net.minecraft.entity.passive.EntityAmbientCreature;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.EntityType;

public class CraftAmbient extends CraftLivingEntity implements Ambient {
    public CraftAmbient(CraftServer server, EntityAmbientCreature entity) {
        super(server, entity);
    }

    @Override
    public EntityAmbientCreature getHandle() {
        return (EntityAmbientCreature) entity;
    }

    @Override
    public String toString() {
        return this.entityName; // Cauldron
    }

    public EntityType getType() {
        // Cauldron start
        EntityType type = EntityType.fromName(this.entityName);
        if (type != null)
            return type;
        else return EntityType.UNKNOWN;
        // Cauldron end
    }
}

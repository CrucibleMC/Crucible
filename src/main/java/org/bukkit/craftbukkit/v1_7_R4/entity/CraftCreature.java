package org.bukkit.craftbukkit.v1_7_R4.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

public class CraftCreature extends CraftLivingEntity implements Creature {
    public CraftCreature(CraftServer server, EntityCreature entity) {
        super(server, entity);
    }

    public CraftLivingEntity getTarget() {
        if (getHandle().entityToAttack == null) return null;
        if (!(getHandle().entityToAttack instanceof EntityLivingBase)) return null;

        return (CraftLivingEntity) getHandle().entityToAttack.getBukkitEntity();
    }

    public void setTarget(LivingEntity target) {
        EntityCreature entity = getHandle();
        if (target == null) {
            entity.entityToAttack = null;
        } else if (target instanceof CraftLivingEntity) {
            entity.entityToAttack = ((CraftLivingEntity) target).getHandle();
            entity.pathToEntity = entity.worldObj.getPathEntityToEntity(entity, entity.entityToAttack, 16.0F, true, false, false, true);
        }
    }

    @Override
    public EntityCreature getHandle() {
        return (EntityCreature) entity;
    }

    @Override
    public String toString() {
        return this.entityName; // Cauldron
    }
}

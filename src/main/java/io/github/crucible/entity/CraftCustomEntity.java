package io.github.crucible.entity;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;

public class CraftCustomEntity extends CraftEntity {

    public Class<? extends Entity> entityClass;
    public String entityName;

    public CraftCustomEntity(CraftServer server, net.minecraft.entity.Entity entity) {
        super(server, entity);
        this.entityClass = entity.getClass();
        this.entityName = EntityRegistry.getCustomEntityTypeName(entityClass);
        if (entityName == null)
            entityName = entity.getCommandSenderName();
    }

    @Override
    public net.minecraft.entity.Entity getHandle() {
        return entity;
    }

    @Override
    public String toString() {
        return this.entityName;
    }

    @SuppressWarnings("deprecation")
    public EntityType getType() {
        EntityType type = EntityType.fromName(this.entityName);
        if (type != null)
            return type;
        else return EntityType.UNKNOWN;
    }
}
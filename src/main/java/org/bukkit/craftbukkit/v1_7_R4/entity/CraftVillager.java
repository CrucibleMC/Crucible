package org.bukkit.craftbukkit.v1_7_R4.entity;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class CraftVillager extends CraftAgeable implements Villager {
    public CraftVillager(CraftServer server, net.minecraft.entity.passive.EntityVillager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.entity.passive.EntityVillager getHandle() {
        return (net.minecraft.entity.passive.EntityVillager) entity;
    }

    @Override
    public String toString() {
        return "CraftVillager";
    }

    public EntityType getType() {
        return EntityType.VILLAGER;
    }

    public Profession getProfession() {
        return Profession.getProfession(getHandle().getProfession());
    }

    public void setProfession(Profession profession) {
        Validate.notNull(profession);
        getHandle().setProfession(profession.getId());
    }
}

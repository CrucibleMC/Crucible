package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;

public class CraftLightningStrike extends CraftEntity implements LightningStrike {
    public CraftLightningStrike(final CraftServer server, final net.minecraft.entity.effect.EntityLightningBolt entity) {
        super(server, entity);
    }

    public boolean isEffect() {
        return ((net.minecraft.entity.effect.EntityLightningBolt) super.getHandle()).isEffect;
    }

    @Override
    public net.minecraft.entity.effect.EntityLightningBolt getHandle() {
        return (net.minecraft.entity.effect.EntityLightningBolt) entity;
    }

    @Override
    public String toString() {
        return "CraftLightningStrike";
    }

    public EntityType getType() {
        return EntityType.LIGHTNING;
    }

    // Spigot start
    private final LightningStrike.Spigot spigot = new LightningStrike.Spigot()
    {

        @Override
        public boolean isSilent()
        {
            return getHandle().isSilent;
        }

    };

    public LightningStrike.Spigot spigot()
    {
        return this.spigot;
    }
    // Spigot end
}

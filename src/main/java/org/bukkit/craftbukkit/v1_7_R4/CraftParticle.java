package org.bukkit.craftbukkit.v1_7_R4;

import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class CraftParticle {

    public static MinecraftParticles toNMS(Particle bukkit) {

        for(MinecraftParticles minecraftParticles : MinecraftParticles.values())
            if(minecraftParticles.name().equalsIgnoreCase(bukkit.name()))
                return minecraftParticles;

        return MinecraftParticles.FLAME;
    }

    public static Particle toBukkit(MinecraftParticles nms) {
        return Particle.valueOf(nms.name());
    }

    public static int[] toData(Particle particle, Object obj) {
        if (particle.getDataType().equals(Void.class)) {
            return new int[0];
        }
        if (particle.getDataType().equals(ItemStack.class)) {
            if (obj == null) {
                return new int[]{0, 0};
            }
            ItemStack itemStack = (ItemStack) obj;
            return new int[]{itemStack.getType().getId(), itemStack.getDurability()};
        }
        if (particle.getDataType().equals(MaterialData.class)) {
            if (obj == null) {
                return new int[]{0};
            }
            MaterialData data = (MaterialData) obj;
            return new int[]{data.getItemTypeId() + ((int)(data.getData()) << 12)};
        }
        throw new IllegalArgumentException(particle.getDataType().toString());
    }
}

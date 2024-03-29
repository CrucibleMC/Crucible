package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Minecart;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class CraftMinecart extends CraftVehicle implements Minecart { // Cauldron - concrete for modded minecarts
    public CraftMinecart(CraftServer server, net.minecraft.entity.item.EntityMinecart entity) {
        super(server, entity);
    }

    // Cauldron start
    public org.bukkit.entity.EntityType getType() {
        return org.bukkit.entity.EntityType.MINECART;
    }
    // Cauldron end

    public double getDamage() {
        return getHandle().getDamage();
    }

    public void setDamage(double damage) {
        getHandle().setDamage((float) damage);
    }

    public double getMaxSpeed() {
        return getHandle().maxSpeed;
    }

    public void setMaxSpeed(double speed) {
        if (speed >= 0D) {
            getHandle().maxSpeed = speed;
        }
    }

    public boolean isSlowWhenEmpty() {
        return getHandle().slowWhenEmpty;
    }

    public void setSlowWhenEmpty(boolean slow) {
        getHandle().slowWhenEmpty = slow;
    }

    public Vector getFlyingVelocityMod() {
        return getHandle().getFlyingVelocityMod();
    }

    public void setFlyingVelocityMod(Vector flying) {
        getHandle().setFlyingVelocityMod(flying);
    }

    public Vector getDerailedVelocityMod() {
        return getHandle().getDerailedVelocityMod();
    }

    public void setDerailedVelocityMod(Vector derailed) {
        getHandle().setDerailedVelocityMod(derailed);
    }

    @Override
    public net.minecraft.entity.item.EntityMinecart getHandle() {
        return (net.minecraft.entity.item.EntityMinecart) entity;
    }

    @Deprecated
    public void _INVALID_setDamage(int damage) {
        setDamage(damage);
    }

    @Deprecated
    public int _INVALID_getDamage() {
        return NumberConversions.ceil(getDamage());
    }
}

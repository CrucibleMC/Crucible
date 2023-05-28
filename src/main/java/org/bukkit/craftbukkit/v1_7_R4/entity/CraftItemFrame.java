package org.bukkit.craftbukkit.v1_7_R4.entity;


import org.apache.commons.lang.Validate;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

public class CraftItemFrame extends CraftHanging implements ItemFrame {
    public CraftItemFrame(CraftServer server, net.minecraft.entity.item.EntityItemFrame entity) {
        super(server, entity);
    }

    static int toInteger(Rotation rotation) {
        // Translate Bukkit API rotation to NMS integer
        switch (rotation) {
            case NONE:
                return 0;
            case CLOCKWISE:
                return 1;
            case FLIPPED:
                return 2;
            case COUNTER_CLOCKWISE:
                return 3;
            default:
                throw new IllegalArgumentException(rotation + " is not applicable to an ItemFrame");
        }
    }

    public boolean setFacingDirection(BlockFace face, boolean force) {
        if (!super.setFacingDirection(face, force)) {
            return false;
        }

        net.minecraft.world.WorldServer world = ((CraftWorld) this.getWorld()).getHandle();
        world.getEntityTracker().removeEntityFromAllTrackingPlayers(this.getHandle());
        world.getEntityTracker().addEntityToTracker(this.getHandle());
        return true;
    }

    public org.bukkit.inventory.ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(getHandle().getDisplayedItem());
    }

    public void setItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || item.getTypeId() == 0) {
            getHandle().getDataWatcher().addObjectByDataType(2, 5);
            getHandle().getDataWatcher().setObjectWatched(2);
        } else {
            getHandle().setDisplayedItem(CraftItemStack.asNMSCopy(item));
        }
    }

    public Rotation getRotation() {
        return toBukkitRotation(getHandle().getRotation());
    }

    public void setRotation(Rotation rotation) {
        Validate.notNull(rotation, "Rotation cannot be null");
        getHandle().setItemRotation(toInteger(rotation));
    }

    Rotation toBukkitRotation(int value) {
        // Translate NMS rotation integer to Bukkit API
        switch (value) {
            case 0:
                return Rotation.NONE;
            case 1:
                return Rotation.CLOCKWISE;
            case 2:
                return Rotation.FLIPPED;
            case 3:
                return Rotation.COUNTER_CLOCKWISE;
            default:
                throw new AssertionError("Unknown rotation " + value + " for " + getHandle());
        }
    }

    @Override
    public net.minecraft.entity.item.EntityItemFrame getHandle() {
        return (net.minecraft.entity.item.EntityItemFrame) entity;
    }

    @Override
    public String toString() {
        return "CraftItemFrame{item=" + getItem() + ", rotation=" + getRotation() + "}";
    }

    public EntityType getType() {
        return EntityType.ITEM_FRAME;
    }
}

package net.minecraft.inventory;

// CraftBukkit start

import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import java.util.List;
// CraftBukkit end

public class ContainerEnchantTableInventory extends InventoryBasic   // CraftBukkit -> public
{
    /**
     * The brewing stand this slot belongs to.
     */
    final ContainerEnchantment container;

    // CraftBukkit start
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    public org.bukkit.entity.Player player;
    private int maxStack = MAX_STACK;

    ContainerEnchantTableInventory(ContainerEnchantment par1ContainerEnchantment, String par2Str, boolean par3, int par4) {
        super(par2Str, par3, par4);
        this.container = par1ContainerEnchantment;
        this.setMaxStackSize(1); // CraftBukkit
    }

    public ItemStack[] getContents() {
        return this.inventoryContents;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    public org.bukkit.inventory.InventoryHolder getOwner() {
        return this.player;
    }
    // CraftBukkit end

    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended. *Isn't
     * this more of a set than a get?*
     */
    public int getInventoryStackLimit() {
        return maxStack; // CraftBukkit
    }

    /**
     * Called when an the contents of an Inventory change, usually
     */
    public void markDirty() {
        super.markDirty();
        this.container.onCraftMatrixChanged(this);
    }
}
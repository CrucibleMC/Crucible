package org.bukkit.craftbukkit.v1_7_R4.inventory;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;


public class CraftInventoryCustom extends CraftInventory {
    public CraftInventoryCustom(InventoryHolder owner, InventoryType type) {
        super(new MinecraftInventory(owner, type));
    }

    public CraftInventoryCustom(InventoryHolder owner, InventoryType type, String title) {
        super(new MinecraftInventory(owner, type, title));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size) {
        super(new MinecraftInventory(owner, size));
    }

    public CraftInventoryCustom(InventoryHolder owner, int size, String title) {
        super(new MinecraftInventory(owner, size, title));
    }

    static class MinecraftInventory implements net.minecraft.inventory.IInventory {
        private final net.minecraft.item.ItemStack[] items;
        private final List<HumanEntity> viewers;
        private final String title;
        private final InventoryHolder owner;
        private int maxStack = MAX_STACK;
        private InventoryType type;

        public MinecraftInventory(InventoryHolder owner, InventoryType type) {
            this(owner, type.getDefaultSize(), type.getDefaultTitle());
            this.type = type;
        }

        public MinecraftInventory(InventoryHolder owner, InventoryType type, String title) {
            this(owner, type.getDefaultSize(), title);
            this.type = type;
        }

        public MinecraftInventory(InventoryHolder owner, int size) {
            this(owner, size, "Chest");
        }

        public MinecraftInventory(InventoryHolder owner, int size, String title) {
            Validate.notNull(title, "Title cannot be null");
            Validate.isTrue(title.length() <= 32, "Title cannot be longer than 32 characters");
            this.items = new net.minecraft.item.ItemStack[size];
            this.title = title;
            this.viewers = new ArrayList<HumanEntity>();
            this.owner = owner;
            this.type = InventoryType.CHEST;
        }

        public int getSizeInventory() {
            return items.length;
        }

        public net.minecraft.item.ItemStack getStackInSlot(int i) {
            return items[i];
        }

        public net.minecraft.item.ItemStack decrStackSize(int i, int j) {
            net.minecraft.item.ItemStack stack = this.getStackInSlot(i);
            net.minecraft.item.ItemStack result;
            if (stack == null) return null;
            if (stack.stackSize <= j) {
                this.setInventorySlotContents(i, null);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, j);
                stack.stackSize -= j;
            }
            this.markDirty();
            return result;
        }

        public net.minecraft.item.ItemStack getStackInSlotOnClosing(int i) {
            net.minecraft.item.ItemStack stack = this.getStackInSlot(i);
            net.minecraft.item.ItemStack result;
            if (stack == null) return null;
            if (stack.stackSize <= 1) {
                this.setInventorySlotContents(i, null);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, 1);
                stack.stackSize -= 1;
            }
            return result;
        }

        public void setInventorySlotContents(int i, net.minecraft.item.ItemStack itemstack) {
            items[i] = itemstack;
            if (itemstack != null && this.getInventoryStackLimit() > 0 && itemstack.stackSize > this.getInventoryStackLimit()) {
                itemstack.stackSize = this.getInventoryStackLimit();
            }
        }

        public String getInventoryName() {
            return title;
        }

        public int getInventoryStackLimit() {
            return maxStack;
        }

        public void setMaxStackSize(int size) {
            maxStack = size;
        }

        public void markDirty() {
        }

        public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer entityhuman) {
            return true;
        }

        public net.minecraft.item.ItemStack[] getContents() {
            return items;
        }

        public void onOpen(CraftHumanEntity who) {
            viewers.add(who);
        }

        public void onClose(CraftHumanEntity who) {
            viewers.remove(who);
        }

        public List<HumanEntity> getViewers() {
            return viewers;
        }

        public InventoryType getType() {
            return type;
        }

        public void closeInventory() {
        }

        public InventoryHolder getOwner() {
            return owner;
        }

        public void openInventory() {
        }

        public boolean hasCustomInventoryName() {
            return false;
        }

        public boolean isItemValidForSlot(int i, net.minecraft.item.ItemStack itemstack) {
            return true;
        }
    }
}

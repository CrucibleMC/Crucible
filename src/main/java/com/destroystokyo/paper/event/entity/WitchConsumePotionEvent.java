package com.destroystokyo.paper.event.entity;

import org.bukkit.entity.Witch;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Fired when a witch consumes the potion in their hand to buff themselves.
 */
public class WitchConsumePotionEvent extends EntityEvent implements Cancellable {
    private ItemStack potion;

    public WitchConsumePotionEvent(Witch witch, ItemStack potion) {
        super(witch);
        this.potion = potion;
    }

    @Override
    public Witch getEntity() {
        return (Witch) super.getEntity();
    }

    /**
     * @return the potion the witch will consume and have the effects applied.
     */
    public ItemStack getPotion() {
        return potion;
    }

    /**
     * Sets the potion to be consumed and applied to the witch.
     * @param potion The potion
     */
    public void setPotion(ItemStack potion) {
        this.potion = potion != null ? potion.clone() : null;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private boolean cancelled = false;

    /**
     * @return Event was cancelled or potion was null
     */
    @Override
    public boolean isCancelled() {
        return cancelled || potion == null;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}

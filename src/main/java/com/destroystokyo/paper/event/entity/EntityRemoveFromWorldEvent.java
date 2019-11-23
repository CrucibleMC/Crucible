package com.destroystokyo.paper.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

/**
 * Fired any time an entity is being removed from a world for any reason
 */
public class EntityRemoveFromWorldEvent extends EntityEvent {

    public EntityRemoveFromWorldEvent(Entity entity) {
        super(entity);
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
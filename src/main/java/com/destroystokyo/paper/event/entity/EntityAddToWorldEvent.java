package com.destroystokyo.paper.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

/**
 * Fired any time an entity is being added to the world for any reason.
 *
 * Not to be confused with {@link org.bukkit.event.entity.CreatureSpawnEvent}
 * This will fire anytime a chunk is reloaded too.
 */
public class EntityAddToWorldEvent extends EntityEvent {

    public EntityAddToWorldEvent(Entity entity) {
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
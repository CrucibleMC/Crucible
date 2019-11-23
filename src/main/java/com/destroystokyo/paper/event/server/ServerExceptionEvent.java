package com.destroystokyo.paper.event.server;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.destroystokyo.paper.exception.ServerException;

/**
 * Called whenever an exception is thrown in a recoverable section of the server.
 */
public class ServerExceptionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private ServerException exception;

    public ServerExceptionEvent(ServerException exception) {
        super(!Bukkit.isPrimaryThread());
        this.exception = Preconditions.checkNotNull(exception, "exception");
    }

    /**
     * Gets the wrapped exception that was thrown.
     *
     * @return Exception thrown
     */
    public ServerException getException() {
        return exception;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
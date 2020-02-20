package io.github.crucible.events;

import io.github.crucible.utils.CrucibleModList;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CruciblePlayerLoginEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    private CrucibleModList list;
    private Player player;
    private Result result = Result.ALLOWED;
    private String resultMessage = "";

    public CruciblePlayerLoginEvent(Player player, CrucibleModList list) {
        this.player = player;
        this.list = list;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void deny(String denyMessage) {
        result = Result.DENY;
        resultMessage = denyMessage;
    }

    public CrucibleModList getModList() {
        return list;
    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public Result getResult() {
        return result;
    }

    public String getResultMessage() {
        return (result.equals(Result.ALLOWED)) ? "" : resultMessage;
    }


    public enum Result {
        ALLOWED,
        DENY
    }

}

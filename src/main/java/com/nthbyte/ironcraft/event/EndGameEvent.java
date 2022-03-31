package com.nthbyte.ironcraft.event;

import com.nthbyte.ironcraft.Reason;
import com.nthbyte.ironcraft.PlayerData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The event that is fired when the game has stopped. This doesn't always mean that the game was completed successfully.
 */
public class EndGameEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private PlayerData playerData;
    private Reason reason;

    public EndGameEvent(Reason reason, PlayerData playerData){
        this.reason = reason;
        this.playerData = playerData;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public Reason getReason() {
        return reason;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

}

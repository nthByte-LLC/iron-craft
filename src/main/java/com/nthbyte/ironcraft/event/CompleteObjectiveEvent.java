package com.nthbyte.ironcraft.event;

import com.nthbyte.ironcraft.PlayerData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CompleteObjectiveEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private PlayerData playerData;

    public CompleteObjectiveEvent(PlayerData playerData){
        this.playerData = playerData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

}

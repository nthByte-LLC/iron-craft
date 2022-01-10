package net.dohaw.ironcraft;

import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The event that fires when the game ends.
 */
public class EndGameEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private PlayerData playerData;

    public EndGameEvent(PlayerData playerData) {
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


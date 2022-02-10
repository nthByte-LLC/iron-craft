package net.dohaw.ironcraft.event;

import net.dohaw.ironcraft.PlayerData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when a user gets assigned a manager.
 */
public class AssignManagerEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private PlayerData manager;
    private PlayerData user;

    public AssignManagerEvent(PlayerData manager, PlayerData user){
        this.manager = manager;
        this.user = user;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public PlayerData getManager() {
        return manager;
    }

    public PlayerData getUser() {
        return user;
    }

}

package net.dohaw.ironcraft.listener;

import net.dohaw.ironcraft.IronCraftPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class ItemWatcher implements Listener {

    IronCraftPlugin plugin;

    public ItemWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onPlayerPickup(EntityPickupItemEvent e) {

        Player player;
        if (e.getEntity() instanceof Player) {
            player = (Player) e.getEntity();
        }



    }
}

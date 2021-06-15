package net.dohaw.diamondcraft.menu;

import net.dohaw.corelib.menus.Menu;
import net.dohaw.diamondcraft.DiamondCraftPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class TribesMenu extends Menu implements Listener {

    public TribesMenu(JavaPlugin plugin, int numSlots) {
        super(plugin, null, "Tribes", numSlots);
    }

//    @Override
//    public void initializeItems(Player p) {
//
//        ((DiamondCraftPlugin)plugin).getHandlergetTribes();
//
//        for(Tribe tribe : tribes){
//
//
//
//        }
//
//
//    }

    @Override
    public void initializeItems(Player p) {

    }

    @EventHandler
    @Override
    protected void onInventoryClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        int slotClicked = e.getSlot();
        ItemStack clickedItem = e.getCurrentItem();

        Inventory clickedInventory = e.getClickedInventory();
        Inventory topInventory = player.getOpenInventory().getTopInventory();

        if (clickedInventory == null) return;
        if (!topInventory.equals(inv) || !clickedInventory.equals(topInventory)) return;
        if (clickedItem == null) return;

        e.setCancelled(true);

        if(this.fillerMat == clickedItem.getType()) return;

    }

}

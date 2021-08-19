package net.dohaw.diamondcraft.data_collection;

import net.dohaw.diamondcraft.DiamondCraftPlugin;
import net.dohaw.diamondcraft.handler.PlayerDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DataCollector extends BukkitRunnable {

    private DiamondCraftPlugin diamondCraftPlugin;

    public DataCollector(DiamondCraftPlugin diamondCraftPlugin) {
        this.diamondCraftPlugin = diamondCraftPlugin;
    }


    @Override
    public void run() {

        // loop through all the player uuids that have been assigned a chamber
        for (UUID uuid : this.diamondCraftPlugin.getPlayerDataHandler().getAllPlayerData().keySet()) {

            // get the player
            Player player = Bukkit.getPlayer(UUID.fromString(uuid.toString()));

            if (player == null) {
                continue;
            }

            // get the player's inventory
            Inventory inventory = player.getInventory();

            // loop through the player's inventory
            for (ItemStack itemStack : inventory.getContents()) {

                Map<String, Integer> inventoryData = new HashMap<>();

                if (itemStack == null) {
                    continue;
                }

                // store the item's name and amount in inventoryData
                inventoryData.put(itemStack.getItemMeta().getDisplayName(), itemStack.getAmount());
                // add the inventoryData to the list in the PlayerData object.
                this.diamondCraftPlugin.getPlayerDataHandler().getData(uuid).addInventoryData(inventoryData);
            }
        }
    }
}

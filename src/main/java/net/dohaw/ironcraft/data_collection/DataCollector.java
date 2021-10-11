package net.dohaw.ironcraft.data_collection;

import net.dohaw.ironcraft.IronCraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DataCollector extends BukkitRunnable {

    private IronCraftPlugin ironCraftPlugin;

    public DataCollector(IronCraftPlugin ironCraftPlugin) {
        this.ironCraftPlugin = ironCraftPlugin;
    }


    @Override
    public void run() {

        // loop through all the player uuids that have been assigned a chamber
        for (UUID uuid : this.ironCraftPlugin.getPlayerDataHandler().getAllPlayerData().keySet()) {

            // get the player
            Player player = Bukkit.getPlayer(UUID.fromString(uuid.toString()));

            if (player == null) {
                continue;
            }

            // get the player's inventory
            Inventory inventory = player.getInventory();

            // loop through the player's inventory
            for (ItemStack itemStack : inventory.getContents()) {

                Map<String, Integer> inventoryData = new TreeMap<>();

                if (itemStack == null) {
                    continue;
                }

                // store the item's name and amount in inventoryData
                inventoryData.put(itemStack.getType().toString().toLowerCase(Locale.ROOT), itemStack.getAmount());
                // add the inventoryData to the list in the PlayerData object.
                this.ironCraftPlugin.getPlayerDataHandler().getData(uuid).addInventoryData(inventoryData);
            }
        }
    }
}

package net.dohaw.ironcraft.data_collection;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DataCollector extends BukkitRunnable {

    private IronCraftPlugin ironCraftPlugin;

    public DataCollector(IronCraftPlugin ironCraftPlugin) {
        this.ironCraftPlugin = ironCraftPlugin;
    }

    @Override
    public void run() {

        // loop through all the player uuids that have been assigned a chamber
        for (PlayerData data : this.ironCraftPlugin.getPlayerDataHandler().getAllPlayerData().values()) {

            // get the player
            Player player = data.getPlayer();
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
                data.addInventoryData(inventoryData);

            }
        }

    }
}

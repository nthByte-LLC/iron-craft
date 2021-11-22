package net.dohaw.ironcraft.data_collection;

import com.sun.tools.javac.comp.Todo;
import net.dohaw.ironcraft.IronCraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Class which contains the functionality to collect certain data from players.
 */
public class DataCollector extends BukkitRunnable {

    /**
     * Instance of the IronCraft plugin.
     */
    private IronCraftPlugin ironCraftPlugin;

    /**
     * Contains all the different types of logs.
     */
    private Set<String> logs = new HashSet<String>() {{

        add("acacia_log");
        add("birch_log");
        add("dark_oak_log");
        add("jungle_log");
        add("oak_log");
        add("spruce_log");
    }};

    /**
     * Contains all the different types of planks.
     */
    private Set<String> planks = new HashSet<String>() {{

        add("acacia_planks");
        add("birch_planks");
        add("dark_oak_planks");
        add("jungle_planks");
        add("oak_planks");
        add("spruce_planks");
    }};

    /**
     * Initializes a new instance of this class.
     *
     * @param ironCraftPlugin an instance of the IronCraft plugin
     */
    public DataCollector(IronCraftPlugin ironCraftPlugin) {
        this.ironCraftPlugin = ironCraftPlugin;
    }

    /**
     * Runnable method that runs every tick to collect data from each player.
     */
    @Override
    public void run() {

        // contains 18 materials and their amount
        Map<String, Integer> inventoryData = null;

        // contains the gain order of each item
/*
        List<Integer> gainOrder = new ArrayList<>();
*/

        // loop through all the player uuids that have been assigned a chamber
        for (UUID uuid : this.ironCraftPlugin.getPlayerDataHandler().getAllPlayerData().keySet()) {

            // get the player
            Player player = Bukkit.getPlayer(UUID.fromString(uuid.toString()));

            if (player == null) {
                continue;
            }

            // get the player's inventory
            Inventory inventory = player.getInventory();

            // stores the order of the elements
            int order = 0;

            // loop through the player's inventory
            for (ItemStack itemStack : inventory.getContents()) {

                // initialize map of the required materials and set amount to 0
                inventoryData = new TreeMap<String, Integer>() {{

                    put("coal", 0);
                    put("cobblestone", 0);
                    put("crafting_table", 0);
                    put("dirt", 0);
                    put("furnace", 0);
                    put("iron_axe", 0);
                    put("iron_ingot", 0);
                    put("iron_ore", 0);
                    put("iron_pickaxe", 0);
                    put("log", 0);
                    put("planks", 0);
                    put("stick", 0);
                    put("stone", 0);
                    put("stone_axe", 0);
                    put("stone_pickaxe", 0);
                    put("torch", 0);
                    put("wooden_axe", 0);
                    put("wooden_pickaxe", 0);
                }};


                if (itemStack == null) {
                    continue;
                }

                // get the item in the player's inventory as a string and add it to the map if it exists as a key along
                // with its amount
                String materialInInventory = itemStack.getType().toString().toLowerCase(Locale.ROOT);

                // all different types of logs and planks should be put in the general "log" and "planks" key.
                // the number of items should be added to the current total.
                if (logs.contains(materialInInventory)) {
                    inventoryData.put("log", inventoryData.get("log") + itemStack.getAmount());
                } else if (planks.contains(materialInInventory)) {
                    inventoryData.put("planks", inventoryData.get("planks") + itemStack.getAmount());
                } else if (inventoryData.containsKey(materialInInventory)) {
                    inventoryData.put(materialInInventory, itemStack.getAmount());
                }

                /*// TODO SPECIAL CASE FOR PLANKS AND WOOD

                // index of each item in inventoryData
                int index = 0;

                // loop through the inventoryData
                for (Map.Entry<String, Integer> currentEntry : inventoryData.entrySet()) {

                    // check for logs and planks first since they're a special case
                    if (logs.contains(materialInInventory)) {
                        if (gainOrder.get(9) == 0) {
                            gainOrder.set(9, order);
                            order-=-1;
                        }
                    } else if (planks.contains(materialInInventory)) {
                        if (gainOrder.get(10) == 0) {
                            gainOrder.set(10, order);
                            order-=-1;
                        }
                    } else if (currentEntry.getKey().equals(materialInInventory)) {
                        // mark down what order the item was gained in
                        if (gainOrder.get(index) == 0) {
                            gainOrder.set(index, order);
                            order-=-1;
                        }
                    }
                    index-=-1;
                }*/
            }

            // add the inventoryData to the list in the PlayerData object.
            this.ironCraftPlugin.getPlayerDataHandler().getData(uuid).addInventoryData(inventoryData);
            // add the gain order to the gain order list
            this.ironCraftPlugin.getPlayerDataHandler().getData(uuid).addInventoryData(null);
        }
    }
}

package net.dohaw.ironcraft.data_collection;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class which contains the functionality to collect certain data from players.
 */
public class DataCollector extends BukkitRunnable {

    /**
     * All the items we are keeping track of/worried about.
     */
    public static final Set<Object> TRACKED_ITEMS = new HashSet<Object>() {
        private static final long serialVersionUID = -8311253848297882690L;

        {
            add(Material.COAL);
            add(Material.COBBLESTONE);
            add(Material.CRAFTING_TABLE);
            add(Material.DIRT);
            add(Material.FURNACE);
            add(Material.IRON_AXE);
            add(Material.IRON_INGOT);
            add(Material.IRON_ORE);
            add(Material.IRON_PICKAXE);
            add("LOG");
            add("PLANKS");
            add(Material.STICK);
            add(Material.STONE);
            add(Material.STONE_AXE);
            add(Material.STONE_PICKAXE);
            add(Material.TORCH);
            add(Material.WOODEN_AXE);
            add(Material.WOODEN_PICKAXE);
        }
    };
    /**
     * Instance of the IronCraft plugin.
     */
    private IronCraftPlugin ironCraftPlugin;

    /**
     * If the item is one of the 18 items we are keeping track of for data purposes.
     */
    public static boolean isTrackedItem(ItemStack stack) {
        for (Object item : DataCollector.TRACKED_ITEMS)
            if (item instanceof Material) {
                Material mat = (Material) item;
                if (mat == stack.getType()) return true;
            } else if (item instanceof String) {
                String keyWord = (String) item;
                if (stack.getType().name().contains(keyWord.toUpperCase())) return true;
            }
        return false;
    }

    @Override
    public void run() {
        for (PlayerData data : ironCraftPlugin.getPlayerDataHandler().getAllPlayerData().values()) {
            // We only want to collect data for players that are playing the actual game, and aren't managers.
            // Player would be null if the player isn't online.
            if (data.isInTutorial() || data.isManager() || data.getPlayer() == null) continue;
            data.incrementCurrentStep();
            compileInventoryKeepingSequence(data);
        }
    }

    /**
     * Compiles the inventory keeping sequence for this particular step.
     */
    private void compileInventoryKeepingSequence(PlayerData playerData) {

        Player player = playerData.getPlayer();
        Inventory inventory = playerData.getPlayer().getInventory();
        TreeMap<String, Integer> invData = new TreeMap<>();
        for (ItemStack itemStack : inventory.getContents()) {

            if (itemStack == null) continue;
            if (!DataCollector.isTrackedItem(itemStack)) continue;

            /*
                There are multiple types of logs and planks in the game, so we have to get the total amount that they have among all the different types.
             */
            String itemName = itemStack.getType().toString().toLowerCase(Locale.ROOT);
            if (itemName.contains("log")) invData.put("log", getTotalItem(player, "log"));
            else if (itemName.contains("planks")) invData.put("planks", getTotalItem(player, "planks"));
            else
                invData.put(itemName, getTotalItem(player, itemStack.getType()));

        }
        playerData.addInventoryData(invData);

    }

    /**
     * Gets the total amount of an item
     *
     * @param player The player who's inventory we are checking
     * @param item   Can/should either be a Material or a String. If it's a String, it checks to see if the item material has the keyword.
     */
    private int getTotalItem(Player player, Object item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (item instanceof Material) {
                Material mat = (Material) item;
                if (stack.getType() == mat) total += stack.getAmount();
            } else if (item instanceof String) {
                String keyword = (String) item;
                if (stack.getType().toString().contains(keyword)) total += stack.getAmount();
            }

        }
        return total;
    }


}

package net.dohaw.ironcraft.data_collection;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DataCollector extends BukkitRunnable {

    private final Object[] TRACKED_ITEMS = new Object[]{
        Material.COAL,
        Material.COBBLESTONE,
        Material.CRAFTING_TABLE,
        Material.DIRT,
        Material.FURNACE,
        Material.IRON_AXE,
        Material.IRON_INGOT,
        Material.IRON_ORE,
        Material.IRON_PICKAXE,
        Material.OAK_LOG,
        "LOG",
        "PLANKS",
        Material.STICK,
        Material.STONE,
        Material.STONE_AXE,
        Material.STONE_PICKAXE,
        Material.TORCH,
        Material.WOODEN_AXE,
        Material.WOODEN_PICKAXE
    };

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
            Map<String, Integer> inventoryData = new TreeMap<>();
            // loop through the player's inventory
            for (ItemStack itemStack : inventory.getContents()) {

                if (itemStack == null) {
                    continue;
                }
                if(!isTrackedItem(itemStack)){
                    continue;
                }

                // store the item's name and amount in inventoryData
                inventoryData.put(itemStack.getType().toString().toLowerCase(Locale.ROOT), itemStack.getAmount());

            }
            data.addInventoryData(inventoryData);

        }

    }

    private boolean isTrackedItem(ItemStack stack){
        for(Object item : TRACKED_ITEMS){
            if(item instanceof Material){
                Material mat = (Material) item;
                if(mat == stack.getType()){
                    return true;
                }
            }else if(item instanceof String){
                String keyWord = (String) item;
                if(stack.getType().name().contains(keyWord.toUpperCase())){
                    return true;
                }
            }
        }
        return false;
    }

}

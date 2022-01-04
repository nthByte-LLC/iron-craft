package net.dohaw.ironcraft.data_collection;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DataCollectionUtil {

    public static final Set<Object> TRACKED_ITEMS = new HashSet<Object>() {
        {
            add(Material.COAL);
            add(Material.COBBLESTONE);
            add(Material.CRAFTING_TABLE);
            add(Material.DIRT);
            add(Material.FURNACE);
            add(Material.IRON_AXE);
            add(Material.IRON_INGOT);
            add(Material.RAW_IRON);
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
     * The item mapped to it's reward amount.
     */
    public static final Map<String, Integer> REWARD_MAPPINGS = new HashMap<String, Integer>() {
        {
            put("log", 1);
            put("planks", 2);
            put("stick", 4);
            put("crafting_table", 4);
            put("wooden_pickaxe", 8);
            put("cobblestone", 16);
            put("furnace", 32);
            put("stone_pickaxe", 32);
            put("raw_iron", 64);
            put("iron_ingot", 128);
            put("iron_pickaxe", 256);
        }
    };

    /**
     * If the item is one of the 18 items we are keeping track of for data purposes.
     */
    public static boolean isTrackedItem(ItemStack stack) {
        for (Object item : DataCollectionUtil.TRACKED_ITEMS)
            if (item instanceof Material) {
                Material mat = (Material) item;
                if (mat == stack.getType()) return true;
            } else if (item instanceof String) {
                String keyWord = (String) item;
                if (stack.getType().name().contains(keyWord.toUpperCase())) return true;
            }
        return false;
    }

    /**
     * Takes a stack and returns the "proper" name for it.
     * Proper meaning the name it's expected to be in data collection.
     * Example: OAK_LOG should just be log. OAK_PLANK should just be planks.
     */
    public static String itemToProperName(ItemStack stack) {
        String itemName = stack.getType().toString().toLowerCase(Locale.ROOT);
        if (itemName.contains("log")) {
            return "log";
        } else if (itemName.contains("planks")) {
            return "planks";
        } else {
            return itemName;
        }
    }

}

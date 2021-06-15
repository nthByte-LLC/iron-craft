package net.dohaw.diamondcraft.menu;

import net.dohaw.corelib.JPUtils;
import net.dohaw.corelib.menus.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RecipeMenu extends Menu implements Listener {

    public RecipeMenu(JavaPlugin plugin) {
        super(plugin, null, "Recipe Menu", 9);
        JPUtils.registerEvents(this);
    }

    @Override
    public void initializeItems(Player p) {
        inv.addItem(createGuiItem(Material.OAK_PLANKS, "&fOak Planks", Arrays.asList("----------", "&fClick me to see how to craft Oak Planks", "----------", "&aOak Planks are what you need to craft crafting tables", "&aYou can use any type of log to create planks")));
        inv.addItem(createGuiItem(Material.STICK, "&fSticks", Arrays.asList("----------", "&fClick me to see how to craft Sticks", "----------", "&aSticks are what you to craft tools like a pickaxe", "&aYou can use any type of planks in the game to craft this")));
        inv.addItem(createGuiItem(Material.CRAFTING_TABLE, "&fCrafting Table", Arrays.asList("----------", "&fClick me to see how to craft a Crafting Table", "----------", "&aCrafting Tables can be used to craft anything in the game", "&aThey can be used to craft pickaxes and furnaces for this tutorial")));
        inv.addItem(createGuiItem(Material.WOODEN_PICKAXE, "&fWooden Pickaxe", Arrays.asList("----------", "&fClick me to see how to craft a Wooden Pickaxe", "----------", "&aWooden Pickaxes can mine anything in the game, but are best at", "&amining things like stone. You usually won't use it for very long")));
        inv.addItem(createGuiItem(Material.STONE_PICKAXE, "&fStone Pickaxe", Arrays.asList("----------", "&fClick me to see how to craft a Stone Pickaxe", "----------", "&aStone Pickaxes can mine anything in the game, but are best at", "&amining things like stone, and iron ore", "&aThis is better than a wooden pickaxe")));
        inv.addItem(createGuiItem(Material.FURNACE, "&fFurnace", Arrays.asList("----------", "&fClick me to see how to craft a Furnace", "----------", "&aFurnaces smelt things like ores.")));
        inv.addItem(createGuiItem(Material.IRON_PICKAXE, "&fIron Pickaxe", Arrays.asList("----------", "&fClick me to see how to craft a Iron Pickaxe", "----------", "&aIron Pickaxes can mine anything in the game, but are best at", "&amining things like stone, diamond ore", "&aThis is better than a stone pickaxe")));
        this.fillerMat = Material.BLACK_STAINED_GLASS_PANE;
        fillMenu(false);
    }

    @EventHandler
    @Override
    protected void onInventoryClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        Inventory clickedInventory = e.getClickedInventory();
        Inventory topInventory = player.getOpenInventory().getTopInventory();

        if (clickedInventory == null) return;
        if (!topInventory.equals(inv) || !clickedInventory.equals(topInventory)) return;
        if (clickedItem == null) return;

        e.setCancelled(true);

        if(this.fillerMat == clickedItem.getType()) return;

        Material clickedItemType = clickedItem.getType();
        Map<Integer, Material> recipeIncredients = getRecipeIngredients(clickedItemType);
        if(recipeIncredients.isEmpty()) return;

        boolean isCraftedInTable = recipeIncredients.size() > 4;
        Recipe recipe = new Recipe(clickedItemType, recipeIncredients, isCraftedInTable);

        RecipeCraftingMenu newMenu = new RecipeCraftingMenu(plugin, this, "Press Arrow to go back", recipe);
        newMenu.initializeItems(player);
        player.closeInventory();
        newMenu.openInventory(player);

    }

    private Map<Integer, Material> getRecipeIngredients(Material result){
        Map<Integer, Material> recipe = new HashMap<>();
        switch(result){
            case OAK_PLANKS:
                recipe.put(2, Material.OAK_LOG);
                break;
            case STICK:
                recipe.put(1, Material.OAK_PLANKS);
                recipe.put(2, Material.OAK_PLANKS);
                break;
            case CRAFTING_TABLE:
                recipe.put(0, Material.OAK_PLANKS);
                recipe.put(1, Material.OAK_PLANKS);
                recipe.put(2, Material.OAK_PLANKS);
                recipe.put(3, Material.OAK_PLANKS);
                break;
            case WOODEN_PICKAXE:
                recipe.put(0, Material.OAK_PLANKS);
                recipe.put(1, Material.OAK_PLANKS);
                recipe.put(2, Material.OAK_PLANKS);
                recipe.put(4, Material.STICK);
                recipe.put(7, Material.STICK);
                break;
            case STONE_PICKAXE:
                recipe.put(0, Material.COBBLESTONE);
                recipe.put(1, Material.COBBLESTONE);
                recipe.put(2, Material.COBBLESTONE);
                recipe.put(4, Material.STICK);
                recipe.put(7, Material.STICK);
                break;
            case FURNACE:
                recipe.put(0, Material.COBBLESTONE);
                recipe.put(1, Material.COBBLESTONE);
                recipe.put(2, Material.COBBLESTONE);
                recipe.put(3, Material.COBBLESTONE);
                recipe.put(6, Material.COBBLESTONE);
                recipe.put(7, Material.COBBLESTONE);
                recipe.put(8, Material.COBBLESTONE);
                recipe.put(5, Material.COBBLESTONE);
                break;
            case IRON_PICKAXE:
                recipe.put(0, Material.IRON_INGOT);
                recipe.put(1, Material.IRON_INGOT);
                recipe.put(2, Material.IRON_INGOT);
                recipe.put(4, Material.STICK);
                recipe.put(7, Material.STICK);
                break;
        }
        return recipe;
    }

}

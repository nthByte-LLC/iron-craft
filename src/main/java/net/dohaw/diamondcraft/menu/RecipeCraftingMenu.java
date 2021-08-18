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

import java.util.Map;

public class RecipeCraftingMenu extends Menu implements Listener {

    private int[] recipeGrid;

    private final Recipe recipe;

    public RecipeCraftingMenu(JavaPlugin plugin, Menu previousMenu, String menuTitle, Recipe recipe) {
        super(plugin, previousMenu, menuTitle, 54);
        this.recipe = recipe;
        JPUtils.registerEvents(this);
    }

    @Override
    public void initializeItems(Player p) {

        if (recipe.isCraftedInTable()) {
            recipeGrid = new int[]{12, 13, 14, 21, 22, 23, 30, 31, 32};
        } else {
            recipeGrid = new int[]{16, 15, 24, 25};
        }

        for (Map.Entry<Integer, Material> entry : recipe.getIngredients().entrySet()) {
            int indexRecipeGrid = entry.getKey();
            Material ingredientMaterial = entry.getValue();
            inv.setItem(recipeGrid[indexRecipeGrid], new ItemStack(ingredientMaterial));
        }

        this.backMat = Material.ARROW;
        this.fillerMat = Material.BLACK_STAINED_GLASS_PANE;

        fillMenu(true, recipeGrid);

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

        if (this.fillerMat == clickedItem.getType()) return;

        if (clickedItem.getType() == backMat) {
            player.closeInventory();
            previousMenu.openInventory(player);
        }

    }

}

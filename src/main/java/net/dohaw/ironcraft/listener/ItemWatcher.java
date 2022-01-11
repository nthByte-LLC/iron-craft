package net.dohaw.ironcraft.listener;

import net.dohaw.ironcraft.CraftUtil;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.data_collection.DataCollectionUtil;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class ItemWatcher implements Listener {

    private IronCraftPlugin plugin;

    public ItemWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    /*
        Deals with gain order index when the player picks up an item.
     */
    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {
            PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
            ItemStack item = e.getItem().getItemStack();
            if (DataCollectionUtil.isTrackedItem(item) && !data.isInTutorial() && !data.isManager()) {
                dealWithGainData(data, item);
                data.addAccumulatedItem(item);
            }
        }

    }

    /*
        Deals with gain order index when the player crafts an item.
     */
    @EventHandler
    public void onPlayerCraftTrackedItem(CraftItemEvent e) {

        HumanEntity entity = e.getWhoClicked();
        if (!(entity instanceof Player)) {
            return;
        }

        ItemStack craftedItem = e.getRecipe().getResult().clone();
        if(e.getSlotType() != InventoryType.SlotType.RESULT) return;
        if(craftedItem == null) return;

        int recipeAmount = craftedItem.getAmount();
        ClickType click = e.getClick();
        switch (click) {
            case NUMBER_KEY:
                // If hotbar slot selected is full, crafting fails (vanilla behavior, even when
                // items match)
                if (e.getWhoClicked().getInventory().getItem(e.getHotbarButton()) != null)
                    recipeAmount = 0;
                break;

            case DROP:
            case CONTROL_DROP:
                // If we are holding items, craft-via-drop fails (vanilla behavior)
                ItemStack cursor = e.getCursor();
                // Apparently, rather than null, an empty cursor is AIR. I don't think that's
                // intended.
                if (cursor.getType() != Material.AIR)
                    recipeAmount = 0;
                break;

            case SHIFT_RIGHT:
            case SHIFT_LEFT:
                // Fixes ezeiger92/QuestWorld2#40
                if (recipeAmount == 0)
                    break;

                int maxCraftable = CraftUtil.getMaxCraftAmount(e.getInventory());
                int capacity = CraftUtil.fits(craftedItem, e.getView().getBottomInventory());

                // If we can't fit everything, increase "space" to include the items dropped by
                // crafting
                // (Think: Uncrafting 8 iron blocks into 1 slot)
                if (capacity < maxCraftable)
                    maxCraftable = ((capacity + recipeAmount - 1) / recipeAmount) * recipeAmount;

                recipeAmount = maxCraftable;
                break;
            default:
        }

        // No use continuing if we haven't actually crafted a thing
        if (recipeAmount == 0)
            return;

        craftedItem.setAmount(recipeAmount);

        PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
        if (!data.isInTutorial() && !data.isManager() && DataCollectionUtil.isTrackedItem(craftedItem)) {
            dealWithGainData(data, craftedItem);
            data.addAccumulatedItem(craftedItem);
        }

    }

    @EventHandler
    public void onPlayerCraftUselessItem(CraftItemEvent e) {

        HumanEntity entity = e.getWhoClicked();
        if (!(entity instanceof Player)) {
            return;
        }

        PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
        if (!data.isInTutorial() && !data.isManager()) {
            Material materialCrafted = e.getRecipe().getResult().getType();
            if (materialCrafted == Material.IRON_AXE) {
                data.getIsUselessToolCrafted().put("iron_axe", true);
            } else if (materialCrafted == Material.STONE_AXE) {
                data.getIsUselessToolCrafted().put("stone_axe", true);
            } else if (materialCrafted == Material.WOODEN_AXE) {
                data.getIsUselessToolCrafted().put("wooden_axe", true);
            }
        }

    }

    // NOTE: This could pose problems in the future. If you run into any catastrophic issues. Look at this method.
    //TODO: Give it another name
    private void dealWithGainData(PlayerData data, ItemStack item) {
        String properItemName = DataCollectionUtil.itemToProperName(item);
        int currentGainIndex = data.getItemToGainIndex().get(properItemName);
        // Will be 0 if the gain index hasn't been set yet.
        if (currentGainIndex == 0) {
            int nextGainIndex = data.getNextGainIndex();
            data.getItemToTimeStepGained().put(properItemName, data.getDurationSteps());
            // Dirty way of preventing a future issue with sparse rewards (Sparse and Dense Reward functionality rely on the contents of the gain index map contents.)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                data.getItemToGainIndex().put(properItemName, nextGainIndex);
            }, 20);
        }
    }

}

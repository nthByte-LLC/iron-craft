package net.dohaw.ironcraft.listener;

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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemWatcher implements Listener {

    private final IronCraftPlugin plugin;

    public ItemWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {
            PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
            ItemStack item = e.getItem().getItemStack();
            if (DataCollectionUtil.isTrackedItem(item) && !data.isInTutorial() && !data.isManager()) {
                dealWithGainData(data, item);
            }
        }

    }

    @EventHandler
    public void onPlayerCraftTrackedItem(CraftItemEvent e) {

        HumanEntity entity = e.getWhoClicked();
        if (!(entity instanceof Player)) {
            return;
        }

        ItemStack craftedItem = e.getRecipe().getResult();
        PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
        if (!data.isInTutorial() && !data.isManager() && DataCollectionUtil.isTrackedItem(craftedItem)) {
            dealWithGainData(data, craftedItem);
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

    // NOTE: This could pose problems in the future. If you run into any catostrophic issues. Look at this method.
    //TODO: Give it another name
    private void dealWithGainData(PlayerData data, ItemStack item) {
        String properItemName = DataCollectionUtil.itemToProperName(item);
        int currentGainIndex = data.getItemToGainIndex().get(properItemName);
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

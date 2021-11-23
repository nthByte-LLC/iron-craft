package net.dohaw.ironcraft.listener;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.data_collection.DataCollector;
import net.dohaw.ironcraft.playerdata.PlayerData;
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

    private IronCraftPlugin plugin;

    public ItemWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if(entity instanceof Player){
            PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
            ItemStack item = e.getItem().getItemStack();
            if(DataCollector.isTrackedItem(item) && !data.isInTutorial() && !data.isManager()){
                dealWithGainData(data, item);
            }
        }

    }

    @EventHandler
    public void onPlayerCraftTrackedItem(CraftItemEvent e){

        HumanEntity entity = e.getWhoClicked();
        if(!(entity instanceof Player)){
            return;
        }

        ItemStack craftedItem = e.getRecipe().getResult();
        PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
        if(!data.isInTutorial() && !data.isManager() && DataCollector.isTrackedItem(craftedItem)){
            dealWithGainData(data, craftedItem);
        }

    }

    @EventHandler
    public void onPlayerCraftUselessItem(CraftItemEvent e){

        HumanEntity entity = e.getWhoClicked();
        if(!(entity instanceof Player)){
            return;
        }

        PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
        if(!data.isInTutorial() && !data.isManager()){
            Material materialCrafted = e.getRecipe().getResult().getType();
            if(materialCrafted == Material.IRON_AXE){
                data.getIsUselessToolCrafted().put("iron_axe", true);
            }else if(materialCrafted == Material.STONE_AXE){
                data.getIsUselessToolCrafted().put("stone_axe", true);
            }else if(materialCrafted == Material.WOODEN_AXE){
                data.getIsUselessToolCrafted().put("wooden_axe", true);
            }
        }

    }

    //TODO: Give it another name
    private void dealWithGainData(PlayerData data, ItemStack item){
        String trackedItemName = item.getType().toString().toLowerCase();
        if(trackedItemName.contains("planks")){
            trackedItemName = "planks";
        }else if(trackedItemName.contains("log")){
            trackedItemName = "log";
        }

        int currentGainIndex = data.getItemToGainIndex().get(trackedItemName);
        if(currentGainIndex == 0){
            int nextGainIndex = data.getNextGainIndex();
            // Replaces the 0 with whatever the next gain index is.
            data.getItemToGainIndex().put(trackedItemName, nextGainIndex);
            data.getItemToTimeStepGained().put(trackedItemName, data.getCurrentStep());
        }
    }

}

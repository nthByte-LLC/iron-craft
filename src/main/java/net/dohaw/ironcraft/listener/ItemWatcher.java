package net.dohaw.ironcraft.listener;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.data_collection.DataCollector;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemWatcher implements Listener {

    IronCraftPlugin plugin;

    public ItemWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onPlayerPickup(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if(entity instanceof Player){
            PlayerData data = plugin.getPlayerDataHandler().getData(entity.getUniqueId());
            ItemStack item = e.getItem().getItemStack();
            if(DataCollector.isTrackedItem(item) && !data.isInTutorial() && !data.isManager()){

                String trackedItem = item.getType().toString().toLowerCase();
                if(trackedItem.contains("log") ){
                    trackedItem = "log";
                }else if(trackedItem.contains("planks")){
                    trackedItem = "planks";
                }

                int currentGainOrder = data.getGainOrderData().get(trackedItem);
                if(currentGainOrder == 0){
                    int nextGainIndex = data.getNextGainIndex();
                    // Replaces the 0 with whatever the next gain index is.
                    data.getGainOrderData().put(trackedItem, nextGainIndex);
                }

            }
        }

    }

    @EventHandler
    public void onPlayerCraft(CraftItemEvent e){

        HumanEntity entity = e.getWhoClicked();
        if(!(entity instanceof Player)){
            return;
        }

        Player player = (Player) entity;
        PlayerData data = plugin.getPlayerDataHandler().getData(player.getUniqueId());
        if(!data.isInTutorial() && !data.isManager()){
            Material materialCrafted = e.getRecipe().getResult().getType();
            if(materialCrafted == Material.IRON_AXE){
                data.getIsUselessToolCrafted().set(0, true);
            }else if(materialCrafted == Material.STONE_AXE){
                data.getIsUselessToolCrafted().set(1, true);
            }else if(materialCrafted == Material.WOODEN_AXE){
                data.getIsUselessToolCrafted().set(2, true);
            }
        }

    }

}

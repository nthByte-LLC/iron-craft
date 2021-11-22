package net.dohaw.ironcraft.listener;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
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

        Player player;
        if (e.getEntity() instanceof Player) {
            player = (Player) e.getEntity();
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

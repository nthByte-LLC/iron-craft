package net.dohaw.diamondcraft.listener;

import net.dohaw.corelib.StringUtils;
import net.dohaw.diamondcraft.DiamondCraftPlugin;
import net.dohaw.diamondcraft.TutorialObjective;
import net.dohaw.diamondcraft.handler.PlayerDataHandler;
import net.dohaw.diamondcraft.playerdata.PlayerData;
import net.dohaw.diamondcraft.prompt.RepeatTutorialPrompt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.UUID;

public class ObjectiveWatcher implements Listener {

    private HashSet<UUID> hasMovedForFirstTime = new HashSet<>();

    private DiamondCraftPlugin plugin;
    private PlayerDataHandler playerDataHandler;

    public ObjectiveWatcher(DiamondCraftPlugin plugin){
        this.plugin = plugin;
        this.playerDataHandler = plugin.getPlayerDataHandler();
    }

    @EventHandler
    public void onMineWood(EntityPickupItemEvent e){

        LivingEntity entity = e.getEntity();
        if(entity instanceof Player){

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            if(isOnObjective(playerData, TutorialObjective.COLLECT_WOOD) && e.getItem().getItemStack().getType() == Material.OAK_LOG){
                if(getCountItem(player.getInventory(), Material.OAK_LOG) >= 4){
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(TutorialObjective.COLLECT_WOOD));
                }
            }

        }
    }

    /* Cancels any damage the player takes */
    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){

        Player player = e.getPlayer();
        if(playerDataHandler.hasDataLoaded(player.getUniqueId())){
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            if(playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == TutorialObjective.MOVE && !hasMovedForFirstTime.contains(player.getUniqueId())) {
                hasMovedForFirstTime.add(player.getUniqueId());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    playerData.setCurrentTutorialObjective(plugin, TutorialObjective.COLLECT_WOOD);
                    hasMovedForFirstTime.remove(player.getUniqueId());
                }, 20L * 20);
            }
        }

    }

    @EventHandler
    public void onCraftSticks(CraftItemEvent e) {
        checkIfPassedCraftingObjective(e, TutorialObjective.MAKE_STICKS, Material.STICK);
        checkIfPassedCraftingObjective(e, TutorialObjective.MAKE_PLANKS, Material.OAK_PLANKS);
        checkIfPassedCraftingObjective(e, TutorialObjective.MAKE_CRAFTING_TABLE, Material.CRAFTING_TABLE);
        checkIfPassedCraftingObjective(e, TutorialObjective.MAKE_WOODEN_PICKAXE, Material.WOODEN_PICKAXE);
        checkIfPassedCraftingObjective(e, TutorialObjective.MAKE_STONE_PICKAXE, Material.STONE_PICKAXE);
        checkIfPassedCraftingObjective(e, TutorialObjective.MAKE_FURNACE, Material.FURNACE);
        checkIfPassedCraftingObjective(e, TutorialObjective.MAKE_IRON_PICKAXE, Material.IRON_PICKAXE);
    }

    @EventHandler
    public void onPlaceCraftingTable(BlockPlaceEvent e){

        Player player = e.getPlayer();
        PlayerData playerData = getPlayerData(player);
        if(playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == TutorialObjective.PLACE_CRAFTING_TABLE && e.getBlockPlaced().getType() == Material.CRAFTING_TABLE){
            playerData.setCurrentTutorialObjective(plugin, getNextObjective(TutorialObjective.PLACE_CRAFTING_TABLE));
        }

    }

    @EventHandler
    public void onCollectCobblestone(EntityPickupItemEvent e){

        LivingEntity entity = e.getEntity();
        if(entity instanceof Player){

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            if(isOnObjective(playerData, TutorialObjective.COLLECT_STONE) && e.getItem().getItemStack().getType() == Material.COBBLESTONE){
                if(getCountItem(player.getInventory(), Material.COBBLESTONE) >= 15){
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(TutorialObjective.COLLECT_STONE));
                }
            }

        }

    }

    @EventHandler
    public void onCollectIron(EntityPickupItemEvent e){

        LivingEntity entity = e.getEntity();
        if(entity instanceof Player){

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            if(isOnObjective(playerData, TutorialObjective.COLLECT_IRON) && e.getItem().getItemStack().getType() == Material.IRON_ORE){
                if(getCountItem(player.getInventory(), Material.IRON_ORE) >= 3){
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(TutorialObjective.COLLECT_IRON));
                }
            }

        }

    }

    @EventHandler
    public void onTakeSmeltedIron(FurnaceExtractEvent e){

        Player player = e.getPlayer();
        PlayerData playerData = getPlayerData(player);
        if(isOnObjective(playerData, TutorialObjective.SMELT_IRON) && e.getItemType() == Material.IRON_INGOT){
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if(getCountItem(player.getInventory(), Material.IRON_INGOT) + e.getItemAmount() >= 3){
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(TutorialObjective.SMELT_IRON));
                }
            }, 1);
        }

    }

    @EventHandler
    public void onCollectionDiamond(EntityPickupItemEvent e){

        LivingEntity entity = e.getEntity();
        if(entity instanceof Player){

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            System.out.println("OBJ: " + playerData.getCurrentTutorialObjective());
            if(isOnObjective(playerData, TutorialObjective.COLLECT_DIAMOND) && e.getItem().getItemStack().getType() == Material.DIAMOND){
                System.out.println("HERE");
                if(getCountItem(player.getInventory(), Material.DIAMOND) >= 1){

                    player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 30, 1, 1, 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 1);
                    player.sendMessage(StringUtils.colorString("&bCongratulations! &fYou have completed the tutorial. You will now be teleported and given the opportunity to go and find your own diamond! Good luck!"));

                    ConversationFactory cf = new ConversationFactory(plugin);
                    Conversation conversation = cf.withFirstPrompt(new RepeatTutorialPrompt(plugin)).withLocalEcho(false).buildConversation(player);
                    conversation.begin();


                }
            }

        }

    }


    private int getCountItem(PlayerInventory playerInventory, Material material){
        int count = 1;
        ItemStack[] contents = playerInventory.getContents();
        for(ItemStack stack : contents){
            if(stack != null){
                if(stack.getType() == material){
                    count += stack.getAmount();
                }
            }
        }
        return count;
    }

    private void checkIfPassedCraftingObjective(CraftItemEvent e, TutorialObjective objective, Material checkedMaterial){

        HumanEntity whoClicked = e.getWhoClicked();
        if(whoClicked instanceof Player){

            Player player = (Player) whoClicked;
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            System.out.println("OBJECTIVE: " + objective);
            System.out.println("PLAYER OBJECTIVE: " + playerData.getCurrentTutorialObjective());
            System.out.println("RESULT: " + e.getRecipe().getResult().getType());
            if(playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == objective && e.getRecipe().getResult().getType() == checkedMaterial){
                playerData.setCurrentTutorialObjective(plugin, getNextObjective(objective));
            }

        }

    }

    private TutorialObjective getNextObjective(TutorialObjective currentObjective){
        int ordinal = currentObjective.ordinal();
        TutorialObjective[] values = TutorialObjective.values();
        return values[ordinal + 1];
    }

    private PlayerData getPlayerData(Player player){
        return playerDataHandler.getData(player.getUniqueId());
    }

    private boolean isOnObjective(PlayerData playerData, TutorialObjective objective){
        return playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == objective;
    }

}

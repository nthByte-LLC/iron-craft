package com.nthbyte.ironcraft.listener;

import com.nthbyte.ironcraft.IronCraftPlugin;
import com.nthbyte.ironcraft.Objective;
import com.nthbyte.ironcraft.Reason;
import com.nthbyte.ironcraft.event.CompleteObjectiveEvent;
import net.dohaw.corelib.StringUtils;
import com.nthbyte.ironcraft.event.EndGameEvent;
import com.nthbyte.ironcraft.handler.PlayerDataHandler;
import com.nthbyte.ironcraft.PlayerData;
import com.nthbyte.ironcraft.prompt.RepeatTutorialPrompt;
import org.bukkit.*;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ObjectiveWatcher implements Listener {

    private final HashSet<UUID> hasMovedForFirstTime = new HashSet<>();

    private final IronCraftPlugin plugin;
    private final PlayerDataHandler playerDataHandler;

    public ObjectiveWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
        this.playerDataHandler = plugin.getPlayerDataHandler();
    }

    // Removes the player from the hashset to prevent any future issues (In case they left, WHILE they were in the act of completing the Move objective)
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        hasMovedForFirstTime.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMineWood(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            if(playerData.isAdmin() || playerData.isManager()) {
                return;
            }

            Material itemType = e.getItem().getItemStack().getType();
            if (isOnObjective(playerData, Objective.COLLECT_WOOD) && itemType.toString().toLowerCase().contains("log")) {
                if (getCountItem(player.getInventory(), itemType) >= 4) {
                    playerData.setCurrentTutorialObjective(plugin.getNextObjective(Objective.COLLECT_WOOD));
                    Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
                }
            }

        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        if (playerDataHandler.hasDataLoaded(player.getUniqueId())) {
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            if(playerData.isAdmin() || playerData.isManager()) {
                return;
            }
            if (playerData.getCurrentTutorialObjective() == Objective.MOVE && !hasMovedForFirstTime.contains(player.getUniqueId())) {
                hasMovedForFirstTime.add(player.getUniqueId());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if(!player.isOnline() || !player.isValid()){
                        return;
                    }
                    playerData.setCurrentTutorialObjective(plugin.getNextObjective(Objective.MOVE));
                    hasMovedForFirstTime.remove(player.getUniqueId());
                    Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
                }, 20L * 20);
            }
        }

    }

    @EventHandler
    public void onCraftSticks(CraftItemEvent e) {

        Player player = (Player) e.getWhoClicked();
        PlayerData pd = playerDataHandler.getData(player);

        if(pd.isAdmin() || pd.isManager()) {
            return;
        }

        Material craftedItemType = e.getRecipe().getResult().getType();
        boolean isInTutorial = pd.isInTutorial();
        // End of the game. They have just crafted an iron pick-axe.
        if(!isInTutorial && craftedItemType == Material.IRON_PICKAXE){
            // It's about to turn to 3.
            if(pd.getRoundsPlayed() == 2){
                Bukkit.getServer().getPluginManager().callEvent(new EndGameEvent(Reason.GAME_COMPLETE, pd));
            }else{
                Bukkit.getServer().getPluginManager().callEvent(new EndGameEvent(Reason.ROUND_COMPLETE, pd));
            }
            return;
        }

        if(!isInTutorial) return;

        checkIfPassedCraftingObjective(e, Objective.MAKE_STICKS, Material.STICK);
        checkIfPassedCraftingObjective(e, Objective.MAKE_PLANKS, Material.OAK_PLANKS, Material.BIRCH_PLANKS, Material.SPRUCE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS, Material.JUNGLE_PLANKS);
        checkIfPassedCraftingObjective(e, Objective.MAKE_CRAFTING_TABLE, Material.CRAFTING_TABLE);
        checkIfPassedCraftingObjective(e, Objective.MAKE_WOODEN_PICKAXE, Material.WOODEN_PICKAXE);
        checkIfPassedCraftingObjective(e, Objective.MAKE_STONE_PICKAXE, Material.STONE_PICKAXE);
        checkIfPassedCraftingObjective(e, Objective.MAKE_FURNACE, Material.FURNACE);
        checkIfPassedCraftingObjective(e, Objective.MAKE_IRON_PICKAXE, Material.IRON_PICKAXE);
    }

    @EventHandler
    public void onPlaceCraftingTable(BlockPlaceEvent e) {

        Player player = e.getPlayer();
        PlayerData playerData = getPlayerData(player);

        if(playerData.isAdmin() || playerData.isManager()) {
            return;
        }

        if (playerData.getCurrentTutorialObjective() == Objective.PLACE_CRAFTING_TABLE && e.getBlockPlaced().getType() == Material.CRAFTING_TABLE) {
            playerData.setCurrentTutorialObjective(plugin.getNextObjective(Objective.PLACE_CRAFTING_TABLE));
            Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
        }

    }

    @EventHandler
    public void onPlaceTorch(BlockPlaceEvent e) {

        Player player = e.getPlayer();
        PlayerData playerData = getPlayerData(player);

        if(playerData.isAdmin() || playerData.isManager()) {
            return;
        }

        Material blockPlacedType = e.getBlockPlaced().getType();
        if (playerData.getCurrentTutorialObjective() == Objective.PLACE_A_TORCH && (blockPlacedType == Material.TORCH || blockPlacedType == Material.WALL_TORCH) ) {
            playerData.setCurrentTutorialObjective(plugin.getNextObjective(Objective.PLACE_A_TORCH));
            Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
        }

    }

    @EventHandler
    public void onCollectCobblestone(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);

            if(playerData.isAdmin() || playerData.isManager()) {
                return;
            }

            if (isOnObjective(playerData, Objective.COLLECT_STONE) && e.getItem().getItemStack().getType() == Material.COBBLESTONE) {
                if (getCountItem(player.getInventory(), Material.COBBLESTONE) >= 15) {
                    playerData.setCurrentTutorialObjective(plugin.getNextObjective(Objective.COLLECT_STONE));
                    Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
                }
            }

        }

    }

    @EventHandler
    public void onCollectIron(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);

            if(playerData.isAdmin() || playerData.isManager()) {
                return;
            }

            if (isOnObjective(playerData, Objective.COLLECT_IRON) && e.getItem().getItemStack().getType() == Material.RAW_IRON) {
                if (getCountItem(player.getInventory(), Material.RAW_IRON) >= 3) {
                    playerData.setCurrentTutorialObjective(plugin.getNextObjective(Objective.COLLECT_IRON));
                    Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
                }
            }

        }

    }

    @EventHandler
    public void onTakeSmeltedIron(FurnaceExtractEvent e) {

        Player player = e.getPlayer();
        PlayerData playerData = getPlayerData(player);

        if(playerData.isAdmin() || playerData.isManager()) {
            return;
        }

        if (isOnObjective(playerData, Objective.SMELT_IRON) && e.getItemType() == Material.IRON_INGOT) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (getCountItem(player.getInventory(), Material.IRON_INGOT) + e.getItemAmount() >= 3) {
                    playerData.setCurrentTutorialObjective(plugin.getNextObjective(Objective.SMELT_IRON));
                    Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
                }
            }, 1);
        }

    }

    @EventHandler
    public void onCompleteObjective(CompleteObjectiveEvent e){

        PlayerData playerData = e.getPlayerData();

        if(playerData.isAdmin() || playerData.isManager()) {
            return;
        }

        if(playerData.isInTutorial()){

            Player player = playerData.getPlayer();
            World world = player.getWorld();
            world.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

            Firework firework = (Firework) world.spawnEntity(player.getLocation().add(0, 1, 0), EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().withTrail().withFlicker().withFade(Color.YELLOW, Color.BLACK).withColor(Color.BLUE, Color.SILVER, Color.WHITE).build());
            firework.setFireworkMeta(meta);

            Bukkit.getScheduler().runTaskLater(plugin, firework::detonate, 10L);

        }

    }

    private int getCountItem(PlayerInventory playerInventory, Material material) {
        int count = 1;
        ItemStack[] contents = playerInventory.getContents();
        for (ItemStack stack : contents) {
            if (stack != null) {
                if (stack.getType() == material) {
                    count += stack.getAmount();
                }
            }
        }
        return count;
    }

    private void checkIfPassedCraftingObjective(CraftItemEvent e, Objective objective, Material... checkedMaterials) {

        HumanEntity whoClicked = e.getWhoClicked();
        if (whoClicked instanceof Player) {

            Player player = (Player) whoClicked;
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            Objective currentObjective = playerData.getCurrentTutorialObjective();
            List<Material> checkedMaterialsList = Arrays.asList(checkedMaterials);
            Material craftedItemType = e.getRecipe().getResult().getType();

            if (currentObjective == objective && checkedMaterialsList.contains(craftedItemType)) {
                if (currentObjective == Objective.MAKE_IRON_PICKAXE) {
                    // End of the tutorial. They have just crafted an iron pickaxe.
                    concludeTutorial(player);
                }else{
                    // Advances them forward to the next objective.
                    playerData.setCurrentTutorialObjective(plugin.getNextObjective(objective));
                    Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
                }
            }

        }

    }

    private PlayerData getPlayerData(Player player) {
        return playerDataHandler.getData(player.getUniqueId());
    }

    private boolean isOnObjective(PlayerData playerData, Objective objective) {
        return playerData.getCurrentTutorialObjective() == objective;
    }

    private void concludeTutorial(Player player) {

        player.getPersistentDataContainer().set(IronCraftPlugin.IN_SURVEY_PDC_KEY, PersistentDataType.STRING, "marker");
        player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 30, 1, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 1);
        player.sendMessage(StringUtils.colorString("&bCongratulations. &fYou have completed the tutorial!"));

        ConversationFactory cf = new ConversationFactory(plugin);
        Conversation conversation = cf.withFirstPrompt(new RepeatTutorialPrompt(plugin)).withLocalEcho(false).buildConversation(player);
        conversation.begin();

    }

}

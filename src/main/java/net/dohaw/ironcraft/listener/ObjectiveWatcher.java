package net.dohaw.ironcraft.listener;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.Objective;
import net.dohaw.ironcraft.handler.PlayerDataHandler;
import net.dohaw.ironcraft.menu.RecipeMenu;
import net.dohaw.ironcraft.playerdata.PlayerData;
import net.dohaw.ironcraft.prompt.RepeatTutorialPrompt;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.UUID;

public class ObjectiveWatcher implements Listener {

    private final HashSet<UUID> hasMovedForFirstTime = new HashSet<>();

    private final IronCraftPlugin plugin;
    private final PlayerDataHandler playerDataHandler;

    public ObjectiveWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
        this.playerDataHandler = plugin.getPlayerDataHandler();
    }

    @EventHandler
    public void onMineWood(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            if (isOnObjective(playerData, Objective.COLLECT_WOOD) && e.getItem().getItemStack().getType() == Material.OAK_LOG) {
                if (getCountItem(player.getInventory(), Material.OAK_LOG) >= 4) {
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.COLLECT_WOOD));
                }
            }

        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        Player player = e.getPlayer();
        if (playerDataHandler.hasDataLoaded(player.getUniqueId())) {
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            if (playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == Objective.MOVE && !hasMovedForFirstTime.contains(player.getUniqueId())) {
                hasMovedForFirstTime.add(player.getUniqueId());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.MOVE));
                    hasMovedForFirstTime.remove(player.getUniqueId());
                }, 20L * 20);
            }
        }

    }

    @EventHandler
    public void onCraftSticks(CraftItemEvent e) {
        checkIfPassedCraftingObjective(e, Objective.MAKE_STICKS, Material.STICK);
        checkIfPassedCraftingObjective(e, Objective.MAKE_PLANKS, Material.OAK_PLANKS);
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
        if (playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == Objective.PLACE_CRAFTING_TABLE && e.getBlockPlaced().getType() == Material.CRAFTING_TABLE) {
            playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.PLACE_CRAFTING_TABLE));
        }

    }

    @EventHandler
    public void onPlaceTorch(BlockPlaceEvent e) {

        Player player = e.getPlayer();
        PlayerData playerData = getPlayerData(player);
        if (playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == Objective.PLACE_A_TORCH && e.getBlockPlaced().getType() == Material.TORCH) {
            playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.PLACE_A_TORCH));
        }

    }

    @EventHandler
    public void onCollectCobblestone(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            if (isOnObjective(playerData, Objective.COLLECT_STONE) && e.getItem().getItemStack().getType() == Material.COBBLESTONE) {
                if (getCountItem(player.getInventory(), Material.COBBLESTONE) >= 15) {
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.COLLECT_STONE));
                }
            }

        }

    }

    @EventHandler
    public void onPlayerAttemptOpenRecipeMenu(PlayerInteractEvent e) {

        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

            ItemStack item = e.getItem();
            if (item != null && item.getType() == Material.PAPER) {

                ItemMeta itemMeta = item.getItemMeta();
                String colorLessDisplayName = StringUtils.removeChatColor(itemMeta.getDisplayName());
                // I know checking via display name is dirty and should use PDC, but 1. i'm lazy and 2. It doesn't really matter much given the context
                if (colorLessDisplayName.equalsIgnoreCase("Recipe Menu")) {

                    Player player = e.getPlayer();
                    RecipeMenu recipeMenu = new RecipeMenu(plugin);
                    recipeMenu.initializeItems(player);
                    recipeMenu.openInventory(player);

                    PlayerData playerData = getPlayerData(player);
                    if (isOnObjective(getPlayerData(player), Objective.OPEN_RECIPE_MENU)) {
                        playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.OPEN_RECIPE_MENU));
                    }

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
            if (isOnObjective(playerData, Objective.COLLECT_IRON) && e.getItem().getItemStack().getType() == Material.IRON_ORE) {
                if (getCountItem(player.getInventory(), Material.IRON_ORE) >= 3) {
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.COLLECT_IRON));
                }
            }

        }

    }

    @EventHandler
    public void onTakeSmeltedIron(FurnaceExtractEvent e) {

        Player player = e.getPlayer();
        PlayerData playerData = getPlayerData(player);
        if (isOnObjective(playerData, Objective.SMELT_IRON) && e.getItemType() == Material.IRON_INGOT) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (getCountItem(player.getInventory(), Material.IRON_INGOT) + e.getItemAmount() >= 3) {
                    playerData.setCurrentTutorialObjective(plugin, getNextObjective(Objective.SMELT_IRON));
                }
            }, 1);
        }

    }

    @EventHandler
    public void onCollectionDiamond(EntityPickupItemEvent e) {

        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) {

            Player player = (Player) entity;
            PlayerData playerData = getPlayerData(player);
            System.out.println("OBJ: " + playerData.getCurrentTutorialObjective());
            if (isOnObjective(playerData, Objective.COLLECT_DIAMOND) && e.getItem().getItemStack().getType() == Material.DIAMOND) {
                System.out.println("HERE");
                if (getCountItem(player.getInventory(), Material.DIAMOND) >= 1) {

                    System.out.println("PLAYER HAS DIAMONDS");
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

    private void checkIfPassedCraftingObjective(CraftItemEvent e, Objective objective, Material checkedMaterial) {

        HumanEntity whoClicked = e.getWhoClicked();
        if (whoClicked instanceof Player) {

            Player player = (Player) whoClicked;
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            System.out.println("OBJECTIVE: " + objective);
            System.out.println("PLAYER OBJECTIVE: " + playerData.getCurrentTutorialObjective());
            System.out.println("RESULT: " + e.getRecipe().getResult().getType());
            if (playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == objective && e.getRecipe().getResult().getType() == checkedMaterial) {
                playerData.setCurrentTutorialObjective(plugin, getNextObjective(objective));
            }

        }

    }

    private Objective getNextObjective(Objective currentObjective) {
        int ordinal = currentObjective.ordinal();
        Objective[] values = Objective.values();
        return values[ordinal + 1];
    }

    private PlayerData getPlayerData(Player player) {
        return playerDataHandler.getData(player.getUniqueId());
    }

    private boolean isOnObjective(PlayerData playerData, Objective objective) {
        return playerData.isInTutorial() && playerData.getCurrentTutorialObjective() == objective;
    }

}

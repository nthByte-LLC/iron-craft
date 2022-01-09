package net.dohaw.ironcraft.listener;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.EndGameEvent;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.handler.PlayerDataHandler;
import net.dohaw.ironcraft.playerdata.PlayerData;
import net.dohaw.ironcraft.prompt.IDPrompt;
import net.dohaw.ironcraft.prompt.autonomysurvey.AutonomySurveyPrompt;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerWatcher implements Listener {

    private final List<Material> BREAKABLE_TUTORIAL_BLOCKS = Arrays.asList(Material.STONE, Material.OAK_LOG,
            Material.OAK_LEAVES, Material.IRON_ORE, Material.DIAMOND_ORE, Material.GRASS_BLOCK, Material.DIRT, Material.CRAFTING_TABLE, Material.FURNACE
    );

    private final IronCraftPlugin plugin;

    public PlayerWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    // Doesn't let them move if they haven't answered the autonomy survey
    @EventHandler
    public static void onPlayerMoveDuringSurvey(PlayerMoveEvent e) {
        PersistentDataContainer pdc = e.getPlayer().getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.minecraft("is-answering-survey");
//        if (pdc.has(key, PersistentDataType.STRING) && hasMoved(e.getTo(), e.getFrom(), true)) {
//            e.setCancelled(true);
//        }
    }

    private static boolean hasMoved(Location to, Location from, boolean checkY) {
        if (to != null) {
            boolean hasMovedHorizontally = from.getX() != to.getX() || from.getZ() != to.getZ();
            if (!hasMovedHorizontally && checkY) {
                return from.getY() != to.getY();
            }
            return hasMovedHorizontally;
        }
        return false;
    }

    /**
     * Determines if a player uses the wrong tool (a wooden pickaxe) to dig iron ore.
     *
     * @param e Event of the player breaking a block
     */
    @EventHandler
    public void onPlayerMineIronOre(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Material block = e.getBlock().getType();
        Material tool = Objects.requireNonNull(p.getItemInUse()).getType();
        PlayerData playerData = plugin.getPlayerDataHandler().getData(p.getUniqueId());

        if (block == Material.IRON_ORE && tool == Material.WOODEN_PICKAXE) {
            playerData.incrementMisuseActionSteps();
        }
    }

    /**
     * Determines if a player uses the wrong tool (a stone pickaxe) to dig a log.
     *
     * @param e Event of the player breaking a block
     */
    @EventHandler
    public void onPlayerMineLog(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Material block = e.getBlock().getType();
        Material tool = Objects.requireNonNull(p.getItemInUse()).getType();
        PlayerData playerData = plugin.getPlayerDataHandler().getData(p.getUniqueId());

        if (block == Material.OAK_LOG || block == Material.ACACIA_LOG || block == Material.BIRCH_LOG || block == Material.DARK_OAK_LOG || block == Material.JUNGLE_LOG || block == Material.SPRUCE_LOG && tool == Material.STONE_PICKAXE) {
            playerData.incrementMisuseActionSteps();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        player.sendTitle("Input your ID", "Press T on your keyboard", 5, 100, 20);
        //player.sendMessage("Once you press T, you should be allowed to type in chat and input your ID!");

        ConversationFactory conversationFactory = new ConversationFactory(plugin);
        Conversation conversation = conversationFactory.withFirstPrompt(new IDPrompt(plugin)).withLocalEcho(false).buildConversation(player);
        conversation.begin();

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {

        Player player = e.getPlayer();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        UUID playerUUID = player.getUniqueId();
        if (playerDataHandler.hasDataLoaded(playerUUID)) {
            plugin.getPlayerDataHandler().saveData(playerUUID);
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        if (!playerDataHandler.hasDataLoaded(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
        if (playerData.isInTutorial() && !BREAKABLE_TUTORIAL_BLOCKS.contains(e.getBlock().getType())) {
            e.setCancelled(true);
            player.sendMessage("You can't break that block. Focus on the objectives!");
        }

    }

    /*
        Doesn't let them move if they don't have player data loaded.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!plugin.getPlayerDataHandler().hasDataLoaded(e.getPlayer().getUniqueId()) && PlayerWatcher.hasMoved(e.getTo(), e.getFrom(), true)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent e) {

        World applicableWorld = plugin.getBaseConfig().getWorld();
        if(applicableWorld == null) return;

        String applicableWorldName = applicableWorld.getName();
        Entity entity = e.getEntity();
        if (entity instanceof Player && entity.getLocation().getWorld().getName().equalsIgnoreCase(applicableWorldName)) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerMineDiamond(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block blockMined = e.getBlock();
        if (blockMined.getType() == Material.DIAMOND_ORE) {
            PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            if (!playerData.isInTutorial()) {
                player.sendMessage(StringUtils.colorString("&aCongratulations! &fYou have successfully completed the game!"));
                player.sendMessage("You will now take a survey. You won't be able to move for the duration of this survey. Don't worry, it'll be quick!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getPersistentDataContainer().set(NamespacedKey.minecraft("is-answering-survey"), PersistentDataType.STRING, "marker");
                    Conversation conv = new ConversationFactory(plugin).withFirstPrompt(new AutonomySurveyPrompt(0, playerDataHandler)).withLocalEcho(false).buildConversation(player);
                    conv.begin();
                }, 20L * 3);
            }
        }

    }

    @EventHandler
    public void EndGameEvent(EndGameEvent e) {
        PlayerData playerData = e.getPlayerData();
        playerData.setEquipmentMisuseRatio((float) playerData.getMisuseActionSteps() / playerData.getDurationSteps());
    }

    /**
     * Keeps track of the items the player places.
     *
     * @param e The event
     */
    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {

        Player player = e.getPlayer();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());

        Material blockTypePlaced = e.getBlock().getType();
        if(blockTypePlaced == Material.TORCH || blockTypePlaced == Material.COBBLESTONE
                || blockTypePlaced == Material.DIRT || blockTypePlaced == Material.STONE){
            playerData.incrementPlacedItems(blockTypePlaced);
        }

    }

    /**
     * Sets a boolean to true if the player smelts coal.
     *
     * @param e The event
     */
    @EventHandler
    public void onPlayerSmelt(InventoryClickEvent e) {

        Player player = e.getWhoClicked() instanceof Player ? (Player) e.getWhoClicked() : null;
        if (player == null) return;

        // check if the player has clicked on the ingredient slot holding iron ore
        InventoryType.SlotType s = e.getSlotType();
        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        if (!(e.getCurrentItem().getType() == Material.IRON_ORE) && (s == InventoryType.SlotType.FUEL || s == InventoryType.SlotType.RESULT || s == InventoryType.SlotType.OUTSIDE || s == InventoryType.SlotType.CONTAINER)) {
            return;
        }

        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
        playerData.setHasSmeltedCoal(true);
    }

    /**
     * Increments the attack steps if a player isn't in the tutorial and they attack something.
     */
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {

        Entity eDamager = e.getDamager();
        if (!(eDamager instanceof Player)) {
            return;
        }

        Player damager = (Player) eDamager;
        PlayerData playerData = plugin.getPlayerDataHandler().getData(damager.getUniqueId());
        if (playerData.isInTutorial() || playerData.isManager()) {
            return;
        }

        Material itemInHandType = damager.getInventory().getItemInMainHand().getType();
        if (itemInHandType == Material.WOODEN_PICKAXE || itemInHandType == Material.STONE_PICKAXE) {
            playerData.incrementEquippedAttackSteps();
        }

        playerData.incrementAttackSteps();
    }

}

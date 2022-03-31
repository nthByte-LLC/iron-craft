package com.nthbyte.ironcraft.listener;

import com.nthbyte.ironcraft.IronCraftPlugin;
import com.nthbyte.ironcraft.event.AssignManagerEvent;
import com.nthbyte.ironcraft.manager.ManagementType;
import com.nthbyte.ironcraft.prompt.AutonomySurveyPrompt;
import com.nthbyte.ironcraft.prompt.ManagerSurvey;
import net.citizensnpcs.npc.CitizensNPC;
import net.dohaw.corelib.StringUtils;
import com.nthbyte.ironcraft.PlayerData;
import com.nthbyte.ironcraft.Reason;
import com.nthbyte.ironcraft.event.EndGameEvent;
import com.nthbyte.ironcraft.handler.PlayerDataHandler;
import com.nthbyte.ironcraft.prompt.IDPrompt;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.xml.soap.DetailEntry;
import java.util.*;

public class PlayerWatcher implements Listener {

    private final List<Material> BREAKABLE_TUTORIAL_BLOCKS = Arrays.asList(Material.STONE, Material.OAK_LOG,
            Material.OAK_LEAVES, Material.IRON_ORE, Material.DIAMOND_ORE, Material.GRASS_BLOCK, Material.DIRT, Material.CRAFTING_TABLE, Material.FURNACE
    );

    private final IronCraftPlugin plugin;

    /**
     * Furnaces that are currently burning fuel.
     */
    private HashSet<Location> burningFurances = new HashSet<>();

    public PlayerWatcher(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    // Doesn't let them move if they haven't answered the autonomy survey
    @EventHandler
    public static void onPlayerMoveDuringSurvey(PlayerMoveEvent e) {
        PersistentDataContainer pdc = e.getPlayer().getPersistentDataContainer();
        if (pdc.has(IronCraftPlugin.IN_SURVEY_PDC_KEY, PersistentDataType.STRING) && hasMoved(e.getTo(), e.getFrom(), true)) {
            e.setCancelled(true);
        }
    }

    /**
     * Determines if a player uses the wrong tool (a wooden pickaxe) to dig iron ore.
     *
     * @param e Event of the player breaking a block
     */
    @EventHandler
    public void onPlayerMineIronOre(BlockBreakEvent e) {

        Player p = e.getPlayer();
        Block block = e.getBlock();
        ItemStack itemInHand = p.getEquipment().getItemInMainHand();
        PlayerData playerData = plugin.getPlayerDataHandler().getData(p.getUniqueId());

        if(playerData == null || playerData.isInTutorial() || playerData.isManager()) return;

        if (block.getType() == Material.IRON_ORE && itemInHand.getType() == Material.WOODEN_PICKAXE) {
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
        ItemStack itemInHand = p.getEquipment().getItemInMainHand();
        Material tool = itemInHand.getType();
        PlayerData playerData = plugin.getPlayerDataHandler().getData(p.getUniqueId());

        if(playerData == null || playerData.isInTutorial() || playerData.isManager()) return;

        if (block == Material.OAK_LOG || block == Material.ACACIA_LOG || block == Material.BIRCH_LOG || block == Material.DARK_OAK_LOG || block == Material.JUNGLE_LOG || block == Material.SPRUCE_LOG && tool == Material.STONE_PICKAXE) {
            playerData.incrementMisuseActionSteps();
        }

    }

    @EventHandler
    public void onManagerSwim(EntityToggleSwimEvent e){

        Entity entity = e.getEntity();
        if(!(entity instanceof Player)){
            return;
        }

        Player player = (Player) entity;
        PlayerData pd = plugin.getPlayerDataHandler().getData(player);
        if(pd != null && pd.isManager()){
            e.setCancelled(true);
        }

    }

    /**
     * Doesn't let managers interact with anything. Also teleports a player to the next focused player if they left-click.
     */
    @EventHandler
    public void onManagerInteract(PlayerInteractEvent e){

        Player player = e.getPlayer();
        if(IronCraftPlugin.isAnsweringSurvey(player)){
            return;
        }

        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        if(!playerDataHandler.hasDataLoaded(player)) return;

        PlayerData pd = plugin.getPlayerDataHandler().getData(player);
        if(!pd.isManager()) return;

        e.setCancelled(true);

        Action action = e.getAction();
        if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){

            List<UUID> overseeingUsers = pd.getUsersOverseeing();
            if(overseeingUsers.isEmpty()) return;

            UUID currentFocusedPlayer = pd.getFocusedPlayerUUID();
            UUID nextFocusedPlayer;
            if(currentFocusedPlayer == null){
                nextFocusedPlayer = overseeingUsers.get(0);
            }else{

                int currentIndexFocusedPlayer = overseeingUsers.indexOf(currentFocusedPlayer);
                int nextIndexFocusedPlayer = currentIndexFocusedPlayer + 1;
                if( (nextIndexFocusedPlayer + 1) > overseeingUsers.size()){
                    nextFocusedPlayer = null;
                    player.sendMessage(StringUtils.colorString("&9!!INFO!! &fYou aren't watching any workers. You are now free to roam. Left-click again to spectate a player."));
                }else{
                    nextFocusedPlayer = overseeingUsers.get(nextIndexFocusedPlayer);
                }

            }

            if(nextFocusedPlayer != null){
                player.sendMessage(StringUtils.colorString("&9!!INFO!! &fYou are now watching " + Bukkit.getPlayer(nextFocusedPlayer).getName()));
            }

            pd.setFocusedPlayerUUID(nextFocusedPlayer);

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        player.setAllowFlight(true);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if(!plugin.getPlayerDataHandler().hasDataLoaded(player)){
                player.sendTitle("Input your ID", "Press T on your keyboard", 0, 23, 0);
            }
        }, 0,  20);

        ConversationFactory conversationFactory = new ConversationFactory(plugin);
        Conversation conversation = conversationFactory.withFirstPrompt(new IDPrompt(plugin)).withLocalEcho(false).buildConversation(player);
        conversation.begin();

    }

    /*
        Prevents dialogue from occurring between players. The only players that will receive other player's messages are admins.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        Set<Player> recipients = e.getRecipients();
        if(recipients != null){
            for(Player p : recipients){
                if(!plugin.getPlayerDataHandler().getData(p).isAdmin()){
                    recipients.remove(p);
                }
            }
        }
    }

    /*
        Ensures that the manager npc is removed when the player is kicked (The PlayerQuitEvent doesn't fire when they are kicked).
     */
    @EventHandler
    public void onPlayerKick(PlayerKickEvent e){

        Player player = e.getPlayer();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        if(playerDataHandler.hasDataLoaded(player)){
            PlayerData playerData = playerDataHandler.getData(player);
            CitizensNPC managerNPC = playerData.getManagerNPC();
            if(managerNPC != null){
                managerNPC.despawn();
                managerNPC.destroy();
            }
        }

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {

        Player player = e.getPlayer();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        PlayerData data = playerDataHandler.getData(player);
        UUID playerUUID = player.getUniqueId();
        if (playerDataHandler.hasDataLoaded(player)) {

            playerDataHandler.saveData(playerUUID);

            CitizensNPC managerNPC = data.getManagerNPC();
            // this works
            if(managerNPC != null){
                managerNPC.despawn();
                managerNPC.destroy();
            }

            UUID managerUUID = data.getManager();
            // If the player had a manager, it removes the player from the list of workers the manager is managing.
            if(managerUUID != null){
                if(playerDataHandler.hasDataLoaded(managerUUID)){
                    PlayerData managerData = playerDataHandler.getData(managerUUID);
                    managerData.getUsersOverseeing().remove(player.getUniqueId());
                    managerData.setFocusedPlayerUUID(null);
                }
            }

        }

        player.getPersistentDataContainer().remove(IronCraftPlugin.IN_SURVEY_PDC_KEY);

    }

    /*
        Prevents players from breaking blocks if their data isn't loaded or if they're in a tutorial & isn't a permitted block to break.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        if (!playerDataHandler.hasDataLoaded(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());

        if(playerData.isManager()){
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Managers can't break blocks!");
            return;
        }

        if (playerData.isInTutorial() && !BREAKABLE_TUTORIAL_BLOCKS.contains(e.getBlock().getType())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't break that block. Focus on the objectives!");
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

    /*
        Prevents anyone from taking damage.
     */
    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent e) {

        World applicableWorld = plugin.getBaseConfig().getWorld();
        if(applicableWorld == null) return;

        Entity damagedEntity = e.getEntity();
        if (damagedEntity instanceof Player) {
            e.setCancelled(true);
        }

    }

    /*
        Listens for the end of the game. Sets the necessary data fields within the player's data & starts the autonomy survey.
     */
    @EventHandler
    public void onGameEnd(EndGameEvent e) {

        PlayerData playerData = e.getPlayerData();
        Player player = playerData.getPlayer();

        // If the player leaves the game and joins back when they previously had 0 minutes left,
        // the plugin would increment the rounds played right when they join again. This fixes it.
        if(!playerData.hasRecentlyJoined()){
            playerData.incrementRoundsPlayed();
        }

        Reason reason = e.getReason();
        if(reason == Reason.OUT_OF_TIME){
            player.sendTitle("Game Over", "You ran out of time!", 10, 50, 9);
        }

        player.sendMessage(StringUtils.colorString("&aCongratulations! &fYou have successfully completed the game!"));
        player.sendMessage("You will now take a survey. You won't be able to move for the duration of this survey. Don't worry, it'll be quick!");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.getPersistentDataContainer().set(IronCraftPlugin.IN_SURVEY_PDC_KEY, PersistentDataType.STRING, "marker");
            Conversation conv = new ConversationFactory(plugin).withFirstPrompt(new AutonomySurveyPrompt(0, plugin)).withLocalEcho(false).buildConversation(player);
            conv.begin();
        }, 20L * 3);

        // If they are managed by a human, start the manager survey with them.
        if(playerData.getManagementType() == ManagementType.HUMAN){
            UUID managerUUID = playerData.getManager();
            PlayerData managerData = plugin.getPlayerDataHandler().getData(managerUUID);
            managerData.getPlayer().getPersistentDataContainer().set(IronCraftPlugin.IN_SURVEY_PDC_KEY, PersistentDataType.STRING, "marker");
            Conversation conv = new ConversationFactory(plugin).withFirstPrompt(new ManagerSurvey(playerData)).withLocalEcho(true).buildConversation(managerData.getPlayer());
            conv.begin();
        }

        // Removes the current player as their focused player if they are done with the game completely.
        if(reason == Reason.GAME_COMPLETE || (playerData.getRoundsPlayed() == 3 && reason == Reason.OUT_OF_TIME)){
            UUID managerUUID = playerData.getManager();
            if(playerData.getManagementType() == ManagementType.HUMAN){
                PlayerData managerData = plugin.getPlayerDataHandler().getData(managerUUID);
                managerData.setFocusedPlayerUUID(null);
                managerData.getUsersOverseeing().remove(player.getUniqueId());
            }
        }

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
                || blockTypePlaced == Material.DIRT || blockTypePlaced == Material.STONE || blockTypePlaced == Material.WALL_TORCH){
            playerData.incrementPlacedItems(blockTypePlaced);
        }

    }

    @EventHandler
    public void onFuelBurn(FurnaceBurnEvent e){
        ItemStack fuel = e.getFuel();
        if(fuel.getType() != Material.COAL) return;
        burningFurances.add(e.getBlock().getLocation());
    }

    /**
     * Sets a boolean to true if the player smelts coal.
     *
     * @param e The event
     */
    @EventHandler
    public void onPlayerSmelt(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();

        InventoryType.SlotType s = e.getSlotType();
        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null || e.getInventory().getType() != InventoryType.FURNACE || s != InventoryType.SlotType.RESULT) return;

        Location loc = e.getInventory().getLocation();
        if(burningFurances.contains(loc)){
            burningFurances.remove(loc);
            PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            playerData.setHasSmeltedCoal(true);
        }

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

    /*
        Doesn't allow managers to pickup items.
     */
    @EventHandler
    public void onManagerPickupItem(EntityPickupItemEvent e){
        Entity entity = e.getEntity();
        if(!(entity instanceof Player)) return;
        Player player = (Player) entity;
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        if(playerDataHandler.hasDataLoaded(player)){
            PlayerData playerData = playerDataHandler.getData(player);
            if(playerData.isManager()){
                e.setCancelled(true);
            }
        }
    }

    /**
     * Stops players from moving entirely if they are a manager and are overseeing users.
     */
    @EventHandler
    public void onManagerMove(PlayerMoveEvent e){
        PlayerData playerData = plugin.getPlayerDataHandler().getData(e.getPlayer());
        if(playerData == null) return;
        if(playerData.isManager() && playerData.getFocusedPlayerUUID() != null && hasMoved(e.getTo(), e.getFrom(), true)) e.setCancelled(true);
    }

    @EventHandler
    public void onAssignManager(AssignManagerEvent e){
        plugin.updateScoreboard(e.getManager().getPlayer());
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

}

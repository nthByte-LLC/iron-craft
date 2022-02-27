package net.dohaw.ironcraft.listener;

import net.citizensnpcs.npc.CitizensNPC;
import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.Objective;
import net.dohaw.ironcraft.PlayerData;
import net.dohaw.ironcraft.Reason;
import net.dohaw.ironcraft.event.AssignManagerEvent;
import net.dohaw.ironcraft.event.EndGameEvent;
import net.dohaw.ironcraft.handler.PlayerDataHandler;
import net.dohaw.ironcraft.manager.ManagementType;
import net.dohaw.ironcraft.prompt.AutonomySurveyPrompt;
import net.dohaw.ironcraft.prompt.IDPrompt;
import net.dohaw.ironcraft.prompt.ManagerSurvey;
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
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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

        if(playerData.isInTutorial() || playerData.isManager()) return;

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

        if(playerData.isInTutorial() || playerData.isManager()) return;

        if (block == Material.OAK_LOG || block == Material.ACACIA_LOG || block == Material.BIRCH_LOG || block == Material.DARK_OAK_LOG || block == Material.JUNGLE_LOG || block == Material.SPRUCE_LOG && tool == Material.STONE_PICKAXE) {
            playerData.incrementMisuseActionSteps();
        }

    }

    /**
     * Doesn't let managers interact with anything. Also teleports a player to the next focused player if they left-click.
     */
    @EventHandler
    public void onManagerInteract(PlayerInteractEvent e){

        PlayerData pd = plugin.getPlayerDataHandler().getData(e.getPlayer());
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
                int nextIndexFocusedPlayer = currentIndexFocusedPlayer == overseeingUsers.size() - 1 ? 0 : currentIndexFocusedPlayer + 1;
                nextFocusedPlayer = overseeingUsers.get(nextIndexFocusedPlayer);
            }

            pd.setFocusedPlayerUUID(nextFocusedPlayer);

        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        player.sendTitle("Input your ID", "Press T on your keyboard", 5, 100, 20);

        ConversationFactory conversationFactory = new ConversationFactory(plugin);
        Conversation conversation = conversationFactory.withFirstPrompt(new IDPrompt(plugin)).withLocalEcho(false).buildConversation(player);
        conversation.begin();

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

        }

        player.getPersistentDataContainer().remove(IronCraftPlugin.IN_SURVEY_PDC_KEY);

    }

    /**
     * Prevents players from breaking blocks if their data isn't loaded or if they're in a tutorial & isn't a permitted block to break.
     * @param e
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
        if (playerData.isInTutorial() && !playerData.isManager() && !BREAKABLE_TUTORIAL_BLOCKS.contains(e.getBlock().getType())) {
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

    /*
     * Listens for the end of the game. Sets the necessary data fields within the player's data & starts the autonomy survey.
     */
    @EventHandler
    public void onGameEnd(EndGameEvent e) {

        PlayerData playerData = e.getPlayerData();
        Player player = playerData.getPlayer();

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
            Conversation conv = new ConversationFactory(plugin).withFirstPrompt(new ManagerSurvey(playerData)).withLocalEcho(false).buildConversation(managerData.getPlayer());
            conv.begin();
        }

        playerData.incrementRoundsPlayed();
        int roundsPlayed = playerData.getRoundsPlayed();
        System.out.println("Rounds played: " + roundsPlayed);
        if(playerData.getRoundsPlayed() < 3){

            player.sendMessage(StringUtils.colorString("You have played " + roundsPlayed + " rounds. You have " + (3 - roundsPlayed) + " more round(s) to go!"));
            Location randomSpawnPoint = plugin.getRandomJourneySpawnPoint();
            if (randomSpawnPoint == null) {
                plugin.getLogger().severe("There has been an error trying to teleport a player to a random spawn point");
                player.sendRawMessage("You could not be teleported to a random spawn point at this moment. Please contact an administrator...");
                return;
            }

            // Lets them play the game again.
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                playerData.setCurrentTutorialObjective(Objective.MOVE);
                player.teleport(randomSpawnPoint);
                playerData.setMinutesInGame(0);
                playerData.initWorker(plugin);
            }, 20 * 3);

        }else{
            player.sendMessage(StringUtils.colorString("Congratulations. You are finished."));
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
        if(playerData.isManager() && !playerData.getUsersOverseeing().isEmpty() && hasMoved(e.getTo(), e.getFrom(), true)) e.setCancelled(true);
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

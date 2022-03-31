package com.nthbyte.ironcraft;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.citizensnpcs.npc.CitizensNPC;
import net.dohaw.corelib.CoreLib;
import net.dohaw.corelib.JPUtils;
import net.dohaw.corelib.StringUtils;
import com.nthbyte.ironcraft.config.BaseConfig;
import com.nthbyte.ironcraft.data_collection.DataCollector;
import com.nthbyte.ironcraft.event.CompleteObjectiveEvent;
import com.nthbyte.ironcraft.handler.PlayerDataHandler;
import com.nthbyte.ironcraft.listener.ItemWatcher;
import com.nthbyte.ironcraft.listener.ObjectiveWatcher;
import com.nthbyte.ironcraft.listener.PlayerWatcher;
import com.nthbyte.ironcraft.prompt.IDPrompt;
import org.bukkit.*;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

/**
 * Plugin for Max Planck Society.
 * Teaches people to obtain an iron pickaxe.
 * @author Caleb Owens, Ayush Chivate
 */
public final class IronCraftPlugin extends JavaPlugin {

    public static final NamespacedKey IN_SURVEY_PDC_KEY = NamespacedKey.minecraft("is-answering-survey");

    private static IronCraftPlugin instance;

    /**
     * The spawn points at which the player can spawn after they finish the tutorial and go out on their own.
     */
    private List<Location> journeySpawnPoints;

    /**
     * The available locations at which a chamber is located.
     */
    private List<Location> availableChamberLocations = new ArrayList<>();

    private PlayerDataHandler playerDataHandler;
    private BaseConfig baseConfig;

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {

        CoreLib.setInstance(this);

        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        JPUtils.validateFiles("config.yml");
        JPUtils.validateFilesOrFolders(
            new HashMap<String, Object>() {{
                put("player_data", getDataFolder());
                put("end_game_data", getDataFolder());
            }}, true
        );
        baseConfig = new BaseConfig();
        loadConfigValues();

        playerDataHandler = new PlayerDataHandler(this);

        JPUtils.registerCommand("ironcraft", new IronCraftCommand(this));
        JPUtils.registerEvents(new PlayerWatcher(this));
        JPUtils.registerEvents(new ObjectiveWatcher(this));
        JPUtils.registerEvents(new ItemWatcher(this));

        // Only useful if there are players on the server, and /plugman reload IronCraft gets ran
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isConversing()) {
                player.kickPlayer("Please re-join the server!");
                continue;
            }
            player.sendMessage("Please re-verify your ID!");
            ConversationFactory conversationFactory = new ConversationFactory(this);
            Conversation conversation = conversationFactory.withFirstPrompt(new IDPrompt(this)).withLocalEcho(false).buildConversation(player);
            conversation.begin();
        }

        // Reminder every 10 seconds
        new Reminder(this).runTaskTimer(this, 0L, 20 * 10);

        // collect data every tick
        new DataCollector(this).runTaskTimer(this, 0L, 1);

        startSurveyWarner();

        formPacketListeners();

    }

    @Override
    public void onDisable() {

        for(PlayerData pd : playerDataHandler.getPlayerDataList()){
            CitizensNPC npc = pd.getManagerNPC();
            if(npc != null){
                npc.despawn();
                npc.destroy();
            }
        }

        baseConfig.saveChamberLocations(availableChamberLocations);
        baseConfig.saveSpawnLocations(journeySpawnPoints);
        playerDataHandler.saveAllData();

    }

    private void formPacketListeners() {

        // Listener for when the player clicks on the recipe book in their inventory.
        protocolManager.addPacketListener(
            new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.RECIPE_SETTINGS) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (event.getPacketType() == PacketType.Play.Client.RECIPE_SETTINGS) {

                        Player player = event.getPlayer();
                        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());

                        if (playerData != null) {
                            if (playerData.getCurrentTutorialObjective() == Objective.OPEN_RECIPE_MENU) {

                                boolean isRecipeMenuOpen = event.getPacket().getBooleans().read(0);
                                // Maggie will have multiple people on the same account. If 1 player leaves the recipe menu open, then it'll stay open for the next person.
                                // If they try to complete the objective and clicking the recipe menu button while it's open, it'll close it and that'll leave them confused.
                                if (isRecipeMenuOpen) {
                                    playerData.setCurrentTutorialObjective(getNextObjective(Objective.OPEN_RECIPE_MENU));
                                    Bukkit.getScheduler().runTask(instance, () -> {
                                        Bukkit.getServer().getPluginManager().callEvent(new CompleteObjectiveEvent(playerData));
                                    });
                                } else {
                                    // Need this on sync delay to fix issue #11
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, player::closeInventory);
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        player.sendMessage(ChatColor.RED + "Oops! Looks like you just closed it. Try opening the recipe menu again...");
                                    }, 5);
                                }

                            }
                        }

                    }
                }
            });

    }

    private void loadConfigValues() {
        availableChamberLocations = baseConfig.getChamberLocations();
        journeySpawnPoints = baseConfig.getSpawnLocations();
    }

    public Location getRandomChamber() {
        if (availableChamberLocations.isEmpty()) {
            return null;
        }
        return availableChamberLocations.remove(new Random().nextInt(availableChamberLocations.size()));
    }

    public Location getRandomJourneySpawnPoint() {
        if(journeySpawnPoints.isEmpty()){
            return null;
        }
        return journeySpawnPoints.remove(new Random().nextInt(journeySpawnPoints.size()));
    }

    public void updateScoreboard(Player player) {

        if(player == null || !player.isValid()){
            return;
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard tutorialScoreBoard = manager.getNewScoreboard();
        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());

        String title = playerData.isManager() ? "&ePlayers Managing" : "&eObjectives";
        org.bukkit.scoreboard.Objective obj = tutorialScoreBoard.registerNewObjective("DCScoreboard", "dummy", StringUtils.colorString(title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        if(playerData.isAdmin() || playerData.isFinished()){
            player.setScoreboard(manager.getNewScoreboard());
            return;
        }

        if(!playerData.isManager()){

            boolean isInTutorial = playerData.isInTutorial();
            // If they aren't in the tutorial, then they don't need the objectives on the side of their screen.
            if(!isInTutorial){

                int minutesRemaining = 7 - playerData.getMinutesInGame();
                Scoreboard gameScoreboard = manager.getNewScoreboard();
                org.bukkit.scoreboard.Objective obj2 = gameScoreboard.registerNewObjective("DCScoreboard", "dummy", StringUtils.colorString("&bTime Remaining"));
                obj2.setDisplaySlot(DisplaySlot.SIDEBAR);

                Score score = obj2.getScore(StringUtils.colorString("&eRounds Played: &f" + playerData.getRoundsPlayed()));
                score.setScore(0);

                if(minutesRemaining != 0){
                    score = obj2.getScore(StringUtils.colorString("&f" + minutesRemaining + " &eminutes remaining"));
                }else{
                    score = obj2.getScore(StringUtils.colorString("&eLess than &f1 minute&e remaining"));
                }
                score.setScore(1);

                score = obj2.getScore(StringUtils.colorString("&eGoal: Make an iron pickaxe"));
                score.setScore(3);

                score = obj2.getScore(StringUtils.colorString("&e&oGood luck"));
                score.setScore(2);

                player.setScoreboard(gameScoreboard);
                return;
            }

            int currentObjectiveOrdinal = playerData.getCurrentTutorialObjective().ordinal();
            int counter = 0;
            Score score;
            for (Objective objective : Objective.values()) {

                if (objective.ordinal() > currentObjectiveOrdinal) {
                    score = obj.getScore(StringUtils.colorString("&8&o" + objective.toProperName()));
                } else if (objective.ordinal() == currentObjectiveOrdinal) {
                    score = obj.getScore(StringUtils.colorString("&6&l" + objective.toProperName()));
                } else {
                    score = obj.getScore(StringUtils.colorString("&2&m" + objective.toProperName()));
                }

                score.setScore(++counter);

            }

        }else{

            Score score;
            List<UUID> usersOverseeing = playerData.getUsersOverseeing();
            for (int i = 0; i < usersOverseeing.size(); i++) {
                Player p = Bukkit.getPlayer(usersOverseeing.get(i));
                if(p == null || !p.isValid()) {
                    usersOverseeing.remove(i);
                    continue;
                }
                PlayerData pd = playerDataHandler.getData(p);
                String userName = Bukkit.getPlayer(usersOverseeing.get(i)).getName();
                int minutesLeft = 7 - pd.getMinutesInGame();
                score = obj.getScore(StringUtils.colorString("&7&l- " + userName + "&f&l [Minutes Left: " + minutesLeft + "]"));
                score.setScore(2 - (i+1));
            }

        }

        player.setScoreboard(tutorialScoreBoard);

    }

    /**
     * Gives the player the essentials items they need to player the game.
     * @param player The player
     */
    public void giveEssentialItems(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.addItem(new ItemStack(Material.TORCH, 64));
    }

    public Objective getNextObjective(Objective currentObjective) {
        int ordinal = currentObjective.ordinal();
        return Objective.values()[ordinal + 1];
    }

    /**
     * Starts the runnable that sends a title to players telling them that they are being surveyed.
     */
    private void startSurveyWarner(){
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for(Player player : Bukkit.getOnlinePlayers()){
                if(player.getPersistentDataContainer().has(IronCraftPlugin.IN_SURVEY_PDC_KEY, PersistentDataType.STRING) && player.isConversing()){
                    player.sendTitle("Look in chat", "Press T", 0, 23, 0);
                }
            }
        }, 0, 20);
    }

    public List<Location> getJourneySpawnPoints() {
        return journeySpawnPoints;
    }

    public List<Location> getAvailableChamberLocations() {
        return availableChamberLocations;
    }

    public PlayerDataHandler getPlayerDataHandler() {
        return playerDataHandler;
    }

    public BaseConfig getBaseConfig() {
        return baseConfig;
    }

    public static IronCraftPlugin getInstance() {
        return instance;
    }

    public static boolean isAnsweringSurvey(Player player){
        return player.getPersistentDataContainer().has(IN_SURVEY_PDC_KEY, PersistentDataType.STRING);
    }

}

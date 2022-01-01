package net.dohaw.ironcraft;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.dohaw.corelib.CoreLib;
import net.dohaw.corelib.JPUtils;
import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.config.BaseConfig;
import net.dohaw.ironcraft.data_collection.DataCollector;
import net.dohaw.ironcraft.handler.PlayerDataHandler;
import net.dohaw.ironcraft.listener.ItemWatcher;
import net.dohaw.ironcraft.listener.ObjectiveWatcher;
import net.dohaw.ironcraft.listener.PlayerWatcher;
import net.dohaw.ironcraft.playerdata.PlayerData;
import net.dohaw.ironcraft.prompt.IDPrompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Plugin for Max Planck Society.
 * Teaches people to obtain an iron pickaxe.
 */
public final class IronCraftPlugin extends JavaPlugin {

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
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        JPUtils.validateFiles("config.yml");
        JPUtils.validateFilesOrFolders(
                new HashMap<String, Object>() {{
                    put("player_data", getDataFolder());
                }}, true
        );
        this.baseConfig = new BaseConfig();
        loadConfigValues();

        this.playerDataHandler = new PlayerDataHandler(this);

        JPUtils.registerCommand("ironcraft", new IronCraftCommand(this));
        JPUtils.registerEvents(new PlayerWatcher(this));
        JPUtils.registerEvents(new ObjectiveWatcher(this));
        JPUtils.registerEvents(new ItemWatcher(this));

        // Only useful if there are players on the server, and /plugman reload DiamondCraft gets ran
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isConversing()) {
                player.kickPlayer("Please rejoin the server!");
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

        formPacketListeners();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
           for(Player player : Bukkit.getOnlinePlayers()){
               System.out.println("DIRECTION: "  + player.getLocation().getDirection());
           }
        }, 0L, 20L);

    }

    @Override
    public void onDisable() {
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
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Oops! Looks like you just closed it. Try opening the recipe menu again...");
                                    }

                                }

                            }

                        }

                    }
                });

    }

    private void loadConfigValues() {
        this.availableChamberLocations = baseConfig.getChamberLocations();
        this.journeySpawnPoints = baseConfig.getSpawnLocations();
    }

    public Location getRandomChamber() {
        if (availableChamberLocations.isEmpty()) {
            return null;
        }
        return availableChamberLocations.remove(new Random().nextInt(availableChamberLocations.size()));
    }

    public Location getRandomJourneySpawnPoint() {
        System.out.println("SPAWN LOCATIONS: " + journeySpawnPoints);
        return journeySpawnPoints.get(new Random().nextInt(journeySpawnPoints.size()));
    }

    public void updateScoreboard(Player player) {

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective obj = board.registerNewObjective("DCScoreboard", "dummy", StringUtils.colorString("&bObjectives"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
        int currentObjectiveOrdinal = playerData.getCurrentTutorialObjective().ordinal();
        int counter = 4;
        for (Objective objective : Objective.values()) {

            Score objScore;
            if (objective.ordinal() > currentObjectiveOrdinal) {
                objScore = obj.getScore(StringUtils.colorString("&8&o" + objective.toProperName()));
            } else if (objective.ordinal() == currentObjectiveOrdinal) {
                objScore = obj.getScore(StringUtils.colorString("&6&l" + objective.toProperName()));
            } else {
                objScore = obj.getScore(StringUtils.colorString("&2&m" + objective.toProperName()));
            }

            objScore.setScore(counter);

            counter -= -1;

        }

        player.setScoreboard(board);

    }

    public void giveEssentialItems(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.addItem(new ItemStack(Material.TORCH, 64));
    }

    public Objective getNextObjective(Objective currentObjective) {
        int ordinal = currentObjective.ordinal();
        return Objective.values()[ordinal + 1];
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

}

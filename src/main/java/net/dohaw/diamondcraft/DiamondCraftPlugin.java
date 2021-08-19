package net.dohaw.diamondcraft;

import net.dohaw.corelib.CoreLib;
import net.dohaw.corelib.JPUtils;
import net.dohaw.corelib.StringUtils;
import net.dohaw.corelib.helpers.ItemStackHelper;
import net.dohaw.diamondcraft.config.BaseConfig;
import net.dohaw.diamondcraft.data_collection.DataCollector;
import net.dohaw.diamondcraft.handler.PlayerDataHandler;
import net.dohaw.diamondcraft.listener.ObjectiveWatcher;
import net.dohaw.diamondcraft.listener.PlayerWatcher;
import net.dohaw.diamondcraft.playerdata.PlayerData;
import net.dohaw.diamondcraft.prompt.IDPrompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * Plugin for Max Planck Society.
 * Teaches people to obtain an iron pickaxe.
 */
public final class DiamondCraftPlugin extends JavaPlugin {

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

    @Override
    public void onEnable() {

        CoreLib.setInstance(this);

        JPUtils.validateFiles("config.yml");
        JPUtils.validateFilesOrFolders(
                new HashMap<String, Object>() {{
                    put("player_data", getDataFolder());
                }}, true
        );
        this.baseConfig = new BaseConfig();
        loadConfigValues();

        this.playerDataHandler = new PlayerDataHandler(this);

        JPUtils.registerCommand("diamondcraft", new DiamondCraftCommand(this));
        JPUtils.registerEvents(new PlayerWatcher(this));
        JPUtils.registerEvents(new ObjectiveWatcher(this));

        // Only useful if there are players on the server, and /plugman reload DiamondCraft gets ran
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("Please re-verify your ID!");
            ConversationFactory conversationFactory = new ConversationFactory(this);
            Conversation conversation = conversationFactory.withFirstPrompt(new IDPrompt(this)).withLocalEcho(false).buildConversation(player);
            conversation.begin();
        }

        // Reminder every 10 seconds
        new Reminder(this).runTaskTimer(this, 0L, 20 * 10);

        // Store inventory data every tick
        new DataCollector(this).runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        baseConfig.saveChamberLocations(availableChamberLocations);
        baseConfig.saveSpawnLocations(journeySpawnPoints);
        playerDataHandler.saveAllData();
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
        //System.out.println("JOURNEY SPANW: " + journeySpawnPoints.toString());
        return journeySpawnPoints.get(new Random().nextInt(journeySpawnPoints.size()));
    }

    public void updateScoreboard(Player player) {

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("DCScoreboard", "dummy", StringUtils.colorString("&bObjectives"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
        int currentObjectiveOrdinal = playerData.getCurrentTutorialObjective().ordinal();
        int counter = 4;
        for (TutorialObjective objective : TutorialObjective.values()) {

            Score objScore;
            if (objective.ordinal() > currentObjectiveOrdinal) {
                objScore = obj.getScore(StringUtils.colorString("&8&o" + objective.toProperName()));
            } else if (objective.ordinal() == currentObjectiveOrdinal) {
                objScore = obj.getScore(StringUtils.colorString("&6&l" + objective.toProperName()));
            } else {
                objScore = obj.getScore(StringUtils.colorString("&2&m" + objective.toProperName()));
            }

            objScore.setScore(counter);

            counter++;

        }

        player.setScoreboard(board);

    }

    public void giveEssentialItems(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.addItem(createRecipeMenuPaper());
        inv.addItem(new ItemStack(Material.TORCH, 64));
    }

    private ItemStack createRecipeMenuPaper() {

        ItemStack menuPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = menuPaper.getItemMeta();
        meta.setDisplayName(StringUtils.colorString("&b&lRecipe Menu"));

        List<String> lore = Arrays.asList(ChatColor.RED + "Right-click with me in hand to see the recipe menu!");
        meta.setLore(lore);
        menuPaper.setItemMeta(meta);

        ItemStackHelper.addGlowToItem(menuPaper);
        return menuPaper;

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

}

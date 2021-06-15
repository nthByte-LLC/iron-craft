package net.dohaw.diamondcraft;

import net.dohaw.corelib.CoreLib;
import net.dohaw.corelib.JPUtils;
import net.dohaw.corelib.StringUtils;
import net.dohaw.diamondcraft.config.BaseConfig;
import net.dohaw.diamondcraft.handler.PlayerDataHandler;
import net.dohaw.diamondcraft.listener.ObjectiveWatcher;
import net.dohaw.diamondcraft.listener.PlayerWatcher;
import net.dohaw.diamondcraft.playerdata.PlayerData;
import net.dohaw.diamondcraft.prompt.IDPrompt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class DiamondCraftPlugin extends JavaPlugin {

    private List<Location> journeySpawnPoints;

    private List<Location> availableChamberLocations = new ArrayList<>();

    private PlayerDataHandler playerDataHandler;
    private BaseConfig baseConfig;

    @Override
    public void onEnable() {

        CoreLib.setInstance(this);

        JPUtils.validateFiles("config.yml");
        JPUtils.validateFilesOrFolders(
            new HashMap<String, Object>(){{
                put("player_data", getDataFolder());
            }}, true
        );
        this.baseConfig = new BaseConfig();
        loadConfigValues();

        this.playerDataHandler = new PlayerDataHandler(this);

        JPUtils.registerCommand("diamondcraft", new DiamondCraftCommand(this));
        JPUtils.registerEvents(new PlayerWatcher(this));
        JPUtils.registerEvents(new ObjectiveWatcher(this));

        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendMessage("Please re-verify your ID!");
            ConversationFactory conversationFactory = new ConversationFactory(this);
            Conversation conversation = conversationFactory.withFirstPrompt(new IDPrompt(this)).withLocalEcho(false).buildConversation(player);
            conversation.begin();
        }

        // Reminder every 10 seconds
        new Reminder(this).runTaskTimer(this, 0L,200L);

    }

    @Override
    public void onDisable() {
        baseConfig.saveChamberLocations(availableChamberLocations);
        baseConfig.saveSpawnLocations(journeySpawnPoints);
        playerDataHandler.saveAllData();
    }

    private void loadConfigValues(){
        this.availableChamberLocations = baseConfig.getChamberLocations();
        this.journeySpawnPoints = baseConfig.getSpawnLocations();
    }

    public List<Location> getJourneySpawnPoints() {
        return journeySpawnPoints;
    }

    public List<Location> getAvailableChamberLocations() {
        return availableChamberLocations;
    }

    public Location getRandomChamber(){
        if(availableChamberLocations.isEmpty()){
            return null;
        }
        return availableChamberLocations.remove(new Random().nextInt(availableChamberLocations.size()));
    }

    public Location getRandomJourneySpawnPoint(){
        return journeySpawnPoints.get(new Random().nextInt(journeySpawnPoints.size()));
    }

    public PlayerDataHandler getPlayerDataHandler() {
        return playerDataHandler;
    }

    public void updateScoreboard(Player player){

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("DCScoreboard", "dummy", StringUtils.colorString("&bObjectives"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
        int currentObjectiveOrdinal = playerData.getCurrentTutorialObjective().ordinal();
        int counter = 4;
        for(TutorialObjective objective : TutorialObjective.values()){

            Score objScore;
            if(objective.ordinal() > currentObjectiveOrdinal){
                objScore = obj.getScore(StringUtils.colorString("&8&o" + objective.toProperName()));
            }else if(objective.ordinal() == currentObjectiveOrdinal){
                objScore = obj.getScore(StringUtils.colorString("&6&l" + objective.toProperName()));
            }else {
                objScore = obj.getScore(StringUtils.colorString("&2&m" + objective.toProperName()));
            }

            objScore.setScore(counter);

            counter++;

        }

        //Score score2 = obj.getScore(StringUtils.colorString("&b=-=-=-=-=-=-=-=-=-=-="));
        //score2.setScore(counter + 1);

        player.setScoreboard(board);

    }

}

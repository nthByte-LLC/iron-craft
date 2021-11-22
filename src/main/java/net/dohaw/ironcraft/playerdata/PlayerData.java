package net.dohaw.ironcraft.playerdata;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.Objective;
import net.dohaw.ironcraft.SurveySession;
import net.dohaw.ironcraft.config.PlayerDataConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private SurveySession surveySession;
    private boolean isManager;
    private UUID uuid;
    private String providedID;
    private PlayerDataConfig playerDataConfig;
    private boolean isInTutorial;
    private Location chamberLocation;
    private Objective currentTutorialObjective;

    /**
     * Stores a list of inventoryData. New data is stored every "step"
     */
     private List<Map<String, Integer>> inventoryDataList = new ArrayList<>();

    /**
     * Stores the gain order of items.
     */
    private List<Integer> gainOrder = new ArrayList<>();

    private List<Boolean> isUselessToolCrafted = new ArrayList<Boolean>(){{
        add(false);
        add(false);
        add(false);
    }};

    public PlayerData(UUID uuid, String providedID) {
        this.providedID = providedID;
        this.uuid = uuid;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setIsManager(boolean manager) {
        isManager = manager;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getProvidedID() {
        return providedID;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void setPlayerDataConfig(PlayerDataConfig playerDataConfig) {
        this.playerDataConfig = playerDataConfig;
    }

    public void saveData() {
        playerDataConfig.saveData(this);
//        objectiveReminder.cancel();
    }

    public boolean isInTutorial() {
        return isInTutorial;
    }

    public void setInTutorial(boolean inTutorial) {
        isInTutorial = inTutorial;
    }

    public Location getChamberLocation() {
        return chamberLocation;
    }

    public void setChamberLocation(Location chamberLocation) {
        this.chamberLocation = chamberLocation;
    }

    public Objective getCurrentTutorialObjective() {
        return currentTutorialObjective;
    }

    public void setCurrentTutorialObjective(Objective currentTutorialObjective) {

        Player player = getPlayer();
        if (player != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        }

        this.currentTutorialObjective = currentTutorialObjective;
        sendObjectiveHelperMessage();

    }

    public void sendObjectiveHelperMessage(){
        Player player = getPlayer();
        player.sendMessage(" ");
        String helperMessage = currentTutorialObjective.getHelperMessage();
        player.sendMessage(StringUtils.colorString("&e[Objective Tip] &7" + helperMessage));
    }

    public SurveySession getSurveySession() {
        return surveySession;
    }

    public void setSurveySession(SurveySession surveySession) {
        this.surveySession = surveySession;
    }

    public void addInventoryData(Map<String, Integer> inventoryData) {
        this.inventoryDataList.add(inventoryData);
    }

    public List<Boolean> getIsUselessToolCrafted() {
        return isUselessToolCrafted;
    }

}

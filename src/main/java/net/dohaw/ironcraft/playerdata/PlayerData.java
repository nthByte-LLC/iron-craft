package net.dohaw.ironcraft.playerdata;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.Objective;
import net.dohaw.ironcraft.SurveySession;
import net.dohaw.ironcraft.config.PlayerDataConfig;
import net.dohaw.ironcraft.data_collection.DataCollector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private final String providedID;
    /**
     * Stores a list of inventoryData. New data is stored every "step"
     */
    private final List<TreeMap<String, Integer>> inventoryDataList = new ArrayList<>();
    /**
     * The gain order of items.
     * <br>
     * <b>Example:</b>
     * <br>
     * <i>coal</i> -> 1
     * <br>
     * <i>crafting_table -> 5</i>
     * <br><br>
     * The coal was the first item gained. The crafting table was the 5th item gained.
     * <br><br>
     * K -> The Item
     * <br>
     * V -> The gain order index
     */
    // I know this is supposed to be in a list form according to the document, but we can easily convert it to a list once we are ready to present the data to the algorithm via Map#values()
    private final Map<String, Integer> itemToGainIndex = new HashMap<>();
    /**
     * The time step at which an item was gained.
     */
    private final Map<String, Integer> itemToTimeStepGained = new HashMap<>();
    /**
     * Whether an iron axe, stone axe, or wooden axe were crafted.
     */
    private final Map<String, Boolean> isUselessToolCrafted = new HashMap<String, Boolean>() {
        private static final long serialVersionUID = 155837926307830916L;

        {
            put("iron_axe", false);
            put("stone_axe", false);
            put("wooden_axe", false);
        }
    };
    private SurveySession surveySession;
    private boolean isManager;
    private PlayerDataConfig playerDataConfig;
    private boolean isInTutorial;
    private Location chamberLocation;
    private Objective currentTutorialObjective;
    /**
     * A "step" is 0.05 seconds, or 50 ms. If currentStep == 2, then 100 ms have passed.
     */
    private int currentStep = 0;

    /**
     * Stores the number of times a player incorrectly uses a tool to break a block.
     */
    private int misuseActionSteps = 0;

    public PlayerData(UUID uuid, String providedID) {
        this.providedID = providedID;
        this.uuid = uuid;
        // Compiles the gain order map with the items that are tracked with a default value of 0
        DataCollector.TRACKED_ITEMS.forEach(item -> {
            itemToGainIndex.put(item.toString().toLowerCase(), 0);
        });
        DataCollector.TRACKED_ITEMS.forEach(item -> itemToTimeStepGained.put(item.toString().toLowerCase(), 0));
    }

    public void incMisuseActionSteps() {
        misuseActionSteps++;
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
        if (player != null) player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);

        this.currentTutorialObjective = currentTutorialObjective;
        sendObjectiveHelperMessage();

    }

    public void sendObjectiveHelperMessage() {
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

    public void addInventoryData(TreeMap<String, Integer> inventoryData) {
        inventoryDataList.add(inventoryData);
    }

    public Map<String, Boolean> getIsUselessToolCrafted() {
        return isUselessToolCrafted;
    }

    public Map<String, Integer> getItemToGainIndex() {
        return itemToGainIndex;
    }

    public int getNextGainIndex() {
        int currentHighestGain = 0;
        for (Integer num : itemToGainIndex.values()) if (num > currentHighestGain) currentHighestGain = num;
        return currentHighestGain + 1;
    }

    public void incrementCurrentStep() {
        currentStep++;
    }

    public Map<String, Integer> getItemToTimeStepGained() {
        return itemToTimeStepGained;
    }

    public int getCurrentStep() {
        return currentStep;
    }

}

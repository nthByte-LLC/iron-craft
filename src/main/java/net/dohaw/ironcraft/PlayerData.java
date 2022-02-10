package net.dohaw.ironcraft;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.manager.ManagementType;
import net.dohaw.ironcraft.config.PlayerDataConfig;
import net.dohaw.ironcraft.data_collection.DataCollectionUtil;
import net.dohaw.ironcraft.manager.ManagerUtil;
import net.dohaw.ironcraft.prompt.AutonomySurveyPrompt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
     * The UUID of your manager. If you have an AI manager, this will be null.
     */
    private UUID manager;

    /**
     * The type of management you are receiving.
     */
    private ManagementType managementType;

    /**
     * The users this player is currently managing.
     */
    private List<UUID> usersOverseeing = new ArrayList<>();

    /**
     * The player that the manager is currently focused on. Will be null if this player is not a manager.
     */
    private UUID focusedPlayerUUID;

    private BukkitTask teleporter, gameTimeTracker;

    /**
     * How many minutes the player has been playing the game.
     */
    private int minutesInGame;

    private String managerFeedback;

    /*
        Data collection variables
     */

    // Use in python model
    private int misuseActionSteps;
    private boolean hasSmeltedCoal;

    /**
     * A "step" is 0.05 seconds, or 50 ms. If currentStep = 2, then 100 ms have passed.
     */
    private int durationSteps = 0;

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
    private Map<String, Integer> itemToGainIndex = new HashMap<>();

    /**
     * The time step at which an item was gained.
     */
    private Map<String, Integer> itemToTimeStepGained = new HashMap<>();

    /**
     * Whether an iron axe, stone axe, or wooden axe were crafted.
     */
    private Map<String, Boolean> isUselessToolCrafted = new HashMap<String, Boolean>() {{
        put("iron_axe", false);
        put("stone_axe", false);
        put("wooden_axe", false);
    }};

    /**
     * The item name and the accumulated amount.
     */
    private Map<String, Integer> itemToAccumulatedAmount = new HashMap<>();

    /**
     * The amount of reward points the player has accumulated (SPARSE) per step.
     */
    private List<Integer> sparseRewardSequence = new ArrayList<>();

    /**
     * The amount of reward points the player has accumulated (DENSE) per step.
     */
    private List<Integer> denseRewardSequence = new ArrayList<>();

    /**
     * The number of times a player has attacked an entity
     */
    private int attackSteps = 0;

    /**
     * Each step we check to see if the player has moved their camera, meaning if their direction vector is different from last steps'. If so, this is increased.
     */
    private int cameraMovingSteps = 0;

    /**
     * Each step we check to see if the player has moved, meaning if their location is different from last steps'. If so, this is increased.
     */
    private int moveSteps = 0;

    /**
     * The amount of times a player had a wooden or stone pickaxe equipped *and* they attacked.
     */
    private int equippedAttackSteps = 0;

    /**
     * The player's camera direction in the last step
     */
    private Vector previousStepCameraDirection;

    /**
     * The player's location in the last step
     */
    private Location previousStepLocation;

    /**
     * The amount of items placed.
     */
    private Map<String, Integer> itemToAmountPlaced = new HashMap<String, Integer>(){{
        put("torch", 0);
        put("cobblestone", 0);
        put("dirt", 0);
        put("stone", 0);
    }};

    // Bad name. Can't figure out how else to explain it though.
    private HashSet<String> firstPickItems = new HashSet<>();

    public PlayerData(UUID uuid, String providedID) {
        this.providedID = providedID;
        this.uuid = uuid;
        // Compiles the gain order map with the items that are tracked with a default value of 0
        DataCollectionUtil.TRACKED_ITEMS.forEach(item -> {
            itemToGainIndex.put(item.toString().toLowerCase(), 0);
        });
        DataCollectionUtil.TRACKED_ITEMS.forEach(item -> {
            itemToTimeStepGained.put(item.toString().toLowerCase(), 0);
        });
        DataCollectionUtil.TRACKED_ITEMS.forEach(item -> {
            itemToAccumulatedAmount.put(item.toString().toLowerCase(), 0);
        });
        Location playerLocation = Bukkit.getPlayer(uuid).getLocation();
        this.previousStepLocation = playerLocation;
        this.previousStepCameraDirection = playerLocation.getDirection();
    }

    public void startTeleporter(IronCraftPlugin plugin){
        if(teleporter != null){
            teleporter.cancel();
        }
        teleporter = Bukkit.getScheduler().runTaskTimer(plugin, () -> teleportToFocusedPlayer(), 0L, 1L);
    }

    /**
     * Starts the task that keeps track of how many minutes the player has been in the game.
     * @param plugin An instance of the plugin.
     */
    public void startGameTimeTracker(IronCraftPlugin plugin){
        if(gameTimeTracker != null){
            gameTimeTracker.cancel();
        }
        gameTimeTracker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            minutesInGame++;
            if(minutesInGame == 7){

                gameTimeTracker.cancel();

                Player player = getPlayer();

                player.sendMessage(StringUtils.colorString("&cYour time is up!"));
                player.sendMessage("You will now take a survey. You won't be able to move for the duration of this survey. Don't worry, it'll be quick!");

                PlayerData managerData = plugin.getPlayerDataHandler().getData(manager);
                if(managerData.getFocusedPlayerUUID().equals(uuid)){
                    managerData.setFocusedPlayerUUID(null);
                }
                this.manager = null;

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getPersistentDataContainer().set(NamespacedKey.minecraft("is-answering-survey"), PersistentDataType.STRING, "marker");
                    Conversation conv = new ConversationFactory(plugin).withFirstPrompt(new AutonomySurveyPrompt(0, plugin)).withLocalEcho(false).buildConversation(player);
                    conv.begin();
                }, 20L * 3);

            }

        },20 * 60L, 20 * 60L);
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
    }

    public boolean hasMovedCamera(Vector currentDirection) {
        return currentDirection.getX() != previousStepCameraDirection.getX() || currentDirection.getY() != previousStepCameraDirection.getY() || currentDirection.getZ() != previousStepCameraDirection.getZ();
    }

    public boolean hasMoved(Location currentLocation) {
        return !previousStepLocation.equals(currentLocation);
    }

    public void addAccumulatedItem(ItemStack stack){
        String properName = DataCollectionUtil.itemToProperName(stack);
        int currentAmount = itemToAccumulatedAmount.get(properName);
        itemToAccumulatedAmount.put(properName, currentAmount + stack.getAmount());
    }

    public void sendObjectiveHelperMessage() {
        Player player = getPlayer();
        player.sendMessage(" ");
        String helperMessage = currentTutorialObjective.getHelperMessage();
        player.sendMessage(StringUtils.colorString("&e[Objective Tip] " + helperMessage));
    }

    /**
     * Adds the given item to the total amount of the item.
     *
     * @param type The material of the item to add
     */
    public void incrementPlacedItems(Material type) {
        String properItemName = DataCollectionUtil.itemToProperName(type == Material.WALL_TORCH ? Material.TORCH : type);
        int previousAmount = itemToAmountPlaced.getOrDefault(properItemName, 0);
        itemToAmountPlaced.put(properItemName, previousAmount + 1);
    }

    public int getNextGainIndex() {
        int currentHighestGain = 0;
        for (Integer num : itemToGainIndex.values()) {
            if (num > currentHighestGain) {
                currentHighestGain = num;
            }
        }
        return currentHighestGain + 1;
    }

    /**
     * Teleports the player to the player they are currently focused on. This method is usually only used if this player is a manager.
     */
    public void teleportToFocusedPlayer(){
        Player focusedPlayer = Bukkit.getPlayer(focusedPlayerUUID);
        if(focusedPlayer == null) return;
        Location focusedPlayerLoc = focusedPlayer.getLocation();
        Location clone = focusedPlayerLoc.clone();
        Location tpLoc = clone.clone().add(clone.getDirection().multiply(-2.5)).add(0, 0.5, 0);
        getPlayer().teleport(tpLoc);
    }

    public void initManager(IronCraftPlugin plugin){
        Player player = getPlayer();
        this.isManager = true;
        player.setGravity(false);
        player.setInvisible(true);
        player.setAllowFlight(true);
        player.setFlying(true);
        startTeleporter(plugin);
        ManagerUtil.sendManagerMessage(player);
    }

    public void initWorker(){
        Player player = getPlayer();
        this.isManager = false;
        player.setGravity(true);
        player.setInvisible(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        if(teleporter != null){
            teleporter.cancel();
        }
    }

    public SurveySession getSurveySession() {
        return surveySession;
    }

    public void setSurveySession(SurveySession surveySession) {
        this.surveySession = surveySession;
    }

    public Map<String, Boolean> getIsUselessToolCrafted() {
        return isUselessToolCrafted;
    }

    public Map<String, Integer> getItemToGainIndex() {
        return itemToGainIndex;
    }

    public HashSet<String> getFirstPickItems() {
        return firstPickItems;
    }

    public void incrementCurrentStep() {
        durationSteps++;
    }

    public Map<String, Integer> getItemToTimeStepGained() {
        return itemToTimeStepGained;
    }

    public int getDurationSteps() {
        return durationSteps;
    }

    public void incrementMisuseActionSteps() {
        misuseActionSteps++;
    }

    public void setHasSmeltedCoal(boolean b) {
        hasSmeltedCoal = b;
    }

    public void incrementAttackSteps() {
        attackSteps++;
    }

    public void incrementEquippedAttackSteps() {
        equippedAttackSteps++;
    }

    public void incrementCameraMovementSteps() {
        cameraMovingSteps++;
    }

    public void incrementMoveSteps() {
        moveSteps++;
    }

    /**
     * Whether the player has ever picked up this item before.
     */
    public boolean hasObtainedItemForFirstTime(ItemStack stack) {
        String properItemName = DataCollectionUtil.itemToProperName(stack);
        return firstPickItems.contains(properItemName);
    }

    public double computeCameraMovingRatio() {
        return cameraMovingSteps / (double) durationSteps;
    }

    public double computePositionMovingRatio() {
        return moveSteps / (double) durationSteps;
    }

    public double computeAttackRatio() {
        return attackSteps / (double) durationSteps;
    }

    public double computeAttackEfficiency() {
        return attackSteps != 0 ? getTotalExcavableInventory() / (double) attackSteps : 0;
    }

    public double computeEquippedAttackRatio() {
        return attackSteps != 0 ? equippedAttackSteps / (double) attackSteps : 0;
    }

    private int getTotalExcavableInventory() {
        return itemToAccumulatedAmount.get("log") + itemToAccumulatedAmount.get("cobblestone") + itemToAccumulatedAmount.get("raw_iron");
    }

    public List<Integer> getSparseRewardSequence() {
        return sparseRewardSequence;
    }

    public List<Integer> getDenseRewardSequence() {
        return denseRewardSequence;
    }

    public int getSparseTotalReward() {
        return sparseRewardSequence.get(sparseRewardSequence.size() - 1);
    }

    public int getDenseTotalReward() {
        return denseRewardSequence.get(denseRewardSequence.size() - 1);
    }

    public void setPreviousStepLocation(Location previousStepLocation) {
        this.previousStepLocation = previousStepLocation;
    }

    public void setPreviousStepCameraDirection(Vector previousStepCameraDirection) {
        this.previousStepCameraDirection = previousStepCameraDirection;
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

    public List<UUID> getUsersOverseeing() {
        return usersOverseeing;
    }

    public ManagementType getManagementType() {
        return managementType;
    }

    public void setManagementType(ManagementType managementType) {
        this.managementType = managementType;
    }

    public Objective getCurrentTutorialObjective() {
        return currentTutorialObjective;
    }

    public void setCurrentTutorialObjective(Objective currentTutorialObjective) {
        this.currentTutorialObjective = currentTutorialObjective;
        sendObjectiveHelperMessage();
    }

    public void setManager(UUID manager) {
        this.manager = manager;
    }

    public void setFocusedPlayerUUID(UUID focusedPlayerUUID) {
        this.focusedPlayerUUID = focusedPlayerUUID;
    }

    public UUID getFocusedPlayerUUID() {
        return focusedPlayerUUID;
    }

    public UUID getManager() {
        return manager;
    }

    public String getManagerFeedback() {
        return managerFeedback;
    }

    public void setManagerFeedback(String managerFeedback) {
        this.managerFeedback = managerFeedback;
    }

    public void writeDataToFile(IronCraftPlugin plugin) throws IOException {

        File file = new File(plugin.getDataFolder() + File.separator + "end_game_data", "input_" + uuid.toString() + ".yml");
        file.createNewFile();

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("gain order sequence", new ArrayList<>(itemToGainIndex.values()));
        config.set("gain order time", new ArrayList<>(itemToTimeStepGained.values()));
        config.set("accumulated amount of each item", new ArrayList<>(itemToAccumulatedAmount.values()));
        config.set("sparse reward accumulation sequence", sparseRewardSequence);
        config.set("dense reward accumulation sequence", denseRewardSequence);
        config.set("if useless tool was crafted", new ArrayList<>(isUselessToolCrafted.values()));
        config.set("sparse total reward", getSparseTotalReward());
        config.set("dense total reward", getDenseTotalReward());
        config.set("dense total reward", getDenseTotalReward());
        config.set("attack efficiency", computeAttackEfficiency());
        config.set("attack ratio", computeAttackRatio());
        config.set("equipped attack ratio", computeEquippedAttackRatio());
        config.set("camera moving ratio", computeCameraMovingRatio());
        config.set("position moving ratio", computePositionMovingRatio());
        config.set("placed items.torch", itemToAmountPlaced.get("torch"));
        config.set("placed items.cobblestone", itemToAmountPlaced.get("cobblestone"));
        config.set("placed items.dirt", itemToAmountPlaced.get("dirt"));
        config.set("placed items.stone", itemToAmountPlaced.get("stone"));
        config.set("if smelt coal", hasSmeltedCoal);
        config.save(file);

    }

    @Override
    public String toString() {
        return "PlayerData{ " + uuid.toString() + "; isManager: " + isManager + "; isInTutorial: " + isInTutorial + "}";
    }

}

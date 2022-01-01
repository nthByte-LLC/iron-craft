package net.dohaw.ironcraft.data_collection;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.TreeMap;

/**
 * Class which contains the functionality to collect certain data from players.
 */
public class DataCollector extends BukkitRunnable {


    /**
     * Instance of the IronCraft plugin.
     */
    private IronCraftPlugin plugin;

    public DataCollector(IronCraftPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (PlayerData data : plugin.getPlayerDataHandler().getAllPlayerData().values()) {
            // We only want to collect data for players that are playing the actual game, and aren't managers.
            // Player would be null if the player isn't online.
            if (data.isInTutorial() || data.isManager() || data.getPlayer() == null) continue;
            data.incrementCurrentStep();
            compileInventoryKeepingSequence(data);
            compileSparseRewardSequence(data);
            compileDenseRewardSequence(data);
            dealWithCameraInformation(data);
            dealWithMovementInformation(data);
        }
    }

    /**
     * Compiles the inventory keeping sequence for this particular step.
     */
    private void compileInventoryKeepingSequence(PlayerData playerData) {

        Player player = playerData.getPlayer();
        Inventory inventory = player.getInventory();
        TreeMap<String, Integer> invData = new TreeMap<>();
        for (ItemStack itemStack : inventory.getContents()) {

            if (itemStack == null || !DataCollectionUtil.isTrackedItem(itemStack)) continue;

            String properItemName = DataCollectionUtil.itemToProperName(itemStack);
            invData.put(properItemName, getTotalItem(player, properItemName));

        }
        playerData.addInventoryData(invData);

    }

    /**
     * Compiles the sparse reward entry for this particular step (See Data Collection document for details).
     */
    private void compileSparseRewardSequence(PlayerData playerData){

        Player player = playerData.getPlayer();
        Inventory inventory = player.getInventory();

        int rewardPointsGained = 0;
        List<Integer> currentSparseRewardSequence = playerData.getSparseRewardSequence();
        int previousStepRewardAmount = 0;
        if(currentSparseRewardSequence.size() != 0){
            previousStepRewardAmount = currentSparseRewardSequence.get(currentSparseRewardSequence.size() - 1);
        }

        for(ItemStack stack : inventory.getContents()){

            if(stack == null || !DataCollectionUtil.isTrackedItem(stack)) continue;

            String properItemName = DataCollectionUtil.itemToProperName(stack);
            // Has previously picked up this item before. We only want to accumulate the reward amount if we are picking up an item for the *first* time.
            if(playerData.hasPickedUpItem(stack)) continue;

            int stackRewardMapping = DataCollectionUtil.REWARD_MAPPINGS.get(properItemName);
            rewardPointsGained += stackRewardMapping;

        }

        currentSparseRewardSequence.add(previousStepRewardAmount + rewardPointsGained);

    }

    /**
     * Compiles the dense reward entry for this particular step (See Data Collection document for details).
     */
    private void compileDenseRewardSequence(PlayerData playerData){

        Player player = playerData.getPlayer();
        Inventory inventory = player.getInventory();

        int accumulatedRewardPoints = 0;
        List<Integer> currentSparseRewardSequence = playerData.getDenseRewardSequence();

        for(ItemStack stack : inventory.getContents()){

            if(stack == null || !DataCollectionUtil.isTrackedItem(stack)) continue;

            String properItemName = DataCollectionUtil.itemToProperName(stack);
            int stackRewardMapping = DataCollectionUtil.REWARD_MAPPINGS.get(properItemName);
            accumulatedRewardPoints += (stackRewardMapping * stack.getAmount());

        }

        currentSparseRewardSequence.add(accumulatedRewardPoints);

    }

    /**
     * Deals with everything involving camera information.
     */
    private void dealWithCameraInformation(PlayerData playerData){

        Player player = playerData.getPlayer();
        Vector currentDirection = player.getLocation().getDirection();
        if(playerData.getPreviousStepCameraDirection() == null){
            playerData.setPreviousStepCameraDirection(currentDirection);
            playerData.incrementCameraMovementSteps();
            return;
        }

        if(playerData.hasMovedCamera(currentDirection)){
            playerData.incrementCameraMovementSteps();
        }

        playerData.setPreviousStepCameraDirection(currentDirection);

    }

    /**
     * Deals with everything involving movement steps.
     */
    private void dealWithMovementInformation(PlayerData playerData){

        Player player = playerData.getPlayer();
        Location currentLocation = player.getLocation();
        if(playerData.getPreviousStepLocation() == null){
            playerData.setPreviousStepLocation(currentLocation);
            playerData.incrementMoveSteps();
            return;
        }

        if(playerData.hasMoved(currentLocation)){
            playerData.incrementMoveSteps();
        }

        playerData.setPreviousStepLocation(currentLocation);

    }

    /**
     * Gets the total amount of an item
     *
     * @param player The player whose inventory we are checking
     * @param item Can/should either be a Material or a String. If it's a String, it checks to see if the item material has the keyword.
     */
    private int getTotalItem(Player player, Object item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            if (item instanceof Material) {
                Material mat = (Material) item;
                if (stack.getType() == mat) total += stack.getAmount();
            } else if (item instanceof String) {
                String keyword = (String) item;
                if (stack.getType().toString().contains(keyword)) total += stack.getAmount();
            }

        }
        return total;
    }

}

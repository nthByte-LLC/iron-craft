package com.nthbyte.ironcraft.manager;

import com.nthbyte.ironcraft.IronCraftPlugin;
import com.nthbyte.ironcraft.event.AssignManagerEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.npc.CitizensNPC;
import net.dohaw.corelib.StringUtils;
import com.nthbyte.ironcraft.PlayerData;
import com.nthbyte.ironcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ManagerUtil {

    private static final String[] RANDOM_NPC_NAMES = {"md_5", "WolverineX15", "Santa", "Bilbow", "Steve"};

    /**
     * How many users you can manager at once.
     */
    public static final int OVERSEEING_USER_LIMIT = 2;

    /**
     * Assigns the player a manager upon joining the server. Could either be a human or AI manager.
     * @param data The data of the player without a manager.
     */
    public static void assignManager(PlayerData data) {

        if(data.isManager() || data.isAdmin()) return;

        ThreadLocalRandom current = ThreadLocalRandom.current();
        // TODO: switch this back
        boolean hasAIManager = false /*current.nextBoolean()*/;
        if (hasAIManager) {
            data.setManagementType(ManagementType.AI);
            return;
        } else {
            data.setManagementType(ManagementType.HUMAN);
        }

        if(data.getManagerNPC() == null){
            setupNPCManager(data);
        }

        List<PlayerData> allPlayerData = new ArrayList<>(IronCraftPlugin.getInstance().getPlayerDataHandler().getAllPlayerData().values());

        // Filters out the people that aren't managers, already have the overseeing user limit, or if it's the player we are trying to assign a manager.
        allPlayerData.removeIf(playerData -> !playerData.isManager()
            || playerData.getUsersOverseeing().size() == OVERSEEING_USER_LIMIT
            || playerData.getUuid().equals(data.getUuid())
            || playerData.isAdmin()
        );

        if(allPlayerData.size() == 0) {
            System.out.println("Remaining is 0");
            data.setManagementType(ManagementType.AI);
            return;
        }

        PlayerData managerData = allPlayerData.get(0);
        Player manager = managerData.getPlayer();
        List<UUID> usersOverseeing = managerData.getUsersOverseeing();

        UUID uuid = data.getUuid();
        usersOverseeing.add(uuid);
        // Sets their the only player they're managing as their focus
        if(usersOverseeing.size() == 1){
            managerData.setFocusedPlayerUUID(uuid);
        }

        data.setManager(managerData.getUuid());

        manager.sendMessage(StringUtils.colorString("You are now managing &e" + data.getPlayer().getName()));
        Bukkit.getServer().getPluginManager().callEvent(new AssignManagerEvent(managerData, data));

    }

    private static void setupNPCManager(PlayerData user){

        if(!CitizensAPI.hasImplementation()){
            return;
        }

        CitizensNPC npc = (CitizensNPC) CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, RANDOM_NPC_NAMES[ThreadLocalRandom.current().nextInt(RANDOM_NPC_NAMES.length)]);
        user.setManagerNPC(npc);
        Location spawnLocation = getNPCManagerTPLocation(user.getPlayer());
        npc.spawn(spawnLocation);
        npc.setAlwaysUseNameHologram(false);

        LivingEntity entity = (LivingEntity) npc.getEntity();
        entity.setAI(false);
        entity.setGravity(false);
        entity.setCollidable(false);
        entity.setCustomNameVisible(false);

    }

    /**
     * Ensures that all players that are in the game have a manager. If not, then it assigns them one.
     * There is the possibility that they do not get assigned a manager at all if all the managers in the game are already managing a maximum amount of players (Currently is capped at 2)
     */
    public static void ensurePlayersHaveManagers(IronCraftPlugin plugin){
        for(PlayerData pd : plugin.getPlayerDataHandler().getPlayerDataList()){
            if(pd.getManager() == null && !pd.isManager() && !pd.isInTutorial()){
                System.out.println("Assigning " + pd.getPlayer().getName());
                ManagerUtil.assignManager(pd);
            }
        }
    }

    public static void sendManagerMessage(Player player){
        player.sendRawMessage("You are a manager in the iron pickaxe factory. You will supervise 2 to 5 workers, who should make an iron pickaxe within 7 mins.");
        player.sendRawMessage("As a manager, your task is to keep an eye on their performance and evaluate them after the 7-min session expires. You can rate each of them on three levels: Beginner level ($0), Intermediate level ($0.2), or Advanced level ($0.5). Your ratings will decide their pay in the session as shown in the brackets.");
        player.sendRawMessage("Left click to switch your focus between players that you manage.");
    }

    /**
     * Acquires the location that the <b>human manager</b> is supposed to be teleported to relative to the user.
     * The location is supposed to be a birds eyes view of the user's view (Behind them and slightly higher than them).
     * @param relativeUser The user playing the game.
     * @return The location that the NPC manager is supposed to be relative to the user.
     */
    public static Location getHumanManagerTPLocation(Player relativeUser){
        Location focusedPlayerLoc = relativeUser.getLocation();
        Location clone = focusedPlayerLoc.clone();
        return clone.clone().add(clone.getDirection().multiply(-2.5)).add(0, 0.5, 0);
    }

    /**
     * Acquires the location that the <b>npc manager</b> is supposed to be teleported to relative to the user.
     * The location is supposed to be in front of the player so that they can see them.
     * @param relativeUser The user playing the game.
     * @return The location that the NPC manager is supposed to be relative to the user.
     */
    public static Location getNPCManagerTPLocation(Player relativeUser){
        Location focusedPlayerLoc = relativeUser.getLocation();
        Location clone = focusedPlayerLoc.clone();
        return LocationUtil.getLocationToLeft(clone.clone().add(clone.getDirection().multiply(2.5)).add(0, 2, 0), 2);
    }

}

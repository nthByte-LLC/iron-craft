package net.dohaw.ironcraft.manager;

import net.citizensnpcs.api.CitizensAPI;
import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.event.AssignManagerEvent;
import net.dohaw.ironcraft.PlayerData;
import net.dohaw.ironcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ManagerUtil {

    /**
     * How many users you can manager at once.
     */
    public static final int OVERSEEING_USER_LIMIT = 2;

    /**
     * Assigns the player a manager upon joining the server. Could either be a human or AI manager.
     * @param data The data of the player without a manager.
     */
    public static void assignManager(PlayerData data) {

        ThreadLocalRandom current = ThreadLocalRandom.current();
        // TODO: switch this back
        boolean hasAIManager = false /*current.nextBoolean()*/;
        if (hasAIManager) {
            data.setManagementType(ManagementType.AI);
            return;
        } else {
            data.setManagementType(ManagementType.HUMAN);
        }

        List<PlayerData> allPlayerData = new ArrayList<>(IronCraftPlugin.getInstance().getPlayerDataHandler().getAllPlayerData().values());

        // Filters out the people that aren't managers, already have the overseeing user limit, or if it's the player we are trying to assign a manager.
        allPlayerData.removeIf(playerData -> !playerData.isManager()
            || playerData.getUsersOverseeing().size() == OVERSEEING_USER_LIMIT
            || playerData.getUuid().equals(data.getUuid())
        );

        if(allPlayerData.size() == 0) {
            data.setManagementType(ManagementType.AI);
            return;
        }

        PlayerData managerData = allPlayerData.get(0);
        Player manager = managerData.getPlayer();
        List<UUID> usersOverseeing = managerData.getUsersOverseeing();

        UUID uuid = data.getUuid();
        usersOverseeing.add(uuid);
        if(usersOverseeing.size() == 1){
            managerData.setFocusedPlayerUUID(uuid);
        }

        data.setManager(managerData.getUuid());

        manager.sendMessage(StringUtils.colorString("You are now managing &e" + data.getPlayer().getName()));
        Bukkit.getServer().getPluginManager().callEvent(new AssignManagerEvent(managerData, data));

    }

    private static void setupNPCManager(PlayerData user){

        NPC npc = (NPC) CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "C10_MC");
        user.setManagerNPC(npc);

        npc.setAI(false);
        npc.setGravity(false);
        npc.setCollidable(false);
        npc.setAware(false);

    }

    public static void sendManagerMessage(Player player){
        player.sendRawMessage("You are a manager in the iron pickaxe factory. You will supervise 2 to 5 workers, who should make an iron pickaxe within 7 mins.");
        player.sendRawMessage("As a manager, your task is to keep an eye on their performance and evaluate them after the 7-min session expires. You can rate each of them on three levels: Beginner level ($0), Intermediate level ($0.2), or Advanced level ($0.5). Your ratings will decide their pay in the session as shown in the brackets.");
        player.sendRawMessage("Left click to switch your focus between players that you manage.");
    }

}

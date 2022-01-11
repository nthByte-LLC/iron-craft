package net.dohaw.ironcraft.manager;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.event.AssignManagerEvent;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ManagerUtil {

    /**
     * How many users you can manager at once.
     */
    private static final int OVERSEEING_USER_LIMIT = 2;

    /**
     * Assigns the player a manager upon joining the server. Could either be a human or AI manager.
     * @param data The data of the player without a manager.
     */
    public static void assignManager(PlayerData data) {

        ThreadLocalRandom current = ThreadLocalRandom.current();
        boolean hasAIManager = false;
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
        System.out.println("ALL: " + allPlayerData.toString());

        if(allPlayerData.size() == 0) {
            data.setManagementType(ManagementType.AI);
            return;
        }

        PlayerData managerData = allPlayerData.get(0);
        Player manager = managerData.getPlayer();
        managerData.getUsersOverseeing().add(data.getUuid());
        manager.setAllowFlight(true);
        manager.setFlying(true);

        data.setManager(managerData.getUuid());

        manager.sendMessage(StringUtils.colorString("You are now managing &e" + data.getPlayer().getName()));
        Bukkit.getServer().getPluginManager().callEvent(new AssignManagerEvent(managerData, data));

    }

}
package net.dohaw.ironcraft.handler;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.Objective;
import net.dohaw.ironcraft.config.PlayerDataConfig;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataHandler {

    private final IronCraftPlugin plugin;
    private final Map<UUID, PlayerData> allPlayerData = new HashMap<>();

    public PlayerDataHandler(IronCraftPlugin plugin) {
        this.plugin = plugin;
        // Updates player's scoreboards every second
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerData data : allPlayerData.values()) {
                plugin.updateScoreboard(data.getPlayer());
            }
        }, 0L, 20L);
    }

    public boolean hasExistingPlayerData(String providedID) {
        File file = new File(plugin.getDataFolder() + File.separator + "player_data", providedID + ".yml");
        return file.exists();
    }

    public boolean createData(UUID uuid, String providedID) {

        File file = new File(plugin.getDataFolder() + File.separator + "player_data", providedID + ".yml");
        boolean hasFileBeenMade = false;
        try {
            hasFileBeenMade = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PlayerData playerData = new PlayerData(uuid, providedID);
        playerData.setPlayerDataConfig(new PlayerDataConfig(file.getName()));
        playerData.setInTutorial(true);
        playerData.setCurrentTutorialObjective(Objective.MOVE);
        allPlayerData.put(uuid, playerData);

        return hasFileBeenMade;

    }

    public PlayerData loadData(String providedID) {
        String fileName = providedID + ".yml";
        PlayerDataConfig playerDataConfig = new PlayerDataConfig(fileName);
        PlayerData pd = playerDataConfig.loadData();
        allPlayerData.put(pd.getUuid(), pd);
        return pd;
    }

    public void saveData(UUID uuid) {
        allPlayerData.remove(uuid).saveData();
    }

    public void saveAllData() {
        for (PlayerData data : allPlayerData.values()) {
            data.saveData();
        }
    }

    public boolean hasDataLoaded(UUID uuid) {
        return allPlayerData.containsKey(uuid);
    }

    public PlayerData getData(UUID uuid) {
        return allPlayerData.get(uuid);
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return allPlayerData;
    }
}

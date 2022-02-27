package net.dohaw.ironcraft.handler;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.Objective;
import net.dohaw.ironcraft.config.PlayerDataConfig;
import net.dohaw.ironcraft.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
            data.getPlayer().getPersistentDataContainer().remove(IronCraftPlugin.IN_SURVEY_PDC_KEY);
            data.saveData();
        }
    }

    public boolean hasDataLoaded(UUID uuid) {
        return allPlayerData.containsKey(uuid);
    }

    public boolean hasDataLoaded(Player player){
        return allPlayerData.containsKey(player.getUniqueId());
    }

    public PlayerData getData(UUID uuid) {
        return allPlayerData.get(uuid);
    }

    public PlayerData getData(Player player){
        return getData(player.getUniqueId());
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return allPlayerData;
    }

    public List<PlayerData> getPlayerDataList(){
        return new ArrayList<>(allPlayerData.values());
    }

    public List<PlayerData> getPlayerDataList(Player player){
        List<PlayerData> list = new ArrayList<>(allPlayerData.values());
        list.removeIf(pd -> pd.getUuid().equals(player.getUniqueId()));
        return list;
    }

    public boolean isManager(Player player){
        if(!hasDataLoaded(player)) return false;
        return getData(player).isManager();
    }

}

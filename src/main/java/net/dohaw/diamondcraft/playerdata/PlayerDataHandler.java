package net.dohaw.diamondcraft.playerdata;

import net.dohaw.diamondcraft.DiamondCraftPlugin;
import net.dohaw.diamondcraft.config.PlayerDataConfig;

import javax.xml.stream.Location;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDataHandler {

    private DiamondCraftPlugin plugin;

    private Map<UUID, PlayerData> allPlayerData = new HashMap<>();

    public PlayerDataHandler(DiamondCraftPlugin plugin){
        this.plugin = plugin;
    }

    public boolean hasExistingPlayerData(String providedID){
        File file = new File(plugin.getDataFolder() + File.separator + "player_data", providedID + ".yml");
        return file.exists();
    }

    public boolean createData(UUID uuid, String providedID){

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
        allPlayerData.put(uuid, playerData);

        return hasFileBeenMade;

    }

    public void loadData(String providedID){
        String fileName = providedID + ".yml";
        PlayerDataConfig playerDataConfig = new PlayerDataConfig(fileName);
        PlayerData pd = playerDataConfig.loadData();
        allPlayerData.put(pd.getUuid(), pd);
    }

    public void saveData(UUID uuid){
        allPlayerData.remove(uuid).saveData();
    }

    public boolean hasDataLoaded(UUID uuid){
        return allPlayerData.containsKey(uuid);
    }

    public PlayerData getData(UUID uuid){
        return allPlayerData.get(uuid);
    }

}

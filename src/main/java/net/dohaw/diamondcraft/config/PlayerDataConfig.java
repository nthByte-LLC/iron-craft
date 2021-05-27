package net.dohaw.diamondcraft.config;

import net.dohaw.corelib.Config;
import net.dohaw.diamondcraft.playerdata.PlayerData;

import java.io.File;
import java.util.UUID;

public class PlayerDataConfig extends Config{

    public PlayerDataConfig(String fileName) {
        super("player_data" + File.separator + fileName);
    }

    public PlayerData loadData(){

        UUID uuid = UUID.fromString(config.getString("uuid"));
        String providedID = file.getName().replace(".yml", "");
        PlayerData playerData = new PlayerData(uuid, providedID);

        playerData.setPlayerDataConfig(this);
        playerData.setIsManager(config.getBoolean("Is Manager"));
        playerData.setInTutorial(config.getBoolean("Is In Tutorial"));
        playerData.setChamberLocation(config.getLocation("Chamber Location"));

        return playerData;
    }

    public void saveData(PlayerData data){
        config.set("uuid", data.getUuid().toString());
        config.set("Is Manager", data.isManager());
        config.set("Is In Tutorial", data.isInTutorial());
        config.set("Chamber Location", data.getChamberLocation());
        saveConfig();
    }

}

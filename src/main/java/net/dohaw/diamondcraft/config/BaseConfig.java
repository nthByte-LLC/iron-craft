package net.dohaw.diamondcraft.config;

import net.dohaw.corelib.Config;
import net.dohaw.corelib.serializers.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class BaseConfig extends Config {

    public BaseConfig() {
        super("config.yml");
    }

//    public List<Tribe> getTribes(){
//
//        ConfigurationSection tribeSection = config.getConfigurationSection("Tribes");
//        // KEYS: Example Tribe, Example Tribe 2
//        for(String key : tribeSection.getKeys(false)){
//            // Tribes.Example Tribe.Name
//            String name = tribeSection.getString(key + ".Name");
//            Material menuMaterial = Material.valueOf(tribeSection.getString(key + ".Material", "APPLE"));
//            if(menuMaterial == Material.PLAYER_HEAD){
//                String playerHeadOwner = tribeSection.getString(key + ".Player Head Owner");
//            }
//
//            int slotNumber = tribeSection.getInt(key + ".Slot Number");
//            List<String> lore = tribeSection.getStringList(key + ".Lore");
//
//
//        }
//
//    }

    public World getWorld(){
        String worldName = config.getString("World", "d_crafting");
        return Bukkit.getWorld(worldName);
    }

    public List<Location> getChamberLocations(){
        return (List<Location>) config.getList("Available Chamber Locations", new ArrayList<>());
    }

    public List<Location> getSpawnLocations(){

        List<String> strLocations = config.getStringList("Spawn Locations");
        List<Location> spawnLocations = new ArrayList<>();

        LocationSerializer locationSerializer = new LocationSerializer();
        for(String strLocation : strLocations){
            spawnLocations.add(locationSerializer.toLocation(strLocation));
        }

        return spawnLocations;

    }

    public void saveChamberLocations(List<Location> chamberLocations){
        config.set("Available Chamber Locations", chamberLocations);
        saveConfig();
    }

    public void saveSpawnLocations(List<Location> spawnLocations){
        config.set("Spawn Locations", spawnLocations);
        saveConfig();
    }

}

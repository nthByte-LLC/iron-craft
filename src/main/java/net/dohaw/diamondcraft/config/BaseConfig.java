package net.dohaw.diamondcraft.config;

import net.dohaw.corelib.Config;
import net.dohaw.corelib.serializers.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class BaseConfig extends Config {

    public BaseConfig() {
        super("config.yml");
    }

    public World getWorld(){
        String worldName = config.getString("World", "d_crafting");
        return Bukkit.getWorld(worldName);
    }

    public List<Location> getChamberLocations(){

        List<Location> chamberLocations = (List<Location>) config.getList("Available Chamber Locations");
        System.out.println("GET: " + config.get("Available Chamber Locations"));

        System.out.println("CHAMBER LOACTIONS: " + chamberLocations.toString());

        return chamberLocations;
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
        System.out.println("SAVE CHAMBER LOCATIONS: " + chamberLocations.toString());
        config.set("Available Chamber Locations", chamberLocations);
        saveConfig();
    }

    public void saveSpawnLocations(List<Location> spawnLocations){
        config.set("Spawn Locations", spawnLocations);
        saveConfig();
    }

}

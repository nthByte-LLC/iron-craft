package net.dohaw.ironcraft.config;

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

    public World getWorld() {
        String worldName = config.getString("World", "d_crafting");
        return Bukkit.getWorld(worldName);
    }

    public List<Location> getChamberLocations() {

        List<String> strLocations = config.getStringList("Available Chamber Locations");
        List<Location> locations = new ArrayList<>();

        LocationSerializer ls = new LocationSerializer();
        for (String strLocation : strLocations) {
            locations.add(ls.toLocation(strLocation));
        }

        return locations;

    }

    public List<Location> getSpawnLocations() {

        List<String> strLocations = config.getStringList("Spawn Locations");
        List<Location> spawnLocations = new ArrayList<>();

        LocationSerializer locationSerializer = new LocationSerializer();
        for (String strLocation : strLocations) {
            spawnLocations.add(locationSerializer.toLocation(strLocation));
        }

        System.out.println("STR LOCATIONS: " + strLocations);
        System.out.println("SPAWN LOCATIONS: " + spawnLocations);

        return spawnLocations;

    }

    public void saveChamberLocations(List<Location> chamberLocations) {
        List<String> strLocations = new ArrayList<>();
        LocationSerializer ls = new LocationSerializer();
        for (Location loc : chamberLocations) {
            strLocations.add(ls.toString(loc));
        }
        config.set("Available Chamber Locations", strLocations);
        saveConfig();
    }

    public void saveSpawnLocations(List<Location> spawnLocations) {
        List<String> strLocations = new ArrayList<>();
        LocationSerializer ls = new LocationSerializer();
        for (Location loc : spawnLocations) {
            strLocations.add(ls.toString(loc));
        }
        config.set("Spawn Locations", strLocations);
        saveConfig();
    }

}

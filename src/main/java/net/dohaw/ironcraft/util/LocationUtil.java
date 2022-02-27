package net.dohaw.ironcraft.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationUtil {

    /**
     * Acquires the location that the NPC manager is supposed to be relative to the user.
     * @param relativeUser The user playing the game.
     * @return The location that the NPC manager is supposed to be relative to the user.
     */
    public static Location getRelativeManagerLocation(Player relativeUser){
        Location focusedPlayerLoc = relativeUser.getLocation();
        Location clone = focusedPlayerLoc.clone();
        return clone.clone().add(clone.getDirection().multiply(-2.5)).add(0, 0.5, 0);
    }

}

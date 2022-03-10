package com.nthbyte.ironcraft.util;

import org.bukkit.Location;

public class LocationUtil {

    public static Location getLocationToLeft(Location loc, double dist){
        Location leftDirectionLocation = loc.clone();
        // shifts direction to the left
        leftDirectionLocation.setYaw(leftDirectionLocation.getYaw() - 90);
        return loc.clone().add(leftDirectionLocation.getDirection().multiply(dist));
    }

//    public static Location getAbsoluteLocationToLeft(Location location, double numBlocksToLeft){
//        Location clone = location.clone();
//        clone.setPitch(0);
//        return getLocationToLeft(clone, numBlocksToLeft);
//    }

}

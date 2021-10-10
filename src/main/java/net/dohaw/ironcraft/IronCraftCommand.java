package net.dohaw.ironcraft;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IronCraftCommand implements CommandExecutor {

    private IronCraftPlugin plugin;

    public IronCraftCommand(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0 && sender instanceof Player && sender.hasPermission("diamondcraft.admin")) {

            Player playerSender = (Player) sender;
            if (args[0].equalsIgnoreCase("create")) {

                Location senderLocation = playerSender.getLocation();
                if (args[1].equalsIgnoreCase("chamber")) {
                    if (!isUniqueLocation(senderLocation, plugin.getAvailableChamberLocations())) {
                        sender.sendMessage("This is not a unique location!");
                        return false;
                    }
                    plugin.getAvailableChamberLocations().add(senderLocation);
                } else if (args[1].equalsIgnoreCase("spawn")) {
                    if (!isUniqueLocation(senderLocation, plugin.getJourneySpawnPoints())) {
                        sender.sendMessage("This is not a unique location!");
                        return false;
                    }
                    plugin.getJourneySpawnPoints().add(senderLocation);
                } else {
                    return false;
                }

                sender.sendMessage("This location has been set!");

            }

        }

        return false;

    }

    private boolean isUniqueLocation(Location senderLocation, List<Location> locations) {
        for (Location location : locations) {
            if (senderLocation.distance(location) <= 5) {
                return false;
            }
        }
        return true;
    }

}

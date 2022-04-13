package com.nthbyte.ironcraft;

import com.nthbyte.ironcraft.manager.ManagerUtil;
import net.dohaw.corelib.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IronCraftCommand implements CommandExecutor {

    private final IronCraftPlugin plugin;

    public IronCraftCommand(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0) {
            sender.sendMessage("/ic create chamber");
            sender.sendMessage("/ic create spawn");
            sender.sendMessage("/ic set obj <player name> <objective>");
            sender.sendMessage("/ic set ingame <player name> <true | false>");
            return true;
        }

        if (args.length > 0 && sender instanceof Player && sender.hasPermission("diamondcraft.admin")) {

            Player playerSender = (Player) sender;
            String firstArg = args[0];
            if (firstArg.equalsIgnoreCase("create")) {

                Location senderLocation = playerSender.getLocation();
                String secondArg = args[1];
                if (secondArg.equalsIgnoreCase("chamber")) {
                    if (!isUniqueLocation(senderLocation, plugin.getAvailableChamberLocations())) {
                        sender.sendMessage("This is not a unique location!");
                        return false;
                    }
                    plugin.getAvailableChamberLocations().add(senderLocation);
                } else if (secondArg.equalsIgnoreCase("spawn")) {
                    if (!isUniqueLocation(senderLocation, plugin.getJourneySpawnPoints())) {
                        sender.sendMessage("This is not a unique location!");
                        return false;
                    }
                    plugin.getJourneySpawnPoints().add(senderLocation);
                } else {
                    return false;
                }

                sender.sendMessage("This location has been set!");

                // ic set obj
            }else if(firstArg.equalsIgnoreCase("set")){
                String secondArg = args[1];
                if(args.length >= 3){
                    String targetPlayerName = args[2];
                    String fourthArg = args[3];
                    if(secondArg.equalsIgnoreCase("obj") || secondArg.equalsIgnoreCase("ingame")){

                        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
                        if(targetPlayer == null){
                            sender.sendMessage(StringUtils.colorString("&cThis is not a valid player!"));
                            return false;
                        }

                        PlayerData targetPlayerData = plugin.getPlayerDataHandler().getData(targetPlayer);
                        if(secondArg.equalsIgnoreCase("obj")) {
                            Objective objective;
                            try {
                                objective = Objective.valueOf(fourthArg.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(StringUtils.colorString("&cThis is not a valid objective!"));
                                return false;
                            }
                            targetPlayer.sendMessage("Your objective has been set to " + objective);
                            targetPlayerData.setCurrentTutorialObjective(objective);
                            sender.sendMessage("This player's objective has been set to " + objective);
                        }else{
                            boolean isInGame;
                            try{
                                isInGame = Boolean.parseBoolean(fourthArg);
                            }catch(IllegalArgumentException e){
                                sender.sendMessage(StringUtils.colorString("&cThis is not a valid argument! It must be \"true\" or \"false\""));
                                return false;
                            }
                            targetPlayer.sendMessage("You have been moved to the game stage");
                            sender.sendMessage("This player has been moved to the game stage");
                            targetPlayerData.setInTutorial(!isInGame);
                            targetPlayerData.init(plugin, targetPlayerData.isManager());
                        }

                        ManagerUtil.ensurePlayersHaveManagers(plugin);

                    }
                }
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

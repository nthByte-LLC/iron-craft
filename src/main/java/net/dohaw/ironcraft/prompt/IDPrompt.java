package net.dohaw.ironcraft.prompt;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.handler.PlayerDataHandler;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class IDPrompt extends StringPrompt {

    private IronCraftPlugin plugin;

    public IDPrompt(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "Hello there! Please input your ID. You can do this by pressing \"T\" on your keyboard and inputting it into chat! \nIf you are a returning player, please make sure you enter your previous ID!";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String providedID) {

        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        Player player = (Player) context.getForWhom();

        if (playerDataHandler.hasExistingPlayerData(providedID)) {
            PlayerData data = playerDataHandler.loadData(providedID);
            // Delayed because data#sendObjectiveHelperMessage does not send a raw message.
            // You can only send raw messages to the player if they are conversing. The player will not be conversing 2 ticks from now.
            Bukkit.getScheduler().runTaskLater(plugin, data::sendObjectiveHelperMessage, 2);
        } else {
            boolean wasDataCreated = playerDataHandler.createData(player.getUniqueId(), providedID);
            if (!wasDataCreated) {
                plugin.getLogger().severe("There was an error trying to create player data for " + player.getName());
                player.sendRawMessage("There was an error! Please contact an administrator...");
            } else {

                Location randomChamberLocation = plugin.getRandomChamber();
                if (randomChamberLocation == null) {
                    plugin.getLogger().severe("There has been an error trying to teleport a player to a training chamber");
                    player.sendRawMessage("You could not be teleported to a training chamber at this moment. Please contact an administrator...");
                    return null;
                }

                player.getInventory().clear();
                plugin.giveEssentialItems(player);
                player.teleport(randomChamberLocation);
                playerDataHandler.getData(player.getUniqueId()).setChamberLocation(randomChamberLocation);

                player.sendRawMessage("Welcome to the training chamber! This is where you will be taught to mine a diamond!");
                player.sendRawMessage("If you look to the right of your screen, you will see (in order) the objectives you need to complete");
                player.sendRawMessage("If you ever get confused, just look in chat. We will be giving you helpful tips along your training session!");

            }
        }

        plugin.updateScoreboard(player);

        return null;

    }

}

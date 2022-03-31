package com.nthbyte.ironcraft.prompt;

import com.nthbyte.ironcraft.IronCraftPlugin;
import com.nthbyte.ironcraft.manager.ManagerUtil;
import com.nthbyte.ironcraft.handler.PlayerDataHandler;
import com.nthbyte.ironcraft.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class IDPrompt extends StringPrompt {

    private final IronCraftPlugin plugin;

    public IDPrompt(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "Hello there! Please input your ID. You can do this by pressing \"T\" on your keyboard and inputting it into chat! \nIf you are a returning player, please make sure you enter your previous ID!";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String providedID) {

        Player player = (Player) context.getForWhom();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        // Allows admin player data.
        if(providedID.contains("admin") && player.isOp()){
            playerDataHandler.getAllPlayerData().put(player.getUniqueId(), PlayerData.createAdminData(player));
            player.getPersistentDataContainer().remove(IronCraftPlugin.IN_SURVEY_PDC_KEY);
            return END_OF_CONVERSATION;
        }

        player.setGravity(true);

        PlayerData data = null;
        boolean needsDataCreated = !playerDataHandler.hasExistingPlayerData(providedID);
        if (!needsDataCreated) {

            data = playerDataHandler.loadData(providedID);
            // Delayed because data#sendObjectiveHelperMessage does not send a raw message.
            // You can only send raw messages to the player if they are conversing. The player will not be conversing 2 ticks from now.
            if(data.isInTutorial()){
                Bukkit.getScheduler().runTaskLater(plugin, data::sendObjectiveHelperMessage, 2);
            }

        } else {

            boolean wasDataCreated = playerDataHandler.createData(player.getUniqueId(), providedID);
            if (!wasDataCreated) {
                plugin.getLogger().severe("There was an error trying to create player data for " + player.getName());
                player.sendRawMessage("There was an error! Please contact an administrator...");
            } else {
                player.getInventory().clear();
                data = playerDataHandler.getData(player);
            }

        }

        player.getPersistentDataContainer().remove(IronCraftPlugin.IN_SURVEY_PDC_KEY);
        if(data == null){
            return END_OF_CONVERSATION;
        }

        boolean isManager = /*(numOnlinePlayers != 1 && ThreadLocalRandom.current().nextBoolean()) ||*/ providedID.contains("_manager");
        data.init(plugin, isManager);
        if(needsDataCreated) {

            int numOnlinePlayers = Bukkit.getOnlinePlayers().size();

            if(!isManager){

                Location randomChamberLocation = plugin.getRandomChamber();
                if (randomChamberLocation == null) {
                    plugin.getLogger().severe("There has been an error trying to teleport a player to a training chamber");
                    player.sendRawMessage("You could not be teleported to a training chamber at this moment. Please contact an administrator...");
                    return null;
                }

                player.teleport(randomChamberLocation);
                data.setChamberLocation(randomChamberLocation);

                player.sendRawMessage("Welcome to the training chamber! This is where you will be taught to mine a diamond!");
                player.sendRawMessage("If you look to the right of your screen, you will see (in order) the objectives you need to complete");
                player.sendRawMessage("If you ever get confused, just look in chat. We will be giving you helpful tips along your training session!");
                player.sendRawMessage("Keep in mind that you are being managed. Your manager will always be watching you!");

            }

        }

        ManagerUtil.ensurePlayersHaveManagers(plugin);

        return END_OF_CONVERSATION;

    }

}

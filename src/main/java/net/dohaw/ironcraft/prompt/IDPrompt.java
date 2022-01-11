package net.dohaw.ironcraft.prompt;

import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.manager.ManagementType;
import net.dohaw.ironcraft.manager.ManagerUtil;
import net.dohaw.ironcraft.handler.PlayerDataHandler;
import net.dohaw.ironcraft.playerdata.PlayerData;
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

        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        Player player = (Player) context.getForWhom();

        player.setGravity(true);

        if (playerDataHandler.hasExistingPlayerData(providedID)) {
            PlayerData data = playerDataHandler.loadData(providedID);
            if(!data.isManager()){
                ManagerUtil.assignManager(data);
                // Delayed because data#sendObjectiveHelperMessage does not send a raw message.
                // You can only send raw messages to the player if they are conversing. The player will not be conversing 2 ticks from now.
                Bukkit.getScheduler().runTaskLater(plugin, data::sendObjectiveHelperMessage, 2);
            }else{
                initManager(data);
            }
        } else {

            boolean wasDataCreated = playerDataHandler.createData(player.getUniqueId(), providedID);
            if (!wasDataCreated) {
                plugin.getLogger().severe("There was an error trying to create player data for " + player.getName());
                player.sendRawMessage("There was an error! Please contact an administrator...");
            } else {

                PlayerData data = playerDataHandler.getData(player);
                int numOnlinePlayers = Bukkit.getOnlinePlayers().size();
                boolean isManager = numOnlinePlayers == 1 ? false : ThreadLocalRandom.current().nextBoolean();
                if(isManager){
                    initManager(data);
                    // Should we switch certain players from AI managers to Human managers?
                }else{

                    Location randomChamberLocation = plugin.getRandomChamber();
                    if (randomChamberLocation == null) {
                        plugin.getLogger().severe("There has been an error trying to teleport a player to a training chamber");
                        player.sendRawMessage("You could not be teleported to a training chamber at this moment. Please contact an administrator...");
                        return null;
                    }

                    ManagerUtil.assignManager(data);

                    player.getInventory().clear();
                    plugin.giveEssentialItems(player);
                    player.teleport(randomChamberLocation);
                    data.setChamberLocation(randomChamberLocation);

                    player.sendRawMessage("Welcome to the training chamber! This is where you will be taught to mine a diamond!");
                    player.sendRawMessage("If you look to the right of your screen, you will see (in order) the objectives you need to complete");
                    player.sendRawMessage("If you ever get confused, just look in chat. We will be giving you helpful tips along your training session!");
                    player.sendRawMessage("Keep in mind that you are being managed. Your manager will always be watching you!");

                }

            }

        }

        plugin.updateScoreboard(player);

        return null;

    }

    private void sendManagerMessage(Player player){
        player.sendRawMessage("You are a manager in the iron pickaxe factory. You will supervise 2 to 5 workers, who should make an iron pickaxe within 7 mins.");
        player.sendRawMessage("As a manager, your task is to keep an eye on their performance and evaluate them after the 7-min session expires. You can rate each of them on three levels: Beginner level ($0), Intermediate level ($0.2), or Advanced level ($0.5). Your ratings will decide their pay in the session as shown in the brackets.");
        player.sendRawMessage("Left click to switch your focus between players that you manage.");
    }

    private void initManager(PlayerData data){
        Player player = data.getPlayer();
        data.setIsManager(true);
        player.setGravity(false);
        player.setInvisible(true);
        player.setAllowFlight(true);
        player.setFlying(true);
        data.startTeleporter(plugin);
        sendManagerMessage(player);
    }

}

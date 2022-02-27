package net.dohaw.ironcraft.prompt;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.Objective;
import net.dohaw.ironcraft.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class RepeatTutorialPrompt extends StringPrompt {

    private final IronCraftPlugin plugin;

    public RepeatTutorialPrompt(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return StringUtils.colorString("Would you like to do the tutorial again?\nPress \"T\" and type in &cno &fif you wish to go venture on your own. \nType &ayes &fif you want to do the tutorial again");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {

        Player player = (Player) context.getForWhom();
        PlayerData playerData = plugin.getPlayerDataHandler().getData(player.getUniqueId());

        if (input != null) {
            if (input.equalsIgnoreCase("yes")) {

                Location randomChamberLocation = plugin.getRandomChamber();
                if (randomChamberLocation == null) {
                    plugin.getLogger().severe("There has been an error trying to teleport a player to a training chamber");
                    player.sendRawMessage("You could not be teleported to a training chamber at this moment. Please contact an administrator...");
                    return null;
                }

                player.sendRawMessage("Very well then! You will be teleported to a new training chamber shortly. Good luck!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getInventory().clear();
                    playerData.setCurrentTutorialObjective(Objective.MOVE);
                    player.teleport(randomChamberLocation);
                }, 20 * 3);

            } else if (input.equalsIgnoreCase("no")) {

                player.sendRawMessage("Looks like you want to start your journey. You will be teleported shortly. Good luck!");

                Location randomSpawnPoint = plugin.getRandomJourneySpawnPoint();
                if (randomSpawnPoint == null) {
                    plugin.getLogger().severe("There has been an error trying to teleport a player to a random spawn point");
                    player.sendRawMessage("You could not be teleported to a random spawn point at this moment. Please contact an administrator...");
                    return null;
                }

                playerData.setCurrentTutorialObjective(Objective.COLLECT_WOOD);
                playerData.setInTutorial(false);
                player.getInventory().clear();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(randomSpawnPoint);
                    playerData.initWorker(plugin);
                }, 20L * 3);

            }

        }

        return END_OF_CONVERSATION;
    }
}

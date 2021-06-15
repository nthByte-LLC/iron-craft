package net.dohaw.diamondcraft;

import net.dohaw.corelib.StringUtils;
import net.dohaw.diamondcraft.playerdata.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * If the player is in the tutorial, they will periodically get useful tips. If the player is not in a tutorial, they will be reminded that their manager is watching them
 */
public class Reminder extends BukkitRunnable {

    private DiamondCraftPlugin plugin;

    private int currentIndexReminder = 0;
    private List<String> reminders = new ArrayList<>();

    public Reminder(DiamondCraftPlugin plugin){
        this.plugin = plugin;
        compileReminders();
    }

    @Override
    public void run() {

        if(currentIndexReminder >= reminders.size()){
            currentIndexReminder = 0;
        }

        for(Player player : Bukkit.getOnlinePlayers()){

            PlayerData data = plugin.getPlayerDataHandler().getData(player.getUniqueId());
            String message = null;
            if(data != null && data.isInTutorial() && !player.isConversing()){
                message = "&b" + reminders.get(currentIndexReminder);
            }else if(data != null && !data.isInTutorial()){
                message = "&cYour manager is watching you!";
            }

            if(message != null){
                message = StringUtils.colorString(message);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }

        }

        currentIndexReminder++;

    }

    private void compileReminders(){
        reminders.add("Press T to open chat so that you can read messages!");
        reminders.add("Use your number keys to switch between items!");
        reminders.add("Press E to open your inventory!");
        reminders.add("Right-Click with the Paper in your hand to see recipes!");
    }

}

package net.dohaw.ironcraft;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.playerdata.PlayerData;
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

    private final IronCraftPlugin plugin;

    private int currentIndexReminder = 0;
    private final List<String> REMINDERS = new ArrayList<String>(){{
        add("Press T to open chat so that you can read messages!");
        add("Use your number keys to switch between items!");
        add("Press E to open your inventory!");
        add("Right-Click with the Paper in your hand to see recipes!");
    }};

    public Reminder(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        if (currentIndexReminder >= REMINDERS.size()) {
            currentIndexReminder = 0;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            PlayerData data = plugin.getPlayerDataHandler().getData(player.getUniqueId());
            String message = null;
            if (data != null && data.isInTutorial() && !player.isConversing()) {
                message = "&b" + REMINDERS.get(currentIndexReminder);
            } else if (data != null && !data.isInTutorial()) {
                message = "&cYour manager is watching you!";
            }

            if (message != null) {
                message = StringUtils.colorString(message);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }

        }

        currentIndexReminder++;

    }

}

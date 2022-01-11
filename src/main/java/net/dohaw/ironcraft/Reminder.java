package net.dohaw.ironcraft;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.playerdata.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * If the player is in the tutorial, they will periodically get useful tips. If the player is not in a tutorial, they will be reminded that their manager is watching them
 */
public class Reminder extends BukkitRunnable {

    private final IronCraftPlugin plugin;

    private Map<UUID, Integer> playerIndexReminders = new HashMap<>();

    private final List<String> PLAYER_REMINDERS = new ArrayList<String>() {{
        add("&bPress T to open chat so that you can read messages!");
        add("&bUse your number keys to switch between items!");
        add("&bPress E to open your inventory!");
    }};

    private final List<String> MANAGER_REMINDERS = new ArrayList<String>(){{
        add("Left-Click to switch between the players you are managing.");
    }};

    public Reminder(IronCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        for (PlayerData data : plugin.getPlayerDataHandler().getPlayerDataList()) {

            List<String> reminders = data.isManager() ? MANAGER_REMINDERS : PLAYER_REMINDERS;
            UUID uuid = data.getUuid();
            int currentIndexReminder = playerIndexReminders.getOrDefault(uuid, 0);
            if (currentIndexReminder >= reminders.size()) {
                currentIndexReminder = 0;
            }else{
                currentIndexReminder++;
            }

            Player player = data.getPlayer();
            if(player.isConversing() || data.isManager()) continue;

            String message = data.isInTutorial() ? StringUtils.colorString(reminders.get(currentIndexReminder)) : StringUtils.colorString("&cYour manager is watching you!");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

            playerIndexReminders.put(uuid, currentIndexReminder);

        }

    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        playerIndexReminders.clear();
    }

}

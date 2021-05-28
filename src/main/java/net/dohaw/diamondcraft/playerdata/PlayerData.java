package net.dohaw.diamondcraft.playerdata;

import net.dohaw.corelib.StringUtils;
import net.dohaw.diamondcraft.DiamondCraftPlugin;
import net.dohaw.diamondcraft.TutorialObjective;
import net.dohaw.diamondcraft.config.PlayerDataConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class PlayerData {

    private boolean isManager;

    private UUID uuid;

    private String providedID;

    private PlayerDataConfig playerDataConfig;

    private boolean isInTutorial;

    private Location chamberLocation;

    private TutorialObjective currentTutorialObjective;

    private BukkitTask objectiveReminder;

    public PlayerData(UUID uuid, String providedID){
        this.providedID = providedID;
        this.uuid = uuid;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setIsManager(boolean manager) {
        isManager = manager;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getProvidedID() {
        return providedID;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(uuid);
    }

    public void setPlayerDataConfig(PlayerDataConfig playerDataConfig) {
        this.playerDataConfig = playerDataConfig;
    }

    public void saveData(){
        playerDataConfig.saveData(this);
    }

    public boolean isInTutorial() {
        return isInTutorial;
    }

    public void setInTutorial(boolean inTutorial) {
        isInTutorial = inTutorial;
    }

    public Location getChamberLocation() {
        return chamberLocation;
    }

    public void setChamberLocation(Location chamberLocation) {
        this.chamberLocation = chamberLocation;
    }

    public TutorialObjective getCurrentTutorialObjective() {
        return currentTutorialObjective;
    }

    public void setCurrentTutorialObjective(JavaPlugin plugin, TutorialObjective currentTutorialObjective) {
        this.currentTutorialObjective = currentTutorialObjective;
        if(objectiveReminder != null){
            objectiveReminder.cancel();
        }
        this.objectiveReminder = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String helperMessage = currentTutorialObjective.getHelperMessage();
            getPlayer().sendMessage(StringUtils.colorString("&a&l[Objective Tip] &f" + helperMessage));
        }, 0L, 1200L);
    }

}

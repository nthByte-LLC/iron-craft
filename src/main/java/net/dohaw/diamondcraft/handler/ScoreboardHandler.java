package net.dohaw.diamondcraft.handler;

import net.dohaw.diamondcraft.DiamondCraftPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ScoreboardHandler {

    private final DiamondCraftPlugin plugin;

    private BukkitTask scoreboardUpdater;

    public ScoreboardHandler(DiamondCraftPlugin plugin) {
        this.plugin = plugin;
    }


}

package net.dohaw.diamondcraft;

import net.dohaw.diamondcraft.playerdata.PlayerData;
import net.dohaw.diamondcraft.playerdata.PlayerDataHandler;
import net.dohaw.diamondcraft.prompt.IDPrompt;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.List;

public class PlayerWatcher implements Listener {

    private final List<Material> BREAKABLE_TUTORIAL_BLOCKS = Arrays.asList(Material.STONE, Material.OAK_LOG,
        Material.OAK_LEAVES, Material.IRON_ORE, Material.DIAMOND_ORE
    );

    private DiamondCraftPlugin plugin;

    public PlayerWatcher(DiamondCraftPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        Player player = e.getPlayer();

        ConversationFactory conversationFactory = new ConversationFactory(plugin);
        Conversation conversation = conversationFactory.withFirstPrompt(new IDPrompt(plugin)).withLocalEcho(false).buildConversation(player);
        conversation.begin();

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();
        plugin.getPlayerDataHandler().saveData(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){

        Player player = e.getPlayer();
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
        if(!playerDataHandler.hasDataLoaded(player.getUniqueId())){
            e.setCancelled(true);
            return;
        }

        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
        if(playerData.isInTutorial() && !BREAKABLE_TUTORIAL_BLOCKS.contains(e.getBlock().getType())){
            e.setCancelled(true);
            player.sendMessage("You can't break that block. Focus on the objectives!");
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if(!plugin.getPlayerDataHandler().hasDataLoaded(e.getPlayer().getUniqueId()) && hasMoved(e.getTo(), e.getFrom(), true)){
            e.setCancelled(true);
        }
    }

    public boolean hasMoved(Location to, Location from, boolean checkY){
        if(to != null){
            boolean hasMovedHorizontally = from.getX() != to.getX() || from.getZ() != to.getZ();
            if(!hasMovedHorizontally && checkY){
                return from.getY() != to.getY();
            }
            return hasMovedHorizontally;
        }
        return false;
    }

}

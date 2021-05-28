package net.dohaw.diamondcraft.listener;

import net.dohaw.corelib.StringUtils;
import net.dohaw.diamondcraft.DiamondCraftPlugin;
import net.dohaw.diamondcraft.playerdata.PlayerData;
import net.dohaw.diamondcraft.handler.PlayerDataHandler;
import net.dohaw.diamondcraft.prompt.IDPrompt;
import net.dohaw.diamondcraft.prompt.autonomysurvey.AutonomySurveyPrompt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class PlayerWatcher implements Listener {

    private final List<Material> BREAKABLE_TUTORIAL_BLOCKS = Arrays.asList(Material.STONE, Material.OAK_LOG,
        Material.OAK_LEAVES, Material.IRON_ORE, Material.DIAMOND_ORE, Material.GRASS_BLOCK, Material.DIRT, Material.CRAFTING_TABLE, Material.FURNACE
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

    /*
        Doesn't let them move if they don't have player data loaded.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if(!plugin.getPlayerDataHandler().hasDataLoaded(e.getPlayer().getUniqueId()) && hasMoved(e.getTo(), e.getFrom(), true)){
            e.setCancelled(true);
        }
    }

    // Doesn't let them move if they haven't answered the autonomy survey
    @EventHandler
    public void onPlayerMoveDuringSurvey(PlayerMoveEvent e){
        PersistentDataContainer pdc = e.getPlayer().getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.minecraft("is-answering-survey");
        if(pdc.has(key, PersistentDataType.STRING) && hasMoved(e.getTo(), e.getFrom(), true)){
            e.setCancelled(true);
        }
    }

    /*
        Endgame
     */
    @EventHandler
    public void onPlayerMineDiamond(BlockBreakEvent e){

        Player player = e.getPlayer();
        Block blockMined = e.getBlock();
        if(blockMined.getType() == Material.DIAMOND_ORE){
            PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
            PlayerData playerData = playerDataHandler.getData(player.getUniqueId());
            if(!playerData.isInTutorial()){
                player.sendMessage(StringUtils.colorString("&aCongratulations! &fYou have successfully mined a diamond!"));
                player.sendMessage("You will now take a survey. You won't be able to move for the duration of this survey. Don't worry, it'll be quick!");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getPersistentDataContainer().set(NamespacedKey.minecraft("is-answering-survey"), PersistentDataType.STRING, "marker");
                    Conversation conv = new ConversationFactory(plugin).withFirstPrompt(new AutonomySurveyPrompt(0, playerDataHandler)).withLocalEcho(false).buildConversation(player);
                    conv.begin();
                }, 20L * 3);
            }
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

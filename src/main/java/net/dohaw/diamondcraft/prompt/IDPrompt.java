package net.dohaw.diamondcraft.prompt;

import net.dohaw.corelib.StringUtils;
import net.dohaw.corelib.helpers.ItemStackHelper;
import net.dohaw.diamondcraft.DiamondCraftPlugin;
import net.dohaw.diamondcraft.handler.PlayerDataHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class IDPrompt extends StringPrompt {

    private DiamondCraftPlugin plugin;

    public IDPrompt(DiamondCraftPlugin plugin){
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

        if(playerDataHandler.hasExistingPlayerData(providedID)){
            playerDataHandler.loadData(providedID);
        }else{
            boolean wasDataCreated = playerDataHandler.createData(player.getUniqueId(), providedID);
            if(!wasDataCreated){
                plugin.getLogger().severe("There was an error trying to create player data for " + player.getName());
                player.sendRawMessage("There was an error! Please contact an administrator...");
            }else{

                Location randomChamberLocation = plugin.getRandomChamber();
                if(randomChamberLocation == null){
                    plugin.getLogger().severe("There has been an error trying to teleport a player to a training chamber");
                    player.sendRawMessage("You could not be teleported to a training chamber at this moment. Please contact an administrator...");
                    return null;
                }

                player.teleport(randomChamberLocation);
                playerDataHandler.getData(player.getUniqueId()).setChamberLocation(randomChamberLocation);
                giveEssentialItems(player);

                player.sendRawMessage("Welcome to the training chamber! This is where you will be taught to mine a diamond!");
                player.sendRawMessage("If you look to the right of your screen, you will see (in order) the objectives you need to complete");
                player.sendRawMessage("If you ever get confused, just look in chat. We will be giving you helpful tips along your training session!");

            }
        }

        plugin.updateScoreboard(player);

        return null;

    }

    private void giveEssentialItems(Player player){
        PlayerInventory inv = player.getInventory();
        inv.addItem(createMenuPaper());
        inv.addItem(new ItemStack(Material.TORCH, 64));
    }

    private ItemStack createMenuPaper(){

        ItemStack menuPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = menuPaper.getItemMeta();
        meta.setDisplayName(StringUtils.colorString("&b&lRecipe Menu"));

        List<String> lore = Arrays.asList(ChatColor.RED + "Right-click with me in hand to see the recipe menu!");
        meta.setLore(lore);
        menuPaper.setItemMeta(meta);

        ItemStackHelper.addGlowToItem(menuPaper);
        return menuPaper;
    }

}

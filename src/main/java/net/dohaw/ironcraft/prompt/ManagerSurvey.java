package net.dohaw.ironcraft.prompt;

import net.dohaw.ironcraft.PlayerData;
import org.apache.commons.lang.WordUtils;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import java.util.Arrays;
import java.util.List;

public class ManagerSurvey extends StringPrompt {

    private final List<String> VALID_REPLIES = Arrays.asList("Beginner", "Intermediate", "Advanced");

    private PlayerData managerData;
    private PlayerData userData;

    public ManagerSurvey(PlayerData managerData, PlayerData userData){
        this.managerData = managerData;
        this.userData = userData;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "Please rate the user's proficiency (" + userData.getPlayer().getName() + "). \n \nValid Responses: Beginner, Intermediate, Advanced";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Conversable who = context.getForWhom();
        String properInput = WordUtils.capitalize(input);
        if(!VALID_REPLIES.contains(properInput)){
            who.sendRawMessage("This isn't a valid response! Please answer with Beginner, Intermediate, or Advanced");
            return this;
        }
        who.sendRawMessage("Thank you for rating the user!");
        userData.getPlayer().sendMessage("Your manager has given you a rating of " + properInput);
        return END_OF_CONVERSATION;
    }

}

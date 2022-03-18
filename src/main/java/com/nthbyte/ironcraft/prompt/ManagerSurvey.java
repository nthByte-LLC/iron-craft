package com.nthbyte.ironcraft.prompt;

import net.dohaw.corelib.StringUtils;
import net.dohaw.corelib.helpers.MathHelper;
import com.nthbyte.ironcraft.PlayerData;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ManagerSurvey extends StringPrompt {

    private final List<String> VALID_REPLIES = Arrays.asList("[1] Beginner", "[2] Intermediate", "[3] Advanced");

    private PlayerData managedUserData;

    public ManagerSurvey(PlayerData managedUserData){
        this.managedUserData = managedUserData;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return StringUtils.colorString("Please rate the user's proficiency (" + managedUserData.getPlayer().getName() + "). \n \nValid Responses:\n&e[1] &7Beginner \n&e[2] &7Intermediate \n&e[3] &7Advanced\n \n&7(Enter a number)");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {

        Conversable who = context.getForWhom();
        if(!isValidAnswer(input)){
            who.sendRawMessage(StringUtils.colorString("This is not a valid answer!"));
            return this;
        }

        String answer = VALID_REPLIES.get(Integer.parseInt(input) - 1).split(" ")[1];
        Player managedUser = managedUserData.getPlayer();
        if(managedUser.isConversing()){
            managedUserData.setCachedManagerFeedback(answer);
        }else{
            managedUser.sendRawMessage("Your manager has given you a proficiency level of " + answer);
        }
        who.sendRawMessage("Thank you for rating the user!");

        return END_OF_CONVERSATION;

    }

    private boolean isValidAnswer(String answerGiven) {
        if(!MathHelper.isInt(answerGiven)) return false;
        int num = Integer.parseInt(answerGiven);
        return num > 0 && num <= VALID_REPLIES.size();
    }

}

package net.dohaw.diamondcraft.prompt.autonomysurvey;

import net.dohaw.corelib.StringUtils;
import net.dohaw.diamondcraft.SurveySession;
import net.dohaw.diamondcraft.handler.PlayerDataHandler;
import net.dohaw.diamondcraft.playerdata.PlayerData;
import org.bukkit.NamespacedKey;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AutonomySurveyPrompt extends StringPrompt {

    private final List<String> QUESTIONS = Arrays.asList(
            "Did you enjoy this task?",
            "Did you put a lot of effort into this task?",
            "Did you feel pressured while doing this task?",
            "Did you feel that you have some choice about doing this task?",
            "Did you like your manager?"
    );

    private final List<String> VALID_ANSWERS = Arrays.asList(
            "Not at all",
            "Slightly",
            "Moderately",
            "Very",
            "Extremely"
    );

    private final String question;

    private final PlayerDataHandler playerDataHandler;

    public AutonomySurveyPrompt(int questionIndex, PlayerDataHandler playerDataHandler) {
        this.playerDataHandler = playerDataHandler;
        this.question = QUESTIONS.get(questionIndex);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        String answers = StringUtils.colorString("&r&a");
        for (String answer : VALID_ANSWERS) {
            answers += answer;
            // if it's not the last one
            if (!VALID_ANSWERS.get(VALID_ANSWERS.size() - 1).equalsIgnoreCase(answer)) {
                answers += "/";
            }
        }
        return StringUtils.colorString("&l" + question + "\n" + answers);
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {

        if (!isValidAnswer(input)) {
            context.getForWhom().sendRawMessage(StringUtils.colorString("&cThat was not a valid answer"));
            return this;
        }

        Player player = (Player) context.getForWhom();
        PlayerData playerData = playerDataHandler.getData(player.getUniqueId());

        SurveySession session = playerData.getSurveySession();
        if (session == null) {
            session = new SurveySession();
            playerData.setSurveySession(session);
        }

        session.addEntry(question, input);
        session.increaseNumQuestion();

        if (session.getCurrentNumQuestion() == QUESTIONS.size()) {
            player.sendRawMessage("You have completed the survey. Thanks for playing!");
            player.getPersistentDataContainer().remove(NamespacedKey.minecraft("is-answering-survey"));
            playerData.setSurveySession(null);
            player.getInventory().clear();
            return null;
        }

        return new AutonomySurveyPrompt(session.getCurrentNumQuestion(), playerDataHandler);
    }

    private boolean isValidAnswer(String answerGiven) {
        for (String answer : VALID_ANSWERS) {
            if (answer.equalsIgnoreCase(answerGiven)) {
                return true;
            }
        }
        return false;
    }

}

package net.dohaw.ironcraft.prompt;

import net.dohaw.corelib.StringUtils;
import net.dohaw.ironcraft.IronCraftPlugin;
import net.dohaw.ironcraft.SurveySession;
import net.dohaw.ironcraft.handler.PlayerDataHandler;
import net.dohaw.ironcraft.manager.ManagementType;
import net.dohaw.ironcraft.playerdata.PlayerData;
import org.bukkit.NamespacedKey;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AutonomySurveyPrompt extends StringPrompt {

    private final List<String> QUESTIONS = Arrays.asList(
        "Did you enjoy this task?",
        "Did you put a lot of effort into this task?",
        "Did you feel pressured while doing this task?",
        "Did you feel that you have some choice about doing this task?"
    );

    private final List<String> VALID_ANSWERS = Arrays.asList(
        "Not at all",
        "Slightly",
        "Moderately",
        "Very",
        "Extremely"
    );

    private String question;
    private IronCraftPlugin plugin;

    public AutonomySurveyPrompt(int questionIndex, IronCraftPlugin plugin) {
        this.plugin = plugin;
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
        PlayerDataHandler playerDataHandler = plugin.getPlayerDataHandler();
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

            runAlgorithm();

            if(playerData.getManagementType() == ManagementType.HUMAN){
                // Starts the survey with the manager.
                UUID managerUUID = playerData.getManager();
                PlayerData managerData = playerDataHandler.getData(managerUUID);
                Conversation conv = new ConversationFactory(plugin).withFirstPrompt(new ManagerSurvey(managerData, playerData)).withLocalEcho(false).buildConversation(managerData.getPlayer());
                conv.begin();
            }

            return null;
        }

        return new AutonomySurveyPrompt(session.getCurrentNumQuestion(), plugin);

    }

    private boolean isValidAnswer(String answerGiven) {
        for (String answer : VALID_ANSWERS) {
            if (answer.equalsIgnoreCase(answerGiven)) {
                return true;
            }
        }
        return false;
    }

    private void runAlgorithm(){

        String s;
        try {
            Process process = Runtime.getRuntime().exec("python D:\\Max Planck\\IronCraft\\Documentation\\final version to developer\\classifier.py");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while((s = in.readLine()) != null){
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

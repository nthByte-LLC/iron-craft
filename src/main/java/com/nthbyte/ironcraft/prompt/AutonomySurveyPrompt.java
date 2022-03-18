package com.nthbyte.ironcraft.prompt;

import com.nthbyte.ironcraft.IronCraftPlugin;
import com.nthbyte.ironcraft.SurveySession;
import net.dohaw.corelib.StringUtils;
import net.dohaw.corelib.helpers.MathHelper;
import com.nthbyte.ironcraft.Objective;
import com.nthbyte.ironcraft.handler.PlayerDataHandler;
import com.nthbyte.ironcraft.PlayerData;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AutonomySurveyPrompt extends StringPrompt {

    private final List<String> QUESTIONS = Arrays.asList(
        "1.) Did you enjoy this task?",
        "2.) Did you put a lot of effort into this task?",
        "3.) Did you feel pressured while doing this task?",
        "4.) Did you feel that you have some choice about doing this task?"
    );

    private final List<String> VALID_ANSWERS = Arrays.asList(
        "&e[1]&7 Not at all",
        "&e[2]&7 Slightly",
        "&e[3]&7 Moderately",
        "&e[4]&7 Very",
        "&e[5]&7 Extremely"
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
                answers += "\n";
            }
        }
        return StringUtils.colorString("\n&l" + question + "\n \n" + answers + "\n \nEnter a number");
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

            String managerFeedback = playerData.getManagerFeedback();
            if(managerFeedback != null){
                player.sendRawMessage("Your manager has given you a proficiency level of " + managerFeedback);
            }else{
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    String currentManagerFeedback = playerData.getManagerFeedback();
                    if(currentManagerFeedback == null || !player.isOnline()){
                        return;
                    }
                    player.sendRawMessage("Your manager has given you a proficiency level of " + currentManagerFeedback);
                }, 10 * 20L);
            }

            // Write their data to file and calculate their proficiency score.
            try {
                playerData.writeDataToFile(plugin);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            calculateProficiencyScore(player);

            int roundsPlayed = playerData.getRoundsPlayed();
            System.out.println("Rounds played: " + ++roundsPlayed);
            if(roundsPlayed < 3){

                player.sendRawMessage("You have played " + roundsPlayed + " rounds. You have " + (3 - roundsPlayed) + " more round(s) to go!");
                Location randomSpawnPoint = plugin.getRandomJourneySpawnPoint();
                if (randomSpawnPoint == null) {
                    plugin.getLogger().severe("There has been an error trying to teleport a player to a random spawn point");
                    player.sendRawMessage("You could not be teleported to a random spawn point at this moment. Please contact an administrator...");
                    return END_OF_CONVERSATION;
                }

                // Lets them play the game again.
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getInventory().clear();
                    playerData.setCurrentTutorialObjective(Objective.COLLECT_WOOD);
                    player.teleport(randomSpawnPoint);
                    playerData.setMinutesInGame(0);
                    playerData.initWorker(plugin);
                }, 20 * 3);

            }else{
                player.sendRawMessage("Congratulations. You are finished.");
                playerData.setRoundsPlayed(0);
                playerData.setCurrentTutorialObjective(null);
            }

            return END_OF_CONVERSATION;

        }

        player.sendRawMessage("Your answer: [" + input.trim() + "]");
        return new AutonomySurveyPrompt(session.getCurrentNumQuestion(), plugin);

    }

    private boolean isValidAnswer(String answerGiven) {
        if(!MathHelper.isInt(answerGiven)) return false;
        int num = Integer.parseInt(answerGiven);
        return num > 0 && num <= VALID_ANSWERS.size();
    }

    /**
     * Calculates the player's proficiency score by feeding data to a Python algorithm.
     * @return
     */
    private int calculateProficiencyScore(Player player){

        UUID playerUUID = player.getUniqueId();
        String inputFilePath = plugin.getDataFolder() + "\\end_game_data\\input_" + playerUUID.toString() + ".yml";
        String pythonFilePath = plugin.getDataFolder() + "\\end_game_data\\algo\\" + "classifier.py";
        String line = "py " + pythonFilePath + " " + inputFilePath;

        CommandLine cmdLine = CommandLine.parse(line);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;

    }

}

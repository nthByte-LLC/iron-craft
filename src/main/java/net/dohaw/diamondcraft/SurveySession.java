package net.dohaw.diamondcraft;

import java.util.HashMap;
import java.util.Map;

public class SurveySession {

    private int currentNumQuestion = 0;

    private Map<String, String> questionsAndAnswers = new HashMap<>();

    public SurveySession(){}

    public void addEntry(String question, String answer){
        this.questionsAndAnswers.put(question, answer);
    }

    public int getCurrentNumQuestion() {
        return currentNumQuestion;
    }

    public void increaseNumQuestion(){
        this.currentNumQuestion++;
    }

}
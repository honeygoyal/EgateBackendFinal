package com.egatetutor.backend.model.responsemodel;

public class QuestionAnalysis {
    private long questionId;
    private String yourTime;
    private String averageTime;
    private String topperTime;
    private int totalAttempt;
    private int unAttempt;
    private int inCorrectAttempt;
    private int correctAttempt;
    private String difficultyLevel;

    public String getYourTime() {
        return yourTime;
    }

    public void setYourTime(String yourTime) {
        this.yourTime = yourTime;
    }

    public String getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(String averageTime) {
        this.averageTime = averageTime;
    }

    public String getTopperTime() {
        return topperTime;
    }

    public void setTopperTime(String topperTime) {
        this.topperTime = topperTime;
    }

    public int getTotalAttempt() {
        return totalAttempt;
    }

    public void setTotalAttempt(int totalAttempt) {
        this.totalAttempt = totalAttempt;
    }

    public int getUnAttempt() {
        return unAttempt;
    }

    public void setUnAttempt(int unAttempt) {
        this.unAttempt = unAttempt;
    }

    public int getInCorrectAttempt() {
        return inCorrectAttempt;
    }

    public void setInCorrectAttempt(int inCorrectAttempt) {
        this.inCorrectAttempt = inCorrectAttempt;
    }

    public int getCorrectAttempt() {
        return correctAttempt;
    }

    public void setCorrectAttempt(int correctAttempt) {
        this.correctAttempt = correctAttempt;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }
}

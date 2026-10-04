package com.fieldops.assignment;

public class AssignmentFactor {

    private String factorName;
    private int score;
    private int maxScore;
    private String explanation;

    public AssignmentFactor() {}

    public AssignmentFactor(String factorName, int score, int maxScore, String explanation) {
        this.factorName = factorName;
        this.score = score;
        this.maxScore = maxScore;
        this.explanation = explanation;
    }

    public String getFactorName() { return factorName; }
    public void setFactorName(String factorName) { this.factorName = factorName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}

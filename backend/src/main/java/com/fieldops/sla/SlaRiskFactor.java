package com.fieldops.sla;

public class SlaRiskFactor {

    private String factorName;
    private int impactScore;
    private String explanation;

    public SlaRiskFactor() {}

    public SlaRiskFactor(String factorName, int impactScore, String explanation) {
        this.factorName = factorName;
        this.impactScore = impactScore;
        this.explanation = explanation;
    }

    public String getFactorName() { return factorName; }
    public void setFactorName(String factorName) { this.factorName = factorName; }

    public int getImpactScore() { return impactScore; }
    public void setImpactScore(int impactScore) { this.impactScore = impactScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}

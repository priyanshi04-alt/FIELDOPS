package com.fieldops.sla;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlaRiskResult {

    private Long workOrderId;
    private String workOrderNumber;
    private int riskScore; // 0 - 100
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Long remainingSlaMinutes;
    private Integer estimatedArrivalMinutes;
    private Double technicianDistanceKm;
    private String technicianName;
    private String summaryRecommendation;
    private LocalDateTime evaluationTimestamp = LocalDateTime.now();
    private List<SlaRiskFactor> contributingFactors = new ArrayList<>();

    public SlaRiskResult() {}

    public SlaRiskResult(Long workOrderId, String workOrderNumber, int riskScore, String riskLevel,
                         Long remainingSlaMinutes, Integer estimatedArrivalMinutes, Double technicianDistanceKm,
                         String technicianName, String summaryRecommendation, List<SlaRiskFactor> contributingFactors) {
        this.workOrderId = workOrderId;
        this.workOrderNumber = workOrderNumber;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.remainingSlaMinutes = remainingSlaMinutes;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
        this.technicianDistanceKm = technicianDistanceKm;
        this.technicianName = technicianName;
        this.summaryRecommendation = summaryRecommendation;
        this.contributingFactors = contributingFactors != null ? contributingFactors : new ArrayList<>();
    }

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }

    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Long getRemainingSlaMinutes() { return remainingSlaMinutes; }
    public void setRemainingSlaMinutes(Long remainingSlaMinutes) { this.remainingSlaMinutes = remainingSlaMinutes; }

    public Integer getEstimatedArrivalMinutes() { return estimatedArrivalMinutes; }
    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) { this.estimatedArrivalMinutes = estimatedArrivalMinutes; }

    public Double getTechnicianDistanceKm() { return technicianDistanceKm; }
    public void setTechnicianDistanceKm(Double technicianDistanceKm) { this.technicianDistanceKm = technicianDistanceKm; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public String getSummaryRecommendation() { return summaryRecommendation; }
    public void setSummaryRecommendation(String summaryRecommendation) { this.summaryRecommendation = summaryRecommendation; }

    public LocalDateTime getEvaluationTimestamp() { return evaluationTimestamp; }
    public void setEvaluationTimestamp(LocalDateTime evaluationTimestamp) { this.evaluationTimestamp = evaluationTimestamp; }

    public List<SlaRiskFactor> getContributingFactors() { return contributingFactors; }
    public void setContributingFactors(List<SlaRiskFactor> contributingFactors) { this.contributingFactors = contributingFactors; }
}

package com.fieldops.predictive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PredictiveSlaResultDTO {

    private Long workOrderId;
    private String workOrderNumber;
    private String title;
    private String priority;

    // 1. Authoritative Deterministic Baseline (Phase 2)
    private int deterministicRiskScore; // 0 - 100
    private String deterministicRiskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    // 2. Machine Learning Predictive Classifier (Phase 4)
    private double mlBreachProbability; // 0.0 - 1.0 (0 - 100%)
    private String mlRiskAssessment; // ON_TRACK, AT_RISK, LIKELY_BREACH
    private double confidenceScore;

    // 3. Risk Fusion (Phase 4 Governance)
    private int fusedRiskScore; // 0 - 100
    private String finalAssessment; // ON_TRACK, AT_RISK, LIKELY_BREACH

    private Long remainingSlaMinutes;
    private Integer estimatedArrivalMinutes;
    private Integer estimatedServiceDurationMinutes;
    private Double technicianDistanceKm;
    private String technicianName;
    private Integer technicianBacklog;

    private List<String> topContributingFactors = new ArrayList<>();
    private String recommendedAction;
    private String explanation;

    private String modelVersion;
    private String algorithm;
    private String syntheticDisclaimer;
    private LocalDateTime generatedAt = LocalDateTime.now();

    public PredictiveSlaResultDTO() {}

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }

    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getDeterministicRiskScore() { return deterministicRiskScore; }
    public void setDeterministicRiskScore(int deterministicRiskScore) { this.deterministicRiskScore = deterministicRiskScore; }

    public String getDeterministicRiskLevel() { return deterministicRiskLevel; }
    public void setDeterministicRiskLevel(String deterministicRiskLevel) { this.deterministicRiskLevel = deterministicRiskLevel; }

    public double getMlBreachProbability() { return mlBreachProbability; }
    public void setMlBreachProbability(double mlBreachProbability) { this.mlBreachProbability = mlBreachProbability; }

    public String getMlRiskAssessment() { return mlRiskAssessment; }
    public void setMlRiskAssessment(String mlRiskAssessment) { this.mlRiskAssessment = mlRiskAssessment; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public int getFusedRiskScore() { return fusedRiskScore; }
    public void setFusedRiskScore(int fusedRiskScore) { this.fusedRiskScore = fusedRiskScore; }

    public String getFinalAssessment() { return finalAssessment; }
    public void setFinalAssessment(String finalAssessment) { this.finalAssessment = finalAssessment; }

    public Long getRemainingSlaMinutes() { return remainingSlaMinutes; }
    public void setRemainingSlaMinutes(Long remainingSlaMinutes) { this.remainingSlaMinutes = remainingSlaMinutes; }

    public Integer getEstimatedArrivalMinutes() { return estimatedArrivalMinutes; }
    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) { this.estimatedArrivalMinutes = estimatedArrivalMinutes; }

    public Integer getEstimatedServiceDurationMinutes() { return estimatedServiceDurationMinutes; }
    public void setEstimatedServiceDurationMinutes(Integer estimatedServiceDurationMinutes) { this.estimatedServiceDurationMinutes = estimatedServiceDurationMinutes; }

    public Double getTechnicianDistanceKm() { return technicianDistanceKm; }
    public void setTechnicianDistanceKm(Double technicianDistanceKm) { this.technicianDistanceKm = technicianDistanceKm; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public Integer getTechnicianBacklog() { return technicianBacklog; }
    public void setTechnicianBacklog(Integer technicianBacklog) { this.technicianBacklog = technicianBacklog; }

    public List<String> getTopContributingFactors() { return topContributingFactors; }
    public void setTopContributingFactors(List<String> topContributingFactors) { this.topContributingFactors = topContributingFactors; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getSyntheticDisclaimer() { return syntheticDisclaimer; }
    public void setSyntheticDisclaimer(String syntheticDisclaimer) { this.syntheticDisclaimer = syntheticDisclaimer; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getExpiresAt() { return generatedAt != null ? generatedAt.plusMinutes(30) : null; }
    public boolean isStale() { return getExpiresAt() != null && LocalDateTime.now().isAfter(getExpiresAt()); }
    public String getPredictionStatus() { return isStale() ? "STALE" : "CURRENT"; }
}

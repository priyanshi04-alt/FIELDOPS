package com.fieldops.predictive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TechnicianPerformanceDTO {

    private Long technicianId;
    private String name;
    private String email;
    private String city;
    private String status;
    private Double userRating;

    // Composite Operational Performance Score (0 - 100)
    private int performanceScore;
    private String performanceTier; // ELITE, HIGH_PERFORMER, STANDARD, REQUIRES_SUPPORT
    private String performanceTrend; // IMPROVING, STABLE, DECLINING

    // Specific Metric Dimensions
    private double slaAdherencePercent;       // 30% weight
    private double firstTimeFixPercent;       // 20% weight
    private int avgCompletionMinutes;         // 20% weight
    private double routeEfficiencyScore;      // 15% weight
    private double workloadBalanceScore;      // 10% weight
    private double reliabilityScore;          // 5% weight

    private int completedJobsCount;
    private int activeJobsCount;
    private double currentUtilizationRate;

    private List<String> operationalStrengths = new ArrayList<>();
    private List<String> improvementAreas = new ArrayList<>();
    private String plainEnglishSummary;

    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public TechnicianPerformanceDTO() {}

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getUserRating() { return userRating; }
    public void setUserRating(Double userRating) { this.userRating = userRating; }

    public int getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(int performanceScore) { this.performanceScore = performanceScore; }

    public String getPerformanceTier() { return performanceTier; }
    public void setPerformanceTier(String performanceTier) { this.performanceTier = performanceTier; }

    public String getPerformanceTrend() { return performanceTrend; }
    public void setPerformanceTrend(String performanceTrend) { this.performanceTrend = performanceTrend; }

    public double getSlaAdherencePercent() { return slaAdherencePercent; }
    public void setSlaAdherencePercent(double slaAdherencePercent) { this.slaAdherencePercent = slaAdherencePercent; }

    public double getFirstTimeFixPercent() { return firstTimeFixPercent; }
    public void setFirstTimeFixPercent(double firstTimeFixPercent) { this.firstTimeFixPercent = firstTimeFixPercent; }

    public int getAvgCompletionMinutes() { return avgCompletionMinutes; }
    public void setAvgCompletionMinutes(int avgCompletionMinutes) { this.avgCompletionMinutes = avgCompletionMinutes; }

    public double getRouteEfficiencyScore() { return routeEfficiencyScore; }
    public void setRouteEfficiencyScore(double routeEfficiencyScore) { this.routeEfficiencyScore = routeEfficiencyScore; }

    public double getWorkloadBalanceScore() { return workloadBalanceScore; }
    public void setWorkloadBalanceScore(double workloadBalanceScore) { this.workloadBalanceScore = workloadBalanceScore; }

    public double getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(double reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public int getCompletedJobsCount() { return completedJobsCount; }
    public void setCompletedJobsCount(int completedJobsCount) { this.completedJobsCount = completedJobsCount; }

    public int getActiveJobsCount() { return activeJobsCount; }
    public void setActiveJobsCount(int activeJobsCount) { this.activeJobsCount = activeJobsCount; }

    public double getCurrentUtilizationRate() { return currentUtilizationRate; }
    public void setCurrentUtilizationRate(double currentUtilizationRate) { this.currentUtilizationRate = currentUtilizationRate; }

    public List<String> getOperationalStrengths() { return operationalStrengths; }
    public void setOperationalStrengths(List<String> operationalStrengths) { this.operationalStrengths = operationalStrengths; }

    public List<String> getImprovementAreas() { return improvementAreas; }
    public void setImprovementAreas(List<String> improvementAreas) { this.improvementAreas = improvementAreas; }

    public String getPlainEnglishSummary() { return plainEnglishSummary; }
    public void setPlainEnglishSummary(String plainEnglishSummary) { this.plainEnglishSummary = plainEnglishSummary; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}

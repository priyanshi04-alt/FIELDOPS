package com.fieldops.simulator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScenarioResult {

    private String simulationId;
    private ScenarioType scenarioType;
    private String scenarioName;

    // Baseline Operational Snapshot (Before)
    private int baselineActiveTechnicians;
    private int baselineAtRiskJobs;
    private int baselineProjectedBreaches;
    private double baselineTotalDistanceKm;
    private int baselineTotalTravelMinutes;
    private int baselineActiveAlerts;

    // Simulated Operational Snapshot (After)
    private int simulatedActiveTechnicians;
    private int simulatedAtRiskJobs;
    private int simulatedProjectedBreaches;
    private double simulatedTotalDistanceKm;
    private int simulatedTotalTravelMinutes;
    private int simulatedActiveAlerts;

    // Impact Deltas
    private int deltaAtRiskJobs;
    private int deltaProjectedBreaches;
    private double deltaDistanceKm;
    private int deltaTravelMinutes;
    private int deltaNewAlerts;
    private int reassignedJobsCount;

    private String operationalRiskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String recommendedAction;
    private List<String> simulatedStateChanges = new ArrayList<>();
    private List<String> projectedWarnings = new ArrayList<>();

    private boolean isApplied = false;
    private LocalDateTime generatedAt = LocalDateTime.now();

    public ScenarioResult() {}

    public String getSimulationId() { return simulationId; }
    public void setSimulationId(String simulationId) { this.simulationId = simulationId; }

    public ScenarioType getScenarioType() { return scenarioType; }
    public void setScenarioType(ScenarioType scenarioType) { this.scenarioType = scenarioType; }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }

    public int getBaselineActiveTechnicians() { return baselineActiveTechnicians; }
    public void setBaselineActiveTechnicians(int baselineActiveTechnicians) { this.baselineActiveTechnicians = baselineActiveTechnicians; }

    public int getBaselineAtRiskJobs() { return baselineAtRiskJobs; }
    public void setBaselineAtRiskJobs(int baselineAtRiskJobs) { this.baselineAtRiskJobs = baselineAtRiskJobs; }

    public int getBaselineProjectedBreaches() { return baselineProjectedBreaches; }
    public void setBaselineProjectedBreaches(int baselineProjectedBreaches) { this.baselineProjectedBreaches = baselineProjectedBreaches; }

    public double getBaselineTotalDistanceKm() { return baselineTotalDistanceKm; }
    public void setBaselineTotalDistanceKm(double baselineTotalDistanceKm) { this.baselineTotalDistanceKm = baselineTotalDistanceKm; }

    public int getBaselineTotalTravelMinutes() { return baselineTotalTravelMinutes; }
    public void setBaselineTotalTravelMinutes(int baselineTotalTravelMinutes) { this.baselineTotalTravelMinutes = baselineTotalTravelMinutes; }

    public int getBaselineActiveAlerts() { return baselineActiveAlerts; }
    public void setBaselineActiveAlerts(int baselineActiveAlerts) { this.baselineActiveAlerts = baselineActiveAlerts; }

    public int getSimulatedActiveTechnicians() { return simulatedActiveTechnicians; }
    public void setSimulatedActiveTechnicians(int simulatedActiveTechnicians) { this.simulatedActiveTechnicians = simulatedActiveTechnicians; }

    public int getSimulatedAtRiskJobs() { return simulatedAtRiskJobs; }
    public void setSimulatedAtRiskJobs(int simulatedAtRiskJobs) { this.simulatedAtRiskJobs = simulatedAtRiskJobs; }

    public int getSimulatedProjectedBreaches() { return simulatedProjectedBreaches; }
    public void setSimulatedProjectedBreaches(int simulatedProjectedBreaches) { this.simulatedProjectedBreaches = simulatedProjectedBreaches; }

    public double getSimulatedTotalDistanceKm() { return simulatedTotalDistanceKm; }
    public void setSimulatedTotalDistanceKm(double simulatedTotalDistanceKm) { this.simulatedTotalDistanceKm = simulatedTotalDistanceKm; }

    public int getSimulatedTotalTravelMinutes() { return simulatedTotalTravelMinutes; }
    public void setSimulatedTotalTravelMinutes(int simulatedTotalTravelMinutes) { this.simulatedTotalTravelMinutes = simulatedTotalTravelMinutes; }

    public int getSimulatedActiveAlerts() { return simulatedActiveAlerts; }
    public void setSimulatedActiveAlerts(int simulatedActiveAlerts) { this.simulatedActiveAlerts = simulatedActiveAlerts; }

    public int getDeltaAtRiskJobs() { return deltaAtRiskJobs; }
    public void setDeltaAtRiskJobs(int deltaAtRiskJobs) { this.deltaAtRiskJobs = deltaAtRiskJobs; }

    public int getDeltaProjectedBreaches() { return deltaProjectedBreaches; }
    public void setDeltaProjectedBreaches(int deltaProjectedBreaches) { this.deltaProjectedBreaches = deltaProjectedBreaches; }

    public double getDeltaDistanceKm() { return deltaDistanceKm; }
    public void setDeltaDistanceKm(double deltaDistanceKm) { this.deltaDistanceKm = deltaDistanceKm; }

    public int getDeltaTravelMinutes() { return deltaTravelMinutes; }
    public void setDeltaTravelMinutes(int deltaTravelMinutes) { this.deltaTravelMinutes = deltaTravelMinutes; }

    public int getDeltaNewAlerts() { return deltaNewAlerts; }
    public void setDeltaNewAlerts(int deltaNewAlerts) { this.deltaNewAlerts = deltaNewAlerts; }

    public int getReassignedJobsCount() { return reassignedJobsCount; }
    public void setReassignedJobsCount(int reassignedJobsCount) { this.reassignedJobsCount = reassignedJobsCount; }

    public String getOperationalRiskLevel() { return operationalRiskLevel; }
    public void setOperationalRiskLevel(String operationalRiskLevel) { this.operationalRiskLevel = operationalRiskLevel; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public List<String> getSimulatedStateChanges() { return simulatedStateChanges; }
    public void setSimulatedStateChanges(List<String> simulatedStateChanges) { this.simulatedStateChanges = simulatedStateChanges; }

    public List<String> getProjectedWarnings() { return projectedWarnings; }
    public void setProjectedWarnings(List<String> projectedWarnings) { this.projectedWarnings = projectedWarnings; }

    public boolean isApplied() { return isApplied; }
    public void setApplied(boolean applied) { isApplied = applied; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}

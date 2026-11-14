package com.fieldops.intelligence;

import com.fieldops.alert.AlertDTO;
import com.fieldops.anomaly.OperationalAnomaly;
import com.fieldops.assignment.AssignmentRecommendation;
import com.fieldops.routing.multistop.OptimizedRoute;
import com.fieldops.routing.multistop.RouteDecisionAudit;
import com.fieldops.sla.SlaRiskResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OperationsIntelligenceOverview {

    private int operationsHealthScore = 96;
    private int activeAnomaliesCount;
    private int criticalSlaCount;
    private int activeAlertsCount;
    // Phase 4 Predictive Intelligence Metrics
    private int predictedBreachesCount;
    private int predictedStockoutsCount;
    private java.util.Map<String, com.fieldops.predictive.ModelRegistryEntry> modelRegistry = new java.util.HashMap<>();
    private List<com.fieldops.predictive.InventoryForecastDTO> topStockoutRisks = new ArrayList<>();
    private List<com.fieldops.predictive.PredictiveSlaResultDTO> topPredictedBreaches = new ArrayList<>();

    private List<SlaRiskResult> highSlaRisks = new ArrayList<>();
    private List<OperationalAnomaly> anomalies = new ArrayList<>();
    private List<AssignmentRecommendation> pendingSmartDispatches = new ArrayList<>();
    private List<AlertDTO> activeAlerts = new ArrayList<>();
    private OptimizedRoute sampleOptimizedRoute;
    private List<RouteDecisionAudit> recentRouteAudits = new ArrayList<>();
    private LocalDateTime generatedAt = LocalDateTime.now();

    public OperationsIntelligenceOverview() {}

    public OperationsIntelligenceOverview(int activeAnomaliesCount, int criticalSlaCount,
                                          List<SlaRiskResult> highSlaRisks, List<OperationalAnomaly> anomalies,
                                          List<AssignmentRecommendation> pendingSmartDispatches) {
        this.activeAnomaliesCount = activeAnomaliesCount;
        this.criticalSlaCount = criticalSlaCount;
        this.highSlaRisks = highSlaRisks != null ? highSlaRisks : new ArrayList<>();
        this.anomalies = anomalies != null ? anomalies : new ArrayList<>();
        this.pendingSmartDispatches = pendingSmartDispatches != null ? pendingSmartDispatches : new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    public int getOperationsHealthScore() { return operationsHealthScore; }
    public void setOperationsHealthScore(int operationsHealthScore) { this.operationsHealthScore = operationsHealthScore; }

    public int getActiveAnomaliesCount() { return activeAnomaliesCount; }
    public void setActiveAnomaliesCount(int activeAnomaliesCount) { this.activeAnomaliesCount = activeAnomaliesCount; }

    public int getCriticalSlaCount() { return criticalSlaCount; }
    public void setCriticalSlaCount(int criticalSlaCount) { this.criticalSlaCount = criticalSlaCount; }

    public int getActiveAlertsCount() { return activeAlertsCount; }
    public void setActiveAlertsCount(int activeAlertsCount) { this.activeAlertsCount = activeAlertsCount; }

    public List<SlaRiskResult> getHighSlaRisks() { return highSlaRisks; }
    public void setHighSlaRisks(List<SlaRiskResult> highSlaRisks) { this.highSlaRisks = highSlaRisks; }

    public List<OperationalAnomaly> getAnomalies() { return anomalies; }
    public void setAnomalies(List<OperationalAnomaly> anomalies) { this.anomalies = anomalies; }

    public List<AssignmentRecommendation> getPendingSmartDispatches() { return pendingSmartDispatches; }
    public void setPendingSmartDispatches(List<AssignmentRecommendation> pendingSmartDispatches) { this.pendingSmartDispatches = pendingSmartDispatches; }

    public List<AlertDTO> getActiveAlerts() { return activeAlerts; }
    public void setActiveAlerts(List<AlertDTO> activeAlerts) { this.activeAlerts = activeAlerts; }

    public OptimizedRoute getSampleOptimizedRoute() { return sampleOptimizedRoute; }
    public void setSampleOptimizedRoute(OptimizedRoute sampleOptimizedRoute) { this.sampleOptimizedRoute = sampleOptimizedRoute; }

    public List<RouteDecisionAudit> getRecentRouteAudits() { return recentRouteAudits; }
    public void setRecentRouteAudits(List<RouteDecisionAudit> recentRouteAudits) { this.recentRouteAudits = recentRouteAudits; }

    public int getPredictedBreachesCount() { return predictedBreachesCount; }
    public void setPredictedBreachesCount(int predictedBreachesCount) { this.predictedBreachesCount = predictedBreachesCount; }

    public int getPredictedStockoutsCount() { return predictedStockoutsCount; }
    public void setPredictedStockoutsCount(int predictedStockoutsCount) { this.predictedStockoutsCount = predictedStockoutsCount; }

    public java.util.Map<String, com.fieldops.predictive.ModelRegistryEntry> getModelRegistry() { return modelRegistry; }
    public void setModelRegistry(java.util.Map<String, com.fieldops.predictive.ModelRegistryEntry> modelRegistry) { this.modelRegistry = modelRegistry; }

    public List<com.fieldops.predictive.InventoryForecastDTO> getTopStockoutRisks() { return topStockoutRisks; }
    public void setTopStockoutRisks(List<com.fieldops.predictive.InventoryForecastDTO> topStockoutRisks) { this.topStockoutRisks = topStockoutRisks; }

    public List<com.fieldops.predictive.PredictiveSlaResultDTO> getTopPredictedBreaches() { return topPredictedBreaches; }
    public void setTopPredictedBreaches(List<com.fieldops.predictive.PredictiveSlaResultDTO> topPredictedBreaches) { this.topPredictedBreaches = topPredictedBreaches; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}

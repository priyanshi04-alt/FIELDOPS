package com.fieldops.intelligence;

import com.fieldops.anomaly.OperationalAnomaly;
import com.fieldops.assignment.AssignmentRecommendation;
import com.fieldops.sla.SlaRiskResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OperationsIntelligenceOverview {

    private int activeAnomaliesCount;
    private int criticalSlaCount;
    private List<SlaRiskResult> highSlaRisks = new ArrayList<>();
    private List<OperationalAnomaly> anomalies = new ArrayList<>();
    private List<AssignmentRecommendation> pendingSmartDispatches = new ArrayList<>();
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

    public int getActiveAnomaliesCount() { return activeAnomaliesCount; }
    public void setActiveAnomaliesCount(int activeAnomaliesCount) { this.activeAnomaliesCount = activeAnomaliesCount; }

    public int getCriticalSlaCount() { return criticalSlaCount; }
    public void setCriticalSlaCount(int criticalSlaCount) { this.criticalSlaCount = criticalSlaCount; }

    public List<SlaRiskResult> getHighSlaRisks() { return highSlaRisks; }
    public void setHighSlaRisks(List<SlaRiskResult> highSlaRisks) { this.highSlaRisks = highSlaRisks; }

    public List<OperationalAnomaly> getAnomalies() { return anomalies; }
    public void setAnomalies(List<OperationalAnomaly> anomalies) { this.anomalies = anomalies; }

    public List<AssignmentRecommendation> getPendingSmartDispatches() { return pendingSmartDispatches; }
    public void setPendingSmartDispatches(List<AssignmentRecommendation> pendingSmartDispatches) { this.pendingSmartDispatches = pendingSmartDispatches; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}

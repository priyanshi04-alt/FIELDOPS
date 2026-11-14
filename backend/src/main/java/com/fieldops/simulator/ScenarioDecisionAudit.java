package com.fieldops.simulator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scenario_decision_audits", indexes = {
        @Index(name = "idx_scenario_type", columnList = "scenarioType"),
        @Index(name = "idx_scenario_dispatcher", columnList = "dispatcherEmail"),
        @Index(name = "idx_scenario_timestamp", columnList = "appliedAt")
})
public class ScenarioDecisionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String simulationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScenarioType scenarioType;

    @Column(nullable = false)
    private String scenarioName;

    @Column(nullable = false)
    private String dispatcherEmail;

    private int reassignedJobsCount;
    private double deltaDistanceKm;
    private int deltaTravelMinutes;
    private int deltaProjectedBreaches;

    @Column(length = 2000)
    private String stateChangesSummary;

    @Column(length = 1000)
    private String rationale;

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    public ScenarioDecisionAudit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSimulationId() { return simulationId; }
    public void setSimulationId(String simulationId) { this.simulationId = simulationId; }
    public ScenarioType getScenarioType() { return scenarioType; }
    public void setScenarioType(ScenarioType scenarioType) { this.scenarioType = scenarioType; }
    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }
    public String getDispatcherEmail() { return dispatcherEmail; }
    public void setDispatcherEmail(String dispatcherEmail) { this.dispatcherEmail = dispatcherEmail; }
    public int getReassignedJobsCount() { return reassignedJobsCount; }
    public void setReassignedJobsCount(int reassignedJobsCount) { this.reassignedJobsCount = reassignedJobsCount; }
    public double getDeltaDistanceKm() { return deltaDistanceKm; }
    public void setDeltaDistanceKm(double deltaDistanceKm) { this.deltaDistanceKm = deltaDistanceKm; }
    public int getDeltaTravelMinutes() { return deltaTravelMinutes; }
    public void setDeltaTravelMinutes(int deltaTravelMinutes) { this.deltaTravelMinutes = deltaTravelMinutes; }
    public int getDeltaProjectedBreaches() { return deltaProjectedBreaches; }
    public void setDeltaProjectedBreaches(int deltaProjectedBreaches) { this.deltaProjectedBreaches = deltaProjectedBreaches; }
    public String getStateChangesSummary() { return stateChangesSummary; }
    public void setStateChangesSummary(String stateChangesSummary) { this.stateChangesSummary = stateChangesSummary; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}

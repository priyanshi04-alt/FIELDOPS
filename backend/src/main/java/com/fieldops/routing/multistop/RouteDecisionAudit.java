package com.fieldops.routing.multistop;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_decision_audits", indexes = {
        @Index(name = "idx_route_audit_tech", columnList = "technicianId"),
        @Index(name = "idx_route_audit_timestamp", columnList = "timestamp")
})
public class RouteDecisionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long technicianId;

    private String technicianName;
    private String dispatcherEmail;
    private Integer jobCount;

    @Column(length = 2000)
    private String previousOrder;

    @Column(length = 2000)
    private String optimizedOrder;

    private String algorithm;
    private Integer optimizationScore;
    private Double totalDistanceKm;
    private Integer totalTravelTimeMinutes;
    private Double distanceSavedKm;
    private Integer timeSavedMinutes;
    private Integer slaAtRiskCount;
    private Integer slaViolationCount;

    private String action; // GENERATED, APPLIED

    @Column(length = 2000)
    private String explanation;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    public RouteDecisionAudit() {}

    public RouteDecisionAudit(Long id, Long technicianId, String technicianName, String dispatcherEmail,
                              Integer jobCount, String previousOrder, String optimizedOrder, String algorithm,
                              Integer optimizationScore, Double totalDistanceKm, Integer totalTravelTimeMinutes,
                              Double distanceSavedKm, Integer timeSavedMinutes, Integer slaAtRiskCount,
                              Integer slaViolationCount, String action, String explanation) {
        this.id = id;
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.dispatcherEmail = dispatcherEmail;
        this.jobCount = jobCount;
        this.previousOrder = previousOrder;
        this.optimizedOrder = optimizedOrder;
        this.algorithm = algorithm;
        this.optimizationScore = optimizationScore;
        this.totalDistanceKm = totalDistanceKm;
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
        this.distanceSavedKm = distanceSavedKm;
        this.timeSavedMinutes = timeSavedMinutes;
        this.slaAtRiskCount = slaAtRiskCount;
        this.slaViolationCount = slaViolationCount;
        this.action = action;
        this.explanation = explanation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long technicianId;
        private String technicianName;
        private String dispatcherEmail;
        private Integer jobCount;
        private String previousOrder;
        private String optimizedOrder;
        private String algorithm;
        private Integer optimizationScore;
        private Double totalDistanceKm;
        private Integer totalTravelTimeMinutes;
        private Double distanceSavedKm;
        private Integer timeSavedMinutes;
        private Integer slaAtRiskCount;
        private Integer slaViolationCount;
        private String action;
        private String explanation;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder technicianId(Long technicianId) { this.technicianId = technicianId; return this; }
        public Builder technicianName(String technicianName) { this.technicianName = technicianName; return this; }
        public Builder dispatcherEmail(String dispatcherEmail) { this.dispatcherEmail = dispatcherEmail; return this; }
        public Builder jobCount(Integer jobCount) { this.jobCount = jobCount; return this; }
        public Builder previousOrder(String previousOrder) { this.previousOrder = previousOrder; return this; }
        public Builder optimizedOrder(String optimizedOrder) { this.optimizedOrder = optimizedOrder; return this; }
        public Builder algorithm(String algorithm) { this.algorithm = algorithm; return this; }
        public Builder optimizationScore(Integer optimizationScore) { this.optimizationScore = optimizationScore; return this; }
        public Builder totalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; return this; }
        public Builder totalTravelTimeMinutes(Integer totalTravelTimeMinutes) { this.totalTravelTimeMinutes = totalTravelTimeMinutes; return this; }
        public Builder distanceSavedKm(Double distanceSavedKm) { this.distanceSavedKm = distanceSavedKm; return this; }
        public Builder timeSavedMinutes(Integer timeSavedMinutes) { this.timeSavedMinutes = timeSavedMinutes; return this; }
        public Builder slaAtRiskCount(Integer slaAtRiskCount) { this.slaAtRiskCount = slaAtRiskCount; return this; }
        public Builder slaViolationCount(Integer slaViolationCount) { this.slaViolationCount = slaViolationCount; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }

        public RouteDecisionAudit build() {
            return new RouteDecisionAudit(id, technicianId, technicianName, dispatcherEmail, jobCount,
                    previousOrder, optimizedOrder, algorithm, optimizationScore, totalDistanceKm,
                    totalTravelTimeMinutes, distanceSavedKm, timeSavedMinutes, slaAtRiskCount,
                    slaViolationCount, action, explanation);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
    public String getDispatcherEmail() { return dispatcherEmail; }
    public void setDispatcherEmail(String dispatcherEmail) { this.dispatcherEmail = dispatcherEmail; }
    public Integer getJobCount() { return jobCount; }
    public void setJobCount(Integer jobCount) { this.jobCount = jobCount; }
    public String getPreviousOrder() { return previousOrder; }
    public void setPreviousOrder(String previousOrder) { this.previousOrder = previousOrder; }
    public String getOptimizedOrder() { return optimizedOrder; }
    public void setOptimizedOrder(String optimizedOrder) { this.optimizedOrder = optimizedOrder; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public Integer getOptimizationScore() { return optimizationScore; }
    public void setOptimizationScore(Integer optimizationScore) { this.optimizationScore = optimizationScore; }
    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    public Integer getTotalTravelTimeMinutes() { return totalTravelTimeMinutes; }
    public void setTotalTravelTimeMinutes(Integer totalTravelTimeMinutes) { this.totalTravelTimeMinutes = totalTravelTimeMinutes; }
    public Double getDistanceSavedKm() { return distanceSavedKm; }
    public void setDistanceSavedKm(Double distanceSavedKm) { this.distanceSavedKm = distanceSavedKm; }
    public Integer getTimeSavedMinutes() { return timeSavedMinutes; }
    public void setTimeSavedMinutes(Integer timeSavedMinutes) { this.timeSavedMinutes = timeSavedMinutes; }
    public Integer getSlaAtRiskCount() { return slaAtRiskCount; }
    public void setSlaAtRiskCount(Integer slaAtRiskCount) { this.slaAtRiskCount = slaAtRiskCount; }
    public Integer getSlaViolationCount() { return slaViolationCount; }
    public void setSlaViolationCount(Integer slaViolationCount) { this.slaViolationCount = slaViolationCount; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

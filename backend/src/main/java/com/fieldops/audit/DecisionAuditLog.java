package com.fieldops.audit;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "decision_audit_logs")
public class DecisionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long workOrderId;

    private String workOrderNumber;

    @Column(nullable = false)
    private String decisionType; // RECOMMENDATION_GENERATED, RECOMMENDATION_ACCEPTED, RECOMMENDATION_REJECTED

    private Long recommendedTechnicianId;
    private String recommendedTechnicianName;

    private Long selectedTechnicianId;
    private String selectedTechnicianName;

    private Integer score;

    @Column(length = 2000)
    private String explanation;

    private String dispatcherEmail;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    public DecisionAuditLog() {}

    public DecisionAuditLog(Long id, Long workOrderId, String workOrderNumber, String decisionType,
                            Long recommendedTechnicianId, String recommendedTechnicianName,
                            Long selectedTechnicianId, String selectedTechnicianName,
                            Integer score, String explanation, String dispatcherEmail) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.workOrderNumber = workOrderNumber;
        this.decisionType = decisionType;
        this.recommendedTechnicianId = recommendedTechnicianId;
        this.recommendedTechnicianName = recommendedTechnicianName;
        this.selectedTechnicianId = selectedTechnicianId;
        this.selectedTechnicianName = selectedTechnicianName;
        this.score = score;
        this.explanation = explanation;
        this.dispatcherEmail = dispatcherEmail;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long workOrderId;
        private String workOrderNumber;
        private String decisionType;
        private Long recommendedTechnicianId;
        private String recommendedTechnicianName;
        private Long selectedTechnicianId;
        private String selectedTechnicianName;
        private Integer score;
        private String explanation;
        private String dispatcherEmail;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder workOrderId(Long workOrderId) { this.workOrderId = workOrderId; return this; }
        public Builder workOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; return this; }
        public Builder decisionType(String decisionType) { this.decisionType = decisionType; return this; }
        public Builder recommendedTechnicianId(Long recommendedTechnicianId) { this.recommendedTechnicianId = recommendedTechnicianId; return this; }
        public Builder recommendedTechnicianName(String recommendedTechnicianName) { this.recommendedTechnicianName = recommendedTechnicianName; return this; }
        public Builder selectedTechnicianId(Long selectedTechnicianId) { this.selectedTechnicianId = selectedTechnicianId; return this; }
        public Builder selectedTechnicianName(String selectedTechnicianName) { this.selectedTechnicianName = selectedTechnicianName; return this; }
        public Builder score(Integer score) { this.score = score; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder dispatcherEmail(String dispatcherEmail) { this.dispatcherEmail = dispatcherEmail; return this; }

        public DecisionAuditLog build() {
            return new DecisionAuditLog(id, workOrderId, workOrderNumber, decisionType,
                    recommendedTechnicianId, recommendedTechnicianName, selectedTechnicianId,
                    selectedTechnicianName, score, explanation, dispatcherEmail);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
    public Long getRecommendedTechnicianId() { return recommendedTechnicianId; }
    public void setRecommendedTechnicianId(Long recommendedTechnicianId) { this.recommendedTechnicianId = recommendedTechnicianId; }
    public String getRecommendedTechnicianName() { return recommendedTechnicianName; }
    public void setRecommendedTechnicianName(String recommendedTechnicianName) { this.recommendedTechnicianName = recommendedTechnicianName; }
    public Long getSelectedTechnicianId() { return selectedTechnicianId; }
    public void setSelectedTechnicianId(Long selectedTechnicianId) { this.selectedTechnicianId = selectedTechnicianId; }
    public String getSelectedTechnicianName() { return selectedTechnicianName; }
    public void setSelectedTechnicianName(String selectedTechnicianName) { this.selectedTechnicianName = selectedTechnicianName; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getDispatcherEmail() { return dispatcherEmail; }
    public void setDispatcherEmail(String dispatcherEmail) { this.dispatcherEmail = dispatcherEmail; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

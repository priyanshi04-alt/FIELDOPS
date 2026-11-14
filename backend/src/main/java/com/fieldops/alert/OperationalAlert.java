package com.fieldops.alert;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "operational_alerts", indexes = {
        @Index(name = "idx_alert_status", columnList = "status"),
        @Index(name = "idx_alert_severity", columnList = "severity"),
        @Index(name = "idx_alert_type", columnList = "alertType"),
        @Index(name = "idx_alert_fingerprint", columnList = "fingerprint"),
        @Index(name = "idx_alert_work_order", columnList = "workOrderId"),
        @Index(name = "idx_alert_tech", columnList = "technicianId"),
        @Index(name = "idx_alert_timestamp", columnList = "createdAt")
})
public class OperationalAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status = AlertStatus.OPEN;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 2000)
    private String explanation;

    // Phase 4 Alert Hardening & Deduplication
    @Column(length = 255)
    private String fingerprint;

    @Column(nullable = false)
    private boolean isPredictive = false;

    private String relatedEntityType; // WORK_ORDER, TECHNICIAN, INVENTORY, TERRITORY
    private Long relatedEntityId;
    private Long workOrderId;
    private String workOrderNumber;
    private Long technicianId;
    private String technicianName;

    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;

    private String resolvedBy;
    private LocalDateTime resolvedAt;

    @Column(length = 1000)
    private String resolutionNotes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public OperationalAlert() {}

    public OperationalAlert(Long id, AlertType alertType, AlertSeverity severity, AlertStatus status,
                            String title, String message, String explanation, String relatedEntityType,
                            Long relatedEntityId, Long workOrderId, String workOrderNumber,
                            Long technicianId, String technicianName) {
        this.id = id;
        this.alertType = alertType;
        this.severity = severity != null ? severity : AlertSeverity.INFO;
        this.status = status != null ? status : AlertStatus.OPEN;
        this.title = title;
        this.message = message;
        this.explanation = explanation;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.workOrderId = workOrderId;
        this.workOrderNumber = workOrderNumber;
        this.technicianId = technicianId;
        this.technicianName = technicianName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private AlertType alertType;
        private AlertSeverity severity = AlertSeverity.INFO;
        private AlertStatus status = AlertStatus.OPEN;
        private String title;
        private String message;
        private String explanation;
        private String relatedEntityType;
        private Long relatedEntityId;
        private Long workOrderId;
        private String workOrderNumber;
        private Long technicianId;
        private String technicianName;

        private String fingerprint;
        private boolean isPredictive = false;

        public Builder fingerprint(String fingerprint) { this.fingerprint = fingerprint; return this; }
        public Builder isPredictive(boolean isPredictive) { this.isPredictive = isPredictive; return this; }
        public Builder id(Long id) { this.id = id; return this; }
        public Builder alertType(AlertType alertType) { this.alertType = alertType; return this; }
        public Builder severity(AlertSeverity severity) { this.severity = severity; return this; }
        public Builder status(AlertStatus status) { this.status = status; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder relatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; return this; }
        public Builder relatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; return this; }
        public Builder workOrderId(Long workOrderId) { this.workOrderId = workOrderId; return this; }
        public Builder workOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; return this; }
        public Builder technicianId(Long technicianId) { this.technicianId = technicianId; return this; }
        public Builder technicianName(String technicianName) { this.technicianName = technicianName; return this; }

        public OperationalAlert build() {
            OperationalAlert a = new OperationalAlert(id, alertType, severity, status, title, message, explanation,
                    relatedEntityType, relatedEntityId, workOrderId, workOrderNumber, technicianId, technicianName);
            a.setFingerprint(fingerprint);
            a.setPredictive(isPredictive);
            return a;
        }
    }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    public boolean isPredictive() { return isPredictive; }
    public void setPredictive(boolean isPredictive) { this.isPredictive = isPredictive; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

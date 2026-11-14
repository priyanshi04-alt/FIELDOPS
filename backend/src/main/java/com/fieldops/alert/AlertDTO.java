package com.fieldops.alert;

import java.time.LocalDateTime;

public class AlertDTO {

    private Long id;
    private AlertType alertType;
    private AlertSeverity severity;
    private AlertStatus status;
    private String title;
    private String message;
    private String explanation;
    private String relatedEntityType;
    private Long relatedEntityId;
    private Long workOrderId;
    private String workOrderNumber;
    private Long technicianId;
    private String technicianName;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String fingerprint;
    private boolean isPredictive;
    private String resolutionNotes;
    private LocalDateTime createdAt;

    public AlertDTO() {}

    public static AlertDTO fromEntity(OperationalAlert entity) {
        if (entity == null) return null;
        AlertDTO dto = new AlertDTO();
        dto.setId(entity.getId());
        dto.setAlertType(entity.getAlertType());
        dto.setSeverity(entity.getSeverity());
        dto.setStatus(entity.getStatus());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setExplanation(entity.getExplanation());
        dto.setFingerprint(entity.getFingerprint());
        dto.setPredictive(entity.isPredictive());
        dto.setRelatedEntityType(entity.getRelatedEntityType());
        dto.setRelatedEntityId(entity.getRelatedEntityId());
        dto.setWorkOrderId(entity.getWorkOrderId());
        dto.setWorkOrderNumber(entity.getWorkOrderNumber());
        dto.setTechnicianId(entity.getTechnicianId());
        dto.setTechnicianName(entity.getTechnicianName());
        dto.setAcknowledgedBy(entity.getAcknowledgedBy());
        dto.setAcknowledgedAt(entity.getAcknowledgedAt());
        dto.setResolvedBy(entity.getResolvedBy());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setResolutionNotes(entity.getResolutionNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
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
}

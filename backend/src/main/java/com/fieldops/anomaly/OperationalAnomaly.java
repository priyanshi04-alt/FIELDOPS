package com.fieldops.anomaly;

import java.time.LocalDateTime;

public class OperationalAnomaly {

    private String id;
    private String title;
    private String description;
    private String severity; // INFO, WARNING, HIGH, CRITICAL
    private String category; // WORKLOAD, SLA, INVENTORY, TERRITORY, TIMING
    private String entityType; // TECHNICIAN, TERRITORY, INVENTORY, WORK_ORDER
    private String entityId;
    private Double baselineMetric;
    private Double currentMetric;
    private String metricUnit;
    private String recommendation;
    private LocalDateTime detectedAt = LocalDateTime.now();

    public OperationalAnomaly() {}

    public OperationalAnomaly(String id, String title, String description, String severity,
                              String category, String entityType, String entityId,
                              Double baselineMetric, Double currentMetric, String metricUnit,
                              String recommendation) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.entityType = entityType;
        this.entityId = entityId;
        this.baselineMetric = baselineMetric;
        this.currentMetric = currentMetric;
        this.metricUnit = metricUnit;
        this.recommendation = recommendation;
        this.detectedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public Double getBaselineMetric() { return baselineMetric; }
    public void setBaselineMetric(Double baselineMetric) { this.baselineMetric = baselineMetric; }
    public Double getCurrentMetric() { return currentMetric; }
    public void setCurrentMetric(Double currentMetric) { this.currentMetric = currentMetric; }
    public String getMetricUnit() { return metricUnit; }
    public void setMetricUnit(String metricUnit) { this.metricUnit = metricUnit; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}

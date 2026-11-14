package com.fieldops.predictive;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_registry_entries")
public class ModelRegistryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String modelKey;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private String modelVersion;

    @Column(nullable = false)
    private String algorithm;

    @Column(length = 500)
    private String evaluationDataset;

    private Double primaryMetricValue; // e.g. MAE or AUC
    private String primaryMetricName;  // e.g. "MAE (units)" or "ROC-AUC"
    private Double secondaryMetricValue;
    private String secondaryMetricName;

    @Column(length = 500)
    private String syntheticDisclaimer;

    private LocalDateTime trainedAt;
    private LocalDateTime loadedAt = LocalDateTime.now();

    public ModelRegistryEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getEvaluationDataset() { return evaluationDataset; }
    public void setEvaluationDataset(String evaluationDataset) { this.evaluationDataset = evaluationDataset; }
    public Double getPrimaryMetricValue() { return primaryMetricValue; }
    public void setPrimaryMetricValue(Double primaryMetricValue) { this.primaryMetricValue = primaryMetricValue; }
    public String getPrimaryMetricName() { return primaryMetricName; }
    public void setPrimaryMetricName(String primaryMetricName) { this.primaryMetricName = primaryMetricName; }
    public Double getSecondaryMetricValue() { return secondaryMetricValue; }
    public void setSecondaryMetricValue(Double secondaryMetricValue) { this.secondaryMetricValue = secondaryMetricValue; }
    public String getSecondaryMetricName() { return secondaryMetricName; }
    public void setSecondaryMetricName(String secondaryMetricName) { this.secondaryMetricName = secondaryMetricName; }
    public String getSyntheticDisclaimer() { return syntheticDisclaimer; }
    public void setSyntheticDisclaimer(String syntheticDisclaimer) { this.syntheticDisclaimer = syntheticDisclaimer; }
    public LocalDateTime getTrainedAt() { return trainedAt; }
    public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }
    public LocalDateTime getLoadedAt() { return loadedAt; }
    public void setLoadedAt(LocalDateTime loadedAt) { this.loadedAt = loadedAt; }
}

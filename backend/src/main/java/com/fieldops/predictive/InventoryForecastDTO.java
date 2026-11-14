package com.fieldops.predictive;

import java.time.LocalDateTime;

public class InventoryForecastDTO {

    private Long inventoryItemId;
    private String partName;
    private String category;
    private int currentStock;
    private int safetyStock;
    private int supplierLeadDays;
    private double predicted7DayDemand;
    private double dailyDemandRate;
    private int estimatedDaysUntilStockout;
    private String recommendation; // REORDER, MONITOR, OPTIMAL
    private int recommendedReorderQuantity;
    private double confidenceScore; // 0 - 100
    private String explanation;
    private String modelVersion;
    private String algorithm;
    private String syntheticDisclaimer;
    private LocalDateTime generatedAt = LocalDateTime.now();

    public InventoryForecastDTO() {}

    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getSafetyStock() { return safetyStock; }
    public void setSafetyStock(int safetyStock) { this.safetyStock = safetyStock; }

    public int getSupplierLeadDays() { return supplierLeadDays; }
    public void setSupplierLeadDays(int supplierLeadDays) { this.supplierLeadDays = supplierLeadDays; }

    public double getPredicted7DayDemand() { return predicted7DayDemand; }
    public void setPredicted7DayDemand(double predicted7DayDemand) { this.predicted7DayDemand = predicted7DayDemand; }

    public double getDailyDemandRate() { return dailyDemandRate; }
    public void setDailyDemandRate(double dailyDemandRate) { this.dailyDemandRate = dailyDemandRate; }

    public int getEstimatedDaysUntilStockout() { return estimatedDaysUntilStockout; }
    public void setEstimatedDaysUntilStockout(int estimatedDaysUntilStockout) { this.estimatedDaysUntilStockout = estimatedDaysUntilStockout; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public int getRecommendedReorderQuantity() { return recommendedReorderQuantity; }
    public void setRecommendedReorderQuantity(int recommendedReorderQuantity) { this.recommendedReorderQuantity = recommendedReorderQuantity; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getSyntheticDisclaimer() { return syntheticDisclaimer; }
    public void setSyntheticDisclaimer(String syntheticDisclaimer) { this.syntheticDisclaimer = syntheticDisclaimer; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getExpiresAt() { return generatedAt != null ? generatedAt.plusHours(12) : null; }
    public boolean isStale() { return getExpiresAt() != null && LocalDateTime.now().isAfter(getExpiresAt()); }
    public String getPredictionStatus() { return isStale() ? "STALE" : "CURRENT"; }
}

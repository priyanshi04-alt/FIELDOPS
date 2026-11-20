package com.fieldops.intelligence;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationsPerformanceMetrics {

    private long databaseRetrievalDurationMs;
    private long transactionDurationMs;
    private long routeOptimizationDurationMs;
    private long slaInferenceDurationMs;
    private long anomalyDetectionDurationMs;
    private long inventoryForecastingDurationMs;
    private long totalOverviewProcessingDurationMs;
    private int retrievedWorkOrdersCount;
    private int retrievedTechniciansCount;
    private int retrievedInventoryItemsCount;

    public OperationsPerformanceMetrics() {}

    public OperationsPerformanceMetrics(long databaseRetrievalDurationMs,
                                        long transactionDurationMs,
                                        long routeOptimizationDurationMs,
                                        long slaInferenceDurationMs,
                                        long anomalyDetectionDurationMs,
                                        long inventoryForecastingDurationMs,
                                        long totalOverviewProcessingDurationMs,
                                        int retrievedWorkOrdersCount,
                                        int retrievedTechniciansCount,
                                        int retrievedInventoryItemsCount) {
        this.databaseRetrievalDurationMs = databaseRetrievalDurationMs;
        this.transactionDurationMs = transactionDurationMs;
        this.routeOptimizationDurationMs = routeOptimizationDurationMs;
        this.slaInferenceDurationMs = slaInferenceDurationMs;
        this.anomalyDetectionDurationMs = anomalyDetectionDurationMs;
        this.inventoryForecastingDurationMs = inventoryForecastingDurationMs;
        this.totalOverviewProcessingDurationMs = totalOverviewProcessingDurationMs;
        this.retrievedWorkOrdersCount = retrievedWorkOrdersCount;
        this.retrievedTechniciansCount = retrievedTechniciansCount;
        this.retrievedInventoryItemsCount = retrievedInventoryItemsCount;
    }

    public long getDatabaseRetrievalDurationMs() { return databaseRetrievalDurationMs; }
    public void setDatabaseRetrievalDurationMs(long databaseRetrievalDurationMs) { this.databaseRetrievalDurationMs = databaseRetrievalDurationMs; }

    public long getTransactionDurationMs() { return transactionDurationMs; }
    public void setTransactionDurationMs(long transactionDurationMs) { this.transactionDurationMs = transactionDurationMs; }

    public long getRouteOptimizationDurationMs() { return routeOptimizationDurationMs; }
    public void setRouteOptimizationDurationMs(long routeOptimizationDurationMs) { this.routeOptimizationDurationMs = routeOptimizationDurationMs; }

    public long getSlaInferenceDurationMs() { return slaInferenceDurationMs; }
    public void setSlaInferenceDurationMs(long slaInferenceDurationMs) { this.slaInferenceDurationMs = slaInferenceDurationMs; }

    public long getAnomalyDetectionDurationMs() { return anomalyDetectionDurationMs; }
    public void setAnomalyDetectionDurationMs(long anomalyDetectionDurationMs) { this.anomalyDetectionDurationMs = anomalyDetectionDurationMs; }

    public long getInventoryForecastingDurationMs() { return inventoryForecastingDurationMs; }
    public void setInventoryForecastingDurationMs(long inventoryForecastingDurationMs) { this.inventoryForecastingDurationMs = inventoryForecastingDurationMs; }

    public long getTotalOverviewProcessingDurationMs() { return totalOverviewProcessingDurationMs; }
    public void setTotalOverviewProcessingDurationMs(long totalOverviewProcessingDurationMs) { this.totalOverviewProcessingDurationMs = totalOverviewProcessingDurationMs; }

    public int getRetrievedWorkOrdersCount() { return retrievedWorkOrdersCount; }
    public void setRetrievedWorkOrdersCount(int retrievedWorkOrdersCount) { this.retrievedWorkOrdersCount = retrievedWorkOrdersCount; }

    public int getRetrievedTechniciansCount() { return retrievedTechniciansCount; }
    public void setRetrievedTechniciansCount(int retrievedTechniciansCount) { this.retrievedTechniciansCount = retrievedTechniciansCount; }

    public int getRetrievedInventoryItemsCount() { return retrievedInventoryItemsCount; }
    public void setRetrievedInventoryItemsCount(int retrievedInventoryItemsCount) { this.retrievedInventoryItemsCount = retrievedInventoryItemsCount; }

    @Override
    public String toString() {
        return String.format("OverviewMetrics[Total: %d ms | DB: %d ms (Tx: %d ms) | RouteOpt: %d ms | SLA: %d ms | Anomaly: %d ms | Inv: %d ms | Items: %d WO, %d Tech, %d Inv]",
                totalOverviewProcessingDurationMs, databaseRetrievalDurationMs, transactionDurationMs,
                routeOptimizationDurationMs, slaInferenceDurationMs, anomalyDetectionDurationMs,
                inventoryForecastingDurationMs, retrievedWorkOrdersCount, retrievedTechniciansCount, retrievedInventoryItemsCount);
    }
}

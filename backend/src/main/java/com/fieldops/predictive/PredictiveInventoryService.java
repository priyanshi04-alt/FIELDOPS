package com.fieldops.predictive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fieldops.alert.AlertSeverity;
import com.fieldops.alert.AlertService;
import com.fieldops.alert.AlertType;
import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.inventory.InventoryItem;
import com.fieldops.inventory.InventoryItemRepository;
import com.fieldops.websocket.OperationsEvent;
import com.fieldops.websocket.OperationsEventPublisher;
import com.fieldops.websocket.OperationsEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictiveInventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ModelRegistryService modelRegistryService;
    private final AlertService alertService;
    private final OperationsEventPublisher eventPublisher;

    public PredictiveInventoryService(InventoryItemRepository inventoryItemRepository,
                                      ModelRegistryService modelRegistryService,
                                      AlertService alertService,
                                      OperationsEventPublisher eventPublisher) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.modelRegistryService = modelRegistryService;
        this.alertService = alertService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<InventoryForecastDTO> getAllForecasts() {
        List<InventoryItem> items = inventoryItemRepository.findAll();
        List<InventoryForecastDTO> list = new ArrayList<>();
        for (InventoryItem item : items) {
            list.add(forecastItem(item));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public InventoryForecastDTO getForecast(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + id));
        return forecastItem(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryForecastDTO> getReplenishmentRecommendations() {
        return computeReplenishmentRecommendations(inventoryItemRepository.findAll());
    }

    /**
     * Stage B in-memory inventory forecasting and replenishment evaluation.
     * Computes consumption velocity and lead-time reorder requirements strictly in-memory.
     */
    public List<InventoryForecastDTO> computeReplenishmentRecommendations(List<InventoryItem> items) {
        List<InventoryForecastDTO> recs = new ArrayList<>();
        if (items != null) {
            for (InventoryItem item : items) {
                InventoryForecastDTO f = forecastItem(item);
                if ("REORDER".equals(f.getRecommendation()) || "MONITOR".equals(f.getRecommendation())) {
                    recs.add(f);
                }
            }
        }
        return recs;
    }

    public InventoryForecastDTO forecastItem(InventoryItem item) {
        JsonNode modelConfig = modelRegistryService.getInventoryModelConfig();

        int currentStock = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
        int safetyStock = item.getMinThreshold() != null ? item.getMinThreshold() : 5;
        int leadDays = 4; // Standard regional supplier turnaround

        // Synthetic Consumption Feature Simulation:
        // Use part stock scale to derive representative historical rolling features
        double baselineDailyVelocity = Math.max(0.4, (double) safetyStock / 4.0);
        double rolling7d = baselineDailyVelocity * 7.0;
        double rolling14d = baselineDailyVelocity * 14.0;
        double rolling30d = baselineDailyVelocity * 30.0;
        double rolling7dStd = baselineDailyVelocity * 0.35;

        double predictedDemand = rolling7d; // Baseline default

        if (modelConfig != null && modelConfig.has("coefficients")) {
            double intercept = modelConfig.path("intercept").asDouble(0.0);
            JsonNode coef = modelConfig.path("coefficients");
            double c7 = coef.path("rolling_7d_consumption").asDouble(0.5);
            double c14 = coef.path("rolling_14d_consumption").asDouble(0.2);
            double c30 = coef.path("rolling_30d_consumption").asDouble(0.1);
            double cStd = coef.path("rolling_7d_std").asDouble(0.1);

            predictedDemand = intercept + (c7 * rolling7d) + (c14 * rolling14d) + (c30 * rolling30d) + (cStd * rolling7dStd);
        }

        predictedDemand = Math.max(1.0, Math.round(predictedDemand * 10.0) / 10.0);
        double dailyRate = Math.round((predictedDemand / 7.0) * 10.0) / 10.0;

        int daysUntilStockout = dailyRate > 0 ? (int) Math.floor(currentStock / dailyRate) : 99;

        // Deterministic Lead-Time Reorder Point:
        // Reorder if current stock <= (Safety Stock + (Daily Rate * Lead Days))
        double leadTimeBuffer = safetyStock + (dailyRate * leadDays);
        String recommendation;
        int recommendedQty = 0;

        if (currentStock <= leadTimeBuffer) {
            recommendation = "REORDER";
            recommendedQty = (int) Math.ceil((leadTimeBuffer * 1.5) - currentStock);
            recommendedQty = Math.max(recommendedQty, safetyStock * 2);
        } else if (currentStock <= leadTimeBuffer * 1.3) {
            recommendation = "MONITOR";
        } else {
            recommendation = "OPTIMAL";
        }

        String explanation = String.format(
                "Predicted 7-day demand is %.1f units (%.1f/day). With %d days supplier lead time and safety threshold of %d units, %s.",
                predictedDemand, dailyRate, leadDays, safetyStock,
                "REORDER".equals(recommendation)
                        ? "stock is projected to breach safety buffer in " + daysUntilStockout + " days. Recommend ordering " + recommendedQty + " units"
                        : "inventory buffer remains sufficient for " + daysUntilStockout + " days"
        );

        InventoryForecastDTO dto = new InventoryForecastDTO();
        dto.setInventoryItemId(item.getId());
        dto.setPartName(item.getName());
        dto.setCategory(item.getCategory() != null ? item.getCategory() : "GENERAL");
        dto.setCurrentStock(currentStock);
        dto.setSafetyStock(safetyStock);
        dto.setSupplierLeadDays(leadDays);
        dto.setPredicted7DayDemand(predictedDemand);
        dto.setDailyDemandRate(dailyRate);
        dto.setEstimatedDaysUntilStockout(daysUntilStockout);
        dto.setRecommendation(recommendation);
        dto.setRecommendedReorderQuantity(recommendedQty);
        dto.setConfidenceScore(86.5);
        dto.setExplanation(explanation);
        dto.setModelVersion(modelConfig != null ? modelConfig.path("modelVersion").asText("1.0.0") : "1.0.0");
        dto.setAlgorithm(modelConfig != null ? modelConfig.path("algorithm").asText("Ridge Regression") : "Ridge Regression");
        dto.setSyntheticDisclaimer("DEMO / SIMULATION DATA: Synthetic demonstration dataset; results do not represent production performance.");
        dto.setGeneratedAt(LocalDateTime.now());

        return dto;
    }

    @Transactional
    public int scanAndGenerateInventoryAlerts() {
        if (alertService == null) return 0;
        int countCreated = 0;
        List<InventoryItem> items = inventoryItemRepository.findAll();
        for (InventoryItem item : items) {
            try {
                InventoryForecastDTO dto = forecastItem(item);
                if ("REORDER".equals(dto.getRecommendation()) && dto.getEstimatedDaysUntilStockout() <= 5) {
                    boolean created = alertService.createOrUpdateAlert(
                            AlertType.PREDICTED_STOCKOUT,
                            dto.getEstimatedDaysUntilStockout() <= 2 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
                            "Projected Stockout: " + item.getName(),
                            String.format("%s projected to stock out in %d days (current stock: %d).", item.getName(), dto.getEstimatedDaysUntilStockout(), dto.getCurrentStock()),
                            dto.getExplanation(),
                            "INVENTORY",
                            item.getId(),
                            null, null, null, null,
                            "STOCKOUT_PREDICTED",
                            true
                    );
                    if (created) countCreated++;
                }
            } catch (Exception ignored) {}
        }
        return countCreated;
    }
}

package com.fieldops.predictive;

import com.fieldops.inventory.InventoryItem;
import com.fieldops.technician.Technician;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PredictiveOperationsUnitTest {

    private PredictiveInventoryService inventoryService;
    private PredictiveSlaService slaService;
    private TechnicianPerformanceService performanceService;

    @BeforeEach
    void setUp() {
        inventoryService = new PredictiveInventoryService(null, new ModelRegistryService(null, null, null) {
            @Override
            public void init() {}
        }, null, null);

        slaService = new PredictiveSlaService(null, new com.fieldops.sla.SlaRiskService(null),
                new ModelRegistryService(null, null, null) {
                    @Override
                    public void init() {}
                }, null, null);

        performanceService = new TechnicianPerformanceService(null, null, null);
    }

    @Test
    @DisplayName("Predictive Inventory: Computes stockout days and replenishment recommendations without target leakage")
    void testInventoryForecasting() {
        InventoryItem item = InventoryItem.builder()
                .id(1L)
                .name("Danfoss MBS 3000 Pressure Sensor")
                .availableQuantity(8)
                .minThreshold(10)
                .category("INSTRUMENTATION")
                .build();

        InventoryForecastDTO forecast = inventoryService.forecastItem(item);

        assertNotNull(forecast);
        assertEquals("Danfoss MBS 3000 Pressure Sensor", forecast.getPartName());
        assertTrue(forecast.getPredicted7DayDemand() > 0, "Demand forecast must be positive");
        assertTrue(forecast.getEstimatedDaysUntilStockout() > 0, "Days until stockout must be computed");
        assertEquals("REORDER", forecast.getRecommendation(), "Low available stock below buffer must trigger REORDER");
        assertTrue(forecast.getRecommendedReorderQuantity() >= 10, "Reorder quantity should restore buffer");
        assertNotNull(forecast.getSyntheticDisclaimer(), "Disclaimer must explicitly state simulation dataset");
    }

    @Test
    @DisplayName("Predictive SLA: Fuses authoritative deterministic risk with ML breach probability")
    void testPredictiveSlaRiskFusion() {
        Technician tech = Technician.builder()
                .id(10L)
                .name("Arjun Reddy")
                .activeJobsCount(3)
                .slaSuccessRate(88.0)
                .latitude(28.70)
                .longitude(77.10)
                .build();

        WorkOrder tightJob = WorkOrder.builder()
                .id(1042L)
                .workOrderNumber("WO-1042")
                .title("PLC Bus Communication Failure")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.IN_PROGRESS)
                .technician(tech)
                .latitude(28.45)
                .longitude(77.02)
                .slaDeadline(LocalDateTime.now().plusMinutes(25))
                .estimatedDurationHours(1.5)
                .build();

        PredictiveSlaResultDTO result = slaService.computePredictiveRisk(tightJob);

        assertNotNull(result);
        assertEquals("WO-1042", result.getWorkOrderNumber());
        assertTrue(result.getMlBreachProbability() > 0.50, "Tight SLA and far tech backlog must produce high ML breach probability");
        assertTrue(result.getFusedRiskScore() >= 60, "Fused risk score must reflect high breach probability");
        assertEquals("LIKELY_BREACH", result.getFinalAssessment());
        assertFalse(result.getTopContributingFactors().isEmpty(), "Must list top contributing features");
    }

    @Test
    @DisplayName("Technician Performance: Computes explainable composite performance score and tier")
    void testTechnicianPerformanceScore() {
        Technician starTech = Technician.builder()
                .id(20L)
                .name("Sarah Jenkins")
                .rating(4.9)
                .completedJobs(140)
                .activeJobsCount(1)
                .slaSuccessRate(98.5)
                .utilizationRate(65.0)
                .build();

        TechnicianPerformanceDTO scorecard = performanceService.computePerformance(starTech);

        assertNotNull(scorecard);
        assertTrue(scorecard.getPerformanceScore() >= 85, "High adherence and rating must yield score >= 85");
        assertEquals("ELITE", scorecard.getPerformanceTier());
        assertTrue(scorecard.getSlaAdherencePercent() > 95.0);
        assertFalse(scorecard.getOperationalStrengths().isEmpty());
    }

    @Test
    @DisplayName("Predictive Reliability: Verifies prediction expiration and staleness policy")
    void testStalePredictionPolicy() {
        InventoryItem item = InventoryItem.builder()
                .id(1L)
                .name("Danfoss MBS 3000 Pressure Sensor")
                .availableQuantity(8)
                .minThreshold(10)
                .category("INSTRUMENTATION")
                .build();

        InventoryForecastDTO forecast = inventoryService.forecastItem(item);
        assertNotNull(forecast.getGeneratedAt());
        assertNotNull(forecast.getExpiresAt());
        assertFalse(forecast.isStale(), "Newly generated forecast must be CURRENT");
        assertEquals("CURRENT", forecast.getPredictionStatus());

        // Simulate historical expired prediction (older than 12 hours)
        forecast.setGeneratedAt(LocalDateTime.now().minusHours(14));
        assertTrue(forecast.isStale(), "Forecast generated 14 hours ago must be marked STALE");
        assertEquals("STALE", forecast.getPredictionStatus());
    }

    @Test
    @DisplayName("SEC-6301 Regression: computePredictiveRisk must be purely read-only and side-effect free")
    void testComputePredictiveRisk_IsSideEffectFree() {
        java.util.concurrent.atomic.AtomicBoolean alertCreated = new java.util.concurrent.atomic.AtomicBoolean(false);
        com.fieldops.alert.AlertService spyAlertService = new com.fieldops.alert.AlertService(null, null, null, null, null, null) {
            @Override
            public boolean createOrUpdateAlert(com.fieldops.alert.AlertType type,
                                               com.fieldops.alert.AlertSeverity severity,
                                               String title,
                                               String message,
                                               String explanation,
                                               String entityType,
                                               Long entityId,
                                               Long workOrderId,
                                               String workOrderNumber,
                                               Long technicianId,
                                               String technicianName,
                                               String conditionKey,
                                               boolean isPredictive) {
                alertCreated.set(true);
                return true;
            }
        };

        PredictiveSlaService isolatedService = new PredictiveSlaService(
                null,
                new com.fieldops.sla.SlaRiskService(null),
                new ModelRegistryService(null, null, null) {
                    @Override
                    public void init() {}
                },
                spyAlertService,
                null
        );

        Technician tech = Technician.builder()
                .id(10L)
                .name("Arjun Reddy")
                .activeJobsCount(4)
                .slaSuccessRate(70.0)
                .latitude(28.70)
                .longitude(77.10)
                .build();

        WorkOrder tightJob = WorkOrder.builder()
                .id(1042L)
                .workOrderNumber("WO-1042")
                .title("Critical Substation Trip")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.IN_PROGRESS)
                .technician(tech)
                .latitude(28.45)
                .longitude(77.02)
                .slaDeadline(LocalDateTime.now().plusMinutes(10))
                .estimatedDurationHours(2.0)
                .build();

        PredictiveSlaResultDTO result = isolatedService.computePredictiveRisk(tightJob);

        assertNotNull(result);
        assertTrue(result.getMlBreachProbability() >= 0.70, "ML breach prob should exceed 70%");
        // Crucial: verify that createOrUpdateAlert was NOT called during computePredictiveRisk
        assertFalse(alertCreated.get(), "computePredictiveRisk must not invoke createOrUpdateAlert or persist alerts");
    }

    @Test
    @DisplayName("SEC-6301 Regression: forecastItem must be purely read-only and side-effect free")
    void testForecastItem_IsSideEffectFree() {
        java.util.concurrent.atomic.AtomicBoolean alertCreated = new java.util.concurrent.atomic.AtomicBoolean(false);
        com.fieldops.alert.AlertService spyAlertService = new com.fieldops.alert.AlertService(null, null, null, null, null, null) {
            @Override
            public boolean createOrUpdateAlert(com.fieldops.alert.AlertType type,
                                               com.fieldops.alert.AlertSeverity severity,
                                               String title,
                                               String message,
                                               String explanation,
                                               String entityType,
                                               Long entityId,
                                               Long workOrderId,
                                               String workOrderNumber,
                                               Long technicianId,
                                               String technicianName,
                                               String conditionKey,
                                               boolean isPredictive) {
                alertCreated.set(true);
                return true;
            }
        };

        PredictiveInventoryService isolatedInventory = new PredictiveInventoryService(
                null,
                new ModelRegistryService(null, null, null) {
                    @Override
                    public void init() {}
                },
                spyAlertService,
                null
        );

        InventoryItem criticalItem = InventoryItem.builder()
                .id(99L)
                .name("Low Stock Critical Sensor")
                .availableQuantity(1)
                .minThreshold(15)
                .category("SENSORS")
                .build();

        InventoryForecastDTO dto = isolatedInventory.forecastItem(criticalItem);
        assertNotNull(dto);
        assertEquals("REORDER", dto.getRecommendation());
        assertFalse(alertCreated.get(), "forecastItem must not invoke createOrUpdateAlert during read-only evaluation");
    }

    @Test
    @DisplayName("SEC-6301 Regression: computePerformance must be purely read-only and side-effect free")
    void testComputePerformance_IsSideEffectFree() {
        java.util.concurrent.atomic.AtomicBoolean alertCreated = new java.util.concurrent.atomic.AtomicBoolean(false);
        com.fieldops.alert.AlertService spyAlertService = new com.fieldops.alert.AlertService(null, null, null, null, null, null) {
            @Override
            public boolean createOrUpdateAlert(com.fieldops.alert.AlertType type,
                                               com.fieldops.alert.AlertSeverity severity,
                                               String title,
                                               String message,
                                               String explanation,
                                               String entityType,
                                               Long entityId,
                                               Long workOrderId,
                                               String workOrderNumber,
                                               Long technicianId,
                                               String technicianName,
                                               String conditionKey,
                                               boolean isPredictive) {
                alertCreated.set(true);
                return true;
            }
        };

        TechnicianPerformanceService isolatedPerf = new TechnicianPerformanceService(null, null, spyAlertService);

        Technician strugglingTech = Technician.builder()
                .id(50L)
                .name("Struggling Tech")
                .rating(3.0)
                .completedJobs(10)
                .activeJobsCount(3)
                .slaSuccessRate(50.0)
                .utilizationRate(95.0)
                .build();

        TechnicianPerformanceDTO dto = isolatedPerf.computePerformance(strugglingTech);
        assertNotNull(dto);
        assertTrue(dto.getPerformanceScore() < 65);
        assertFalse(alertCreated.get(), "computePerformance must not invoke createOrUpdateAlert during read-only evaluation");
    }
}

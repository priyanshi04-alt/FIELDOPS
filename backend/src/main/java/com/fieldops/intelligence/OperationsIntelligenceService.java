package com.fieldops.intelligence;

import com.fieldops.alert.AlertDTO;
import com.fieldops.alert.AlertService;
import com.fieldops.anomaly.OperationalAnomaly;
import com.fieldops.anomaly.OperationalAnomalyService;
import com.fieldops.assignment.AssignmentRecommendation;
import com.fieldops.assignment.TechnicianAssignmentService;
import com.fieldops.routing.multistop.MultiStopRouteService;
import com.fieldops.routing.multistop.OptimizedRoute;
import com.fieldops.routing.multistop.RouteDecisionAudit;
import com.fieldops.sla.SlaRiskResult;
import com.fieldops.sla.SlaRiskService;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 6.7.5: Operations Intelligence Overview Processing Architecture.
 *
 * Implements a strict three-stage pipeline:
 *  - Stage A: Database retrieval via OperationsSnapshotService inside a scoped read-only transaction.
 *  - Stage B: CPU-bound computations (SLA scoring, 2-Opt TSP routing, Anomaly detection,
 *             Ridge Regression, and Logistic Regression) executed 100% outside database transactions.
 *  - Stage C: Overview response assembly with performance metric instrumentation.
 *
 * This non-transactional orchestrator ensures HikariCP database connections are held only for ~5-15 ms
 * rather than remaining captive across 650-3000 ms of CPU calculation.
 */
@Service
public class OperationsIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(OperationsIntelligenceService.class);

    private final SlaRiskService slaRiskService;
    private final TechnicianAssignmentService assignmentService;
    private final OperationalAnomalyService anomalyService;
    private final WorkOrderRepository workOrderRepository;
    private final AlertService alertService;
    private final MultiStopRouteService multiStopRouteService;
    private final TechnicianRepository technicianRepository;
    private final com.fieldops.predictive.PredictiveSlaService predictiveSlaService;
    private final com.fieldops.predictive.PredictiveInventoryService predictiveInventoryService;
    private final com.fieldops.predictive.ModelRegistryService modelRegistryService;
    private final OperationsSnapshotService snapshotService;

    public OperationsIntelligenceService(SlaRiskService slaRiskService,
                                         TechnicianAssignmentService assignmentService,
                                         OperationalAnomalyService anomalyService,
                                         WorkOrderRepository workOrderRepository,
                                         AlertService alertService,
                                         MultiStopRouteService multiStopRouteService,
                                         TechnicianRepository technicianRepository,
                                         com.fieldops.predictive.PredictiveSlaService predictiveSlaService,
                                         com.fieldops.predictive.PredictiveInventoryService predictiveInventoryService,
                                         com.fieldops.predictive.ModelRegistryService modelRegistryService,
                                         OperationsSnapshotService snapshotService) {
        this.slaRiskService = slaRiskService;
        this.assignmentService = assignmentService;
        this.anomalyService = anomalyService;
        this.workOrderRepository = workOrderRepository;
        this.alertService = alertService;
        this.multiStopRouteService = multiStopRouteService;
        this.technicianRepository = technicianRepository;
        this.predictiveSlaService = predictiveSlaService;
        this.predictiveInventoryService = predictiveInventoryService;
        this.modelRegistryService = modelRegistryService;
        this.snapshotService = snapshotService;
    }

    /**
     * Backward-compatible constructor for existing tests and configurations.
     */
    public OperationsIntelligenceService(SlaRiskService slaRiskService,
                                         TechnicianAssignmentService assignmentService,
                                         OperationalAnomalyService anomalyService,
                                         WorkOrderRepository workOrderRepository,
                                         AlertService alertService,
                                         MultiStopRouteService multiStopRouteService,
                                         TechnicianRepository technicianRepository,
                                         com.fieldops.predictive.PredictiveSlaService predictiveSlaService,
                                         com.fieldops.predictive.PredictiveInventoryService predictiveInventoryService,
                                         com.fieldops.predictive.ModelRegistryService modelRegistryService) {
        this(slaRiskService, assignmentService, anomalyService, workOrderRepository, alertService,
                multiStopRouteService, technicianRepository, predictiveSlaService, predictiveInventoryService,
                modelRegistryService, new OperationsSnapshotService(workOrderRepository, technicianRepository,
                        null, alertService, null, modelRegistryService));
    }

    /**
     * Non-transactional overview generation orchestrator.
     * Database connection is acquired and released in Stage A.
     * All subsequent computations run outside transaction boundaries.
     */
    public OperationsIntelligenceOverview getOverview() {
        long tTotalStart = System.nanoTime();

        // =========================================================================
        // STAGE A: DATABASE RETRIEVAL (Scoped Read-Only Transaction in Separate Bean)
        // =========================================================================
        long tDbStart = System.nanoTime();
        OperationsDataSnapshot snapshot = snapshotService.captureSnapshot();
        long dbDurationMs = (System.nanoTime() - tDbStart) / 1_000_000;
        long txDurationMs = snapshot.getTransactionDurationMs();

        // =========================================================================
        // STAGE B: CPU-BOUND COMPUTATION (100% Outside Database Transactions)
        // =========================================================================
        List<WorkOrder> allWorkOrders = snapshot.getWorkOrders();
        List<Technician> allTechnicians = snapshot.getTechnicians();

        // 1. Calculate SLA risks for all open work orders
        long tSlaStart = System.nanoTime();
        List<SlaRiskResult> calculatedRisks = allWorkOrders.stream()
                .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED)
                .map(slaRiskService::calculateRisk)
                .sorted(Comparator.comparingInt(SlaRiskResult::getRiskScore).reversed())
                .collect(Collectors.toList());

        List<SlaRiskResult> highRisks = calculatedRisks.stream()
                .filter(r -> r.getRiskScore() >= 60)
                .limit(6)
                .collect(Collectors.toList());

        int criticalCount = (int) calculatedRisks.stream().filter(r -> r.getRiskScore() >= 80).count();
        long slaRiskDurationMs = (System.nanoTime() - tSlaStart) / 1_000_000;

        // 2. Pending smart dispatches for unassigned work orders (in-memory candidate scoring)
        List<WorkOrder> unassigned = allWorkOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.NEW || wo.getTechnician() == null)
                .sorted(Comparator.comparing(WorkOrder::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(4)
                .collect(Collectors.toList());

        List<AssignmentRecommendation> recommendations = new ArrayList<>();
        for (WorkOrder wo : unassigned) {
            try {
                AssignmentRecommendation rec = assignmentService.computeRecommendation(wo, allTechnicians);
                if (rec != null) {
                    recommendations.add(rec);
                }
            } catch (Exception ex) {
                log.warn("Recommendation calculation skipped for WO {}: {}", wo.getId(), ex.getMessage());
            }
        }

        // 3. Operational anomalies detection (in-memory)
        long tAnomStart = System.nanoTime();
        List<OperationalAnomaly> anomalies = Collections.emptyList();
        try {
            if (anomalyService != null) {
                anomalies = anomalyService.computeAnomalies(
                        snapshot.getWorkOrders(),
                        snapshot.getTechnicians(),
                        snapshot.getInventoryItems()
                );
            }
        } catch (Exception ex) {
            log.warn("Anomaly detection fallback: {}", ex.getMessage());
        }
        long anomDurationMs = (System.nanoTime() - tAnomStart) / 1_000_000;

        // 4. Active operational alerts (retrieved in Stage A)
        List<AlertDTO> activeAlerts = snapshot.getActiveAlerts() != null ? snapshot.getActiveAlerts() : Collections.emptyList();

        // 5. Multi-stop sample route optimization (in-memory 2-Opt TSP)
        long tRouteStart = System.nanoTime();
        OptimizedRoute sampleRoute = null;
        List<RouteDecisionAudit> recentAudits = snapshot.getRecentRouteAudits() != null ? snapshot.getRecentRouteAudits() : Collections.emptyList();
        try {
            Technician topTech = snapshot.getTopTechnician();
            if (topTech != null && multiStopRouteService != null) {
                sampleRoute = multiStopRouteService.computeOptimizedRoute(topTech, snapshot.getWorkOrders(), LocalDateTime.now());
            }
        } catch (Exception ex) {
            log.warn("Sample route optimization fallback: {}", ex.getMessage());
        }
        long routeDurationMs = (System.nanoTime() - tRouteStart) / 1_000_000;

        // 6. Operations Health score calculation (0 - 100)
        int health = 100 - (criticalCount * 5) - (anomalies.size() * 3) - (activeAlerts.size() * 2);
        health = Math.max(45, Math.min(100, health));

        // 7. Phase 4 Predictive Intelligence (Ridge Regression & Logistic Regression in-memory)
        long tInvStart = System.nanoTime();
        List<com.fieldops.predictive.InventoryForecastDTO> topStockouts = Collections.emptyList();
        int predictedStockoutsCount = 0;
        try {
            if (predictiveInventoryService != null) {
                List<com.fieldops.predictive.InventoryForecastDTO> reorders =
                        predictiveInventoryService.computeReplenishmentRecommendations(snapshot.getInventoryItems());
                predictedStockoutsCount = (int) reorders.stream().filter(r -> r.getEstimatedDaysUntilStockout() <= 7).count();
                topStockouts = reorders.stream().limit(5).collect(Collectors.toList());
            }
        } catch (Exception ex) {
            log.warn("Predictive inventory calculation fallback: {}", ex.getMessage());
        }
        long invDurationMs = (System.nanoTime() - tInvStart) / 1_000_000;

        long tPredSlaStart = System.nanoTime();
        List<com.fieldops.predictive.PredictiveSlaResultDTO> topBreaches = Collections.emptyList();
        int predictedBreachesCount = 0;
        try {
            if (predictiveSlaService != null) {
                List<com.fieldops.predictive.PredictiveSlaResultDTO> slaPreds =
                        predictiveSlaService.computeAllActive(snapshot.getWorkOrders());
                List<com.fieldops.predictive.PredictiveSlaResultDTO> breaches = slaPreds.stream()
                        .filter(p -> "LIKELY_BREACH".equals(p.getFinalAssessment()) || p.getMlBreachProbability() >= 0.65)
                        .sorted(Comparator.comparingDouble(com.fieldops.predictive.PredictiveSlaResultDTO::getMlBreachProbability).reversed())
                        .collect(Collectors.toList());
                predictedBreachesCount = breaches.size();
                topBreaches = breaches.stream().limit(5).collect(Collectors.toList());
            }
        } catch (Exception ex) {
            log.warn("Predictive SLA calculation fallback: {}", ex.getMessage());
        }
        long predSlaDurationMs = (System.nanoTime() - tPredSlaStart) / 1_000_000;

        // =========================================================================
        // STAGE C: RESPONSE ASSEMBLY & PERFORMANCE INSTRUMENTATION
        // =========================================================================
        OperationsIntelligenceOverview overview = new OperationsIntelligenceOverview(
                anomalies.size(),
                criticalCount,
                highRisks,
                anomalies,
                recommendations
        );
        overview.setOperationsHealthScore(health);
        overview.setActiveAlertsCount(activeAlerts.size());
        overview.setActiveAlerts(activeAlerts);
        overview.setSampleOptimizedRoute(sampleRoute);
        overview.setRecentRouteAudits(recentAudits);

        if (snapshot.getModelRegistry() != null && !snapshot.getModelRegistry().isEmpty()) {
            overview.setModelRegistry(snapshot.getModelRegistry());
        }
        overview.setPredictedStockoutsCount(predictedStockoutsCount);
        overview.setTopStockoutRisks(topStockouts);
        overview.setPredictedBreachesCount(predictedBreachesCount);
        overview.setTopPredictedBreaches(topBreaches);

        long totalDurationMs = (System.nanoTime() - tTotalStart) / 1_000_000;
        long totalSlaDurationMs = slaRiskDurationMs + predSlaDurationMs;

        OperationsPerformanceMetrics metrics = new OperationsPerformanceMetrics(
                dbDurationMs,
                txDurationMs,
                routeDurationMs,
                totalSlaDurationMs,
                anomDurationMs,
                invDurationMs,
                totalDurationMs,
                allWorkOrders.size(),
                allTechnicians.size(),
                snapshot.getInventoryItems().size()
        );
        overview.setPerformanceMetrics(metrics);

        log.info("{}", metrics);
        return overview;
    }
}

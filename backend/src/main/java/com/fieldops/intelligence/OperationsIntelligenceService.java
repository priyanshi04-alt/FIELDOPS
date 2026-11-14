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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationsIntelligenceService {

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
    }

    @Transactional(readOnly = true)
    public OperationsIntelligenceOverview getOverview() {
        List<WorkOrder> allWorkOrders = workOrderRepository.findAll();

        // 1. Calculate SLA risks for all open work orders
        List<SlaRiskResult> calculatedRisks = allWorkOrders.stream()
                .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED)
                .map(slaRiskService::calculateRisk)
                .sorted(Comparator.comparingInt(SlaRiskResult::getRiskScore).reversed())
                .collect(Collectors.toList());

        List<SlaRiskResult> highRisks = calculatedRisks.stream()
                .filter(r -> r.getRiskScore() >= 60)
                .limit(6)
                .collect(Collectors.toList());

        // 2. Pending smart dispatches for unassigned work orders
        List<WorkOrder> unassigned = allWorkOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.NEW || wo.getTechnician() == null)
                .sorted(Comparator.comparing(WorkOrder::getCreatedAt).reversed())
                .limit(4)
                .collect(Collectors.toList());

        List<AssignmentRecommendation> recommendations = new ArrayList<>();
        for (WorkOrder wo : unassigned) {
            try {
                recommendations.add(assignmentService.getRecommendation(wo.getId()));
            } catch (Exception ignored) {}
        }

        // 3. Operational anomalies
        List<OperationalAnomaly> anomalies = anomalyService.detectAnomalies();

        int criticalCount = (int) calculatedRisks.stream().filter(r -> r.getRiskScore() >= 80).count();

        // 4. Phase 3: Active operational alerts
        List<AlertDTO> activeAlerts = alertService != null ? alertService.getActiveAlerts() : new ArrayList<>();

        // 5. Phase 3: Multi-stop sample route
        OptimizedRoute sampleRoute = null;
        List<RouteDecisionAudit> recentAudits = new ArrayList<>();
        try {
            List<Technician> techs = technicianRepository.findAll();
            if (!techs.isEmpty()) {
                Technician topTech = techs.stream()
                        .max(Comparator.comparingInt(t -> t.getActiveJobsCount() != null ? t.getActiveJobsCount() : 0))
                        .orElse(techs.get(0));
                sampleRoute = multiStopRouteService.getOptimizedRoute(topTech.getId());
                recentAudits = multiStopRouteService.getRouteAuditHistory(topTech.getId());
            }
        } catch (Exception ignored) {}

        // 6. Operations Health score calculation (0 - 100)
        int health = 100 - (criticalCount * 5) - (anomalies.size() * 3) - (activeAlerts.size() * 2);
        health = Math.max(45, Math.min(100, health));

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

        // 7. Phase 4: Predictive Operations Intelligence
        try {
            if (modelRegistryService != null) {
                overview.setModelRegistry(modelRegistryService.getAllRegisteredModels());
            }
            if (predictiveInventoryService != null) {
                List<com.fieldops.predictive.InventoryForecastDTO> reorders = predictiveInventoryService.getReplenishmentRecommendations();
                overview.setPredictedStockoutsCount((int) reorders.stream().filter(r -> r.getEstimatedDaysUntilStockout() <= 7).count());
                overview.setTopStockoutRisks(reorders.stream().limit(5).collect(Collectors.toList()));
            }
            if (predictiveSlaService != null) {
                List<com.fieldops.predictive.PredictiveSlaResultDTO> slaPreds = predictiveSlaService.evaluateAllActive();
                List<com.fieldops.predictive.PredictiveSlaResultDTO> breaches = slaPreds.stream()
                        .filter(p -> "LIKELY_BREACH".equals(p.getFinalAssessment()) || p.getMlBreachProbability() >= 0.65)
                        .sorted(Comparator.comparingDouble(com.fieldops.predictive.PredictiveSlaResultDTO::getMlBreachProbability).reversed())
                        .collect(Collectors.toList());
                overview.setPredictedBreachesCount(breaches.size());
                overview.setTopPredictedBreaches(breaches.stream().limit(5).collect(Collectors.toList()));
            }
        } catch (Exception ignored) {}

        return overview;
    }
}

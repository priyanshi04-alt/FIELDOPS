package com.fieldops.intelligence;

import com.fieldops.anomaly.OperationalAnomaly;
import com.fieldops.anomaly.OperationalAnomalyService;
import com.fieldops.assignment.AssignmentRecommendation;
import com.fieldops.assignment.TechnicianAssignmentService;
import com.fieldops.sla.SlaRiskResult;
import com.fieldops.sla.SlaRiskService;
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

    public OperationsIntelligenceService(SlaRiskService slaRiskService,
                                         TechnicianAssignmentService assignmentService,
                                         OperationalAnomalyService anomalyService,
                                         WorkOrderRepository workOrderRepository) {
        this.slaRiskService = slaRiskService;
        this.assignmentService = assignmentService;
        this.anomalyService = anomalyService;
        this.workOrderRepository = workOrderRepository;
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

        return new OperationsIntelligenceOverview(
                anomalies.size(),
                criticalCount,
                highRisks,
                anomalies,
                recommendations
        );
    }
}

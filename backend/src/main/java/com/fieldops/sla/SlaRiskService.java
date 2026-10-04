package com.fieldops.sla;

import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.routing.GeoUtils;
import com.fieldops.technician.Technician;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlaRiskService {

    private final WorkOrderRepository workOrderRepository;

    public SlaRiskService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public SlaRiskResult evaluateSlaRisk(Long workOrderId) {
        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + workOrderId));
        return calculateRisk(wo);
    }

    public SlaRiskResult calculateRisk(WorkOrder wo) {
        List<SlaRiskFactor> factors = new ArrayList<>();

        if (wo.getStatus() == WorkOrderStatus.COMPLETED) {
            return new SlaRiskResult(
                    wo.getId(),
                    wo.getWorkOrderNumber(),
                    0,
                    "LOW",
                    0L,
                    0,
                    0.0,
                    wo.getTechnician() != null ? wo.getTechnician().getName() : "None",
                    "Work order successfully resolved and verified.",
                    List.of(new SlaRiskFactor("TICKET_RESOLVED", 0, "Completed prior to closure."))
            );
        }

        // 1. Remaining SLA Time Analysis (Weight ~35%)
        long remainingMinutes = 120;
        if (wo.getSlaDeadline() != null) {
            Duration dur = Duration.between(LocalDateTime.now(), wo.getSlaDeadline());
            remainingMinutes = dur.toMinutes();
        }

        int slaImpact = 0;
        if (remainingMinutes <= 0) {
            slaImpact = 35;
            factors.add(new SlaRiskFactor("REMAINING_SLA", 35,
                    String.format("SLA deadline has expired (%d min elapsed past deadline). Immediate breach.", Math.abs(remainingMinutes))));
        } else if (remainingMinutes < 30) {
            slaImpact = 30;
            factors.add(new SlaRiskFactor("REMAINING_SLA", 30,
                    String.format("Critical remaining buffer: only %d min before contract breach.", remainingMinutes)));
        } else if (remainingMinutes < 60) {
            slaImpact = 20;
            factors.add(new SlaRiskFactor("REMAINING_SLA", 20,
                    String.format("Tight timeline: %d min remaining before SLA target.", remainingMinutes)));
        } else if (remainingMinutes < 120) {
            slaImpact = 10;
            factors.add(new SlaRiskFactor("REMAINING_SLA", 10,
                    String.format("Moderate buffer: %d min remaining.", remainingMinutes)));
        } else {
            slaImpact = 2;
            factors.add(new SlaRiskFactor("REMAINING_SLA", 2,
                    String.format("Comfortable SLA horizon: %d min remaining.", remainingMinutes)));
        }

        // 2. Distance and Transit Time Impact (Weight ~25%)
        Technician tech = wo.getTechnician();
        double distanceKm = 8.0;
        int etaMinutes = 20;

        if (tech != null && tech.getLatitude() != null && wo.getLatitude() != null) {
            distanceKm = GeoUtils.calculateHaversineDistanceKm(
                    tech.getLatitude(), tech.getLongitude(),
                    wo.getLatitude(), wo.getLongitude()
            );
            etaMinutes = GeoUtils.estimateTravelMinutes(distanceKm);
        } else if (wo.getDistanceKm() != null) {
            distanceKm = wo.getDistanceKm();
            etaMinutes = wo.getTechnicianEtaMinutes() != null ? wo.getTechnicianEtaMinutes() : GeoUtils.estimateTravelMinutes(distanceKm);
        }

        int distanceImpact = 0;
        if (tech == null) {
            distanceImpact = 25;
            factors.add(new SlaRiskFactor("UNASSIGNED_DISPATCH", 25,
                    "No technician currently assigned. Ticket accumulating idle dispatch latency."));
        } else {
            if (distanceKm > 20.0) {
                distanceImpact = 22;
                factors.add(new SlaRiskFactor("TRANSIT_DISTANCE", 22,
                        String.format("Long transit distance: %.1f km away with estimated %d min ETA.", distanceKm, etaMinutes)));
            } else if (distanceKm > 10.0) {
                distanceImpact = 15;
                factors.add(new SlaRiskFactor("TRANSIT_DISTANCE", 15,
                        String.format("Moderate distance: %.1f km away with %d min ETA.", distanceKm, etaMinutes)));
            } else if (distanceKm > 4.0) {
                distanceImpact = 8;
                factors.add(new SlaRiskFactor("TRANSIT_DISTANCE", 8,
                        String.format("Nearby dispatch: %.1f km away with %d min ETA.", distanceKm, etaMinutes)));
            } else {
                distanceImpact = 3;
                factors.add(new SlaRiskFactor("TRANSIT_DISTANCE", 3,
                        String.format("Immediate proximity: %.1f km away with quick %d min ETA.", distanceKm, etaMinutes)));
            }
        }

        // 3. Workload and Active Backlog (Weight ~15%)
        int workloadImpact = 0;
        if (tech != null) {
            int activeJobs = tech.getActiveJobsCount() != null ? tech.getActiveJobsCount() : 1;
            if (activeJobs >= 4) {
                workloadImpact = 15;
                factors.add(new SlaRiskFactor("TECHNICIAN_BACKLOG", 15,
                        String.format("Assigned engineer %s has %d concurrent active jobs.", tech.getName(), activeJobs)));
            } else if (activeJobs >= 2) {
                workloadImpact = 8;
                factors.add(new SlaRiskFactor("TECHNICIAN_BACKLOG", 8,
                        String.format("Engineer %s currently handling %d active work orders.", tech.getName(), activeJobs)));
            } else {
                workloadImpact = 2;
                factors.add(new SlaRiskFactor("TECHNICIAN_BACKLOG", 2,
                        String.format("Engineer %s has low queue backlog (%d active job).", tech.getName(), activeJobs)));
            }
        } else {
            workloadImpact = 10;
        }

        // 4. Ticket Priority Factor (Weight ~15%)
        int priorityImpact = 0;
        if (wo.getPriority() == WorkOrderPriority.CRITICAL) {
            priorityImpact = 15;
            factors.add(new SlaRiskFactor("SEVERITY_LEVEL", 15,
                    "CRITICAL severity carries high operational penalty and strict escalation rules."));
        } else if (wo.getPriority() == WorkOrderPriority.HIGH) {
            priorityImpact = 10;
            factors.add(new SlaRiskFactor("SEVERITY_LEVEL", 10,
                    "HIGH priority tier with fast resolution requirement."));
        } else if (wo.getPriority() == WorkOrderPriority.MEDIUM) {
            priorityImpact = 5;
            factors.add(new SlaRiskFactor("SEVERITY_LEVEL", 5,
                    "MEDIUM priority tier. Standard business workflow."));
        } else {
            priorityImpact = 2;
            factors.add(new SlaRiskFactor("SEVERITY_LEVEL", 2,
                    "LOW priority standard maintenance request."));
        }

        // 5. Historical Performance & Duration (Weight ~10%)
        int performanceImpact = 0;
        double estHours = wo.getEstimatedDurationHours() != null ? wo.getEstimatedDurationHours() : 2.0;
        if (estHours >= 3.0) {
            performanceImpact = 10;
            factors.add(new SlaRiskFactor("ESTIMATED_DURATION", 10,
                    String.format("Complex job scope with estimated duration of %.1f hours.", estHours)));
        } else if (estHours >= 2.0) {
            performanceImpact = 5;
            factors.add(new SlaRiskFactor("ESTIMATED_DURATION", 5,
                    String.format("Standard task scope of %.1f hours.", estHours)));
        } else {
            performanceImpact = 2;
            factors.add(new SlaRiskFactor("ESTIMATED_DURATION", 2,
                    String.format("Rapid service scope of %.1f hours.", estHours)));
        }

        int totalRisk = slaImpact + distanceImpact + workloadImpact + priorityImpact + performanceImpact;
        totalRisk = Math.min(100, Math.max(0, totalRisk));

        String riskLevel = "LOW";
        if (totalRisk >= 80) riskLevel = "CRITICAL";
        else if (totalRisk >= 60) riskLevel = "HIGH";
        else if (totalRisk >= 30) riskLevel = "MEDIUM";

        String summary = String.format("Risk is %s (%d%%). %s",
                riskLevel,
                totalRisk,
                totalRisk >= 60 ? "Immediate dispatcher intervention or expedited rerouting recommended." : "Operations proceeding within nominal SLAs."
        );

        return new SlaRiskResult(
                wo.getId(),
                wo.getWorkOrderNumber(),
                totalRisk,
                riskLevel,
                remainingMinutes,
                etaMinutes,
                distanceKm,
                tech != null ? tech.getName() : "Unassigned",
                summary,
                factors
        );
    }
}

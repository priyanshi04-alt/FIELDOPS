package com.fieldops.routing.multistop;

import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.websocket.OperationsEvent;
import com.fieldops.websocket.OperationsEventPublisher;
import com.fieldops.websocket.OperationsEventType;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultiStopRouteService {

    private final TechnicianRepository technicianRepository;
    private final WorkOrderRepository workOrderRepository;
    private final RouteOptimizationService optimizationService;
    private final RouteDecisionAuditRepository auditRepository;
    private final OperationsEventPublisher eventPublisher;

    public MultiStopRouteService(TechnicianRepository technicianRepository,
                                 WorkOrderRepository workOrderRepository,
                                 RouteOptimizationService optimizationService,
                                 RouteDecisionAuditRepository auditRepository,
                                 OperationsEventPublisher eventPublisher) {
        this.technicianRepository = technicianRepository;
        this.workOrderRepository = workOrderRepository;
        this.optimizationService = optimizationService;
        this.auditRepository = auditRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public OptimizedRoute getOptimizedRoute(Long technicianId) {
        Technician tech = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        return computeOptimizedRoute(tech, workOrderRepository.findAll(), LocalDateTime.now());
    }

    /**
     * Stage B in-memory route optimization.
     * Computes multi-stop route optimization using 2-Opt and Nearest Neighbor algorithms
     * strictly outside database transactions.
     */
    public OptimizedRoute computeOptimizedRoute(Technician tech, List<WorkOrder> allWorkOrders, LocalDateTime departureTime) {
        if (tech == null) return null;
        List<WorkOrder> assignedJobs = filterActiveJobsForTechnician(tech, allWorkOrders);
        return optimizationService.optimizeRoute(tech, assignedJobs, departureTime != null ? departureTime : LocalDateTime.now());
    }

    @Transactional
    public OptimizedRoute optimizeAndAuditRoute(Long technicianId, String dispatcherEmail) {
        Technician tech = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        List<WorkOrder> assignedJobs = findActiveJobsForTechnician(technicianId);
        String previousSequence = assignedJobs.stream()
                .map(WorkOrder::getWorkOrderNumber)
                .collect(Collectors.joining(" → "));

        OptimizedRoute route = optimizationService.optimizeRoute(tech, assignedJobs, LocalDateTime.now());

        String optimizedSequence = route.getStops().stream()
                .map(RouteStop::getWorkOrderNumber)
                .collect(Collectors.joining(" → "));

        // Record audit entry
        RouteDecisionAudit audit = RouteDecisionAudit.builder()
                .technicianId(tech.getId())
                .technicianName(tech.getName())
                .dispatcherEmail(dispatcherEmail != null ? dispatcherEmail : "dispatcher@fieldops.demo")
                .jobCount(route.getTotalStops())
                .previousOrder(previousSequence)
                .optimizedOrder(optimizedSequence)
                .algorithm("SLA-Weighted Nearest Neighbor + 2-Opt Improvement")
                .optimizationScore(route.getOptimizationScore())
                .totalDistanceKm(route.getTotalDistanceKm())
                .totalTravelTimeMinutes(route.getTotalTravelTimeMinutes())
                .distanceSavedKm(route.getDistanceSavedKm())
                .timeSavedMinutes(route.getTimeSavedMinutes())
                .slaAtRiskCount(route.getSlaAtRiskCount())
                .slaViolationCount(route.getSlaViolationCount())
                .action("OPTIMIZED")
                .explanation(route.getExplanation())
                .build();
        auditRepository.save(audit);

        // Publish WebSocket notification
        Map<String, Object> payload = new HashMap<>();
        payload.put("technicianId", tech.getId());
        payload.put("stopsCount", route.getTotalStops());
        payload.put("distanceKm", route.getTotalDistanceKm());
        payload.put("timeSavedMin", route.getTimeSavedMinutes());

        eventPublisher.publish(OperationsEvent.builder()
                .eventType(OperationsEventType.ROUTE_OPTIMIZED)
                .severity("INFO")
                .message(String.format("Optimized %d-stop itinerary calculated for %s (Saved %.1f km)",
                        route.getTotalStops(), tech.getName(), route.getDistanceSavedKm()))
                .technicianId(tech.getId())
                .payload(payload)
                .build());

        return route;
    }

    @Transactional
    public OptimizedRoute applyOptimizedRoute(Long technicianId, String dispatcherEmail) {
        Technician tech = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        List<WorkOrder> assignedJobs = findActiveJobsForTechnician(technicianId);
        OptimizedRoute route = optimizationService.optimizeRoute(tech, assignedJobs, LocalDateTime.now());
        route.setIsApplied(true);

        // Apply updated schedule to work orders
        for (RouteStop stop : route.getStops()) {
            WorkOrder wo = workOrderRepository.findById(stop.getWorkOrderId()).orElse(null);
            if (wo != null) {
                wo.setScheduledStartTime(stop.getEstimatedArrival());
                wo.setScheduledEndTime(stop.getEstimatedDeparture());
                wo.setTechnicianEtaMinutes(stop.getTravelTimeFromPreviousMinutes());
                wo.setDistanceKm(stop.getDistanceFromPreviousKm());
                if (stop.getSequenceNumber() == 1 && tech.getStatus() == TechnicianStatus.AVAILABLE) {
                    tech.setStatus(TechnicianStatus.ON_ROUTE);
                    technicianRepository.save(tech);
                }
                workOrderRepository.save(wo);
            }
        }

        String optimizedSequence = route.getStops().stream()
                .map(RouteStop::getWorkOrderNumber)
                .collect(Collectors.joining(" → "));

        // Audit Application
        RouteDecisionAudit audit = RouteDecisionAudit.builder()
                .technicianId(tech.getId())
                .technicianName(tech.getName())
                .dispatcherEmail(dispatcherEmail != null ? dispatcherEmail : "dispatcher@fieldops.demo")
                .jobCount(route.getTotalStops())
                .optimizedOrder(optimizedSequence)
                .algorithm("SLA-Weighted Nearest Neighbor + 2-Opt Improvement")
                .optimizationScore(route.getOptimizationScore())
                .totalDistanceKm(route.getTotalDistanceKm())
                .totalTravelTimeMinutes(route.getTotalTravelTimeMinutes())
                .distanceSavedKm(route.getDistanceSavedKm())
                .timeSavedMinutes(route.getTimeSavedMinutes())
                .slaAtRiskCount(route.getSlaAtRiskCount())
                .slaViolationCount(route.getSlaViolationCount())
                .action("APPLIED")
                .explanation("Dispatcher explicitly approved and scheduled the multi-stop route sequence.")
                .build();
        auditRepository.save(audit);

        // Publish WebSocket Event
        eventPublisher.publish(OperationsEvent.builder()
                .eventType(OperationsEventType.ROUTE_APPLIED)
                .severity("INFO")
                .message(String.format("Applied route sequence for %s: %s", tech.getName(), optimizedSequence))
                .technicianId(tech.getId())
                .build());

        return route;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRouteSummary(Long technicianId) {
        Technician tech = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        OptimizedRoute route = getOptimizedRoute(technicianId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("technicianId", tech.getId());
        summary.put("technicianName", tech.getName());
        summary.put("status", tech.getStatus());
        summary.put("totalStops", route.getTotalStops());
        summary.put("totalDistanceKm", route.getTotalDistanceKm());
        summary.put("totalDurationMinutes", route.getTotalDurationMinutes());
        summary.put("projectedCompletionTime", route.getProjectedCompletionTime());
        summary.put("slaCompliantCount", route.getSlaCompliantCount());
        summary.put("slaAtRiskCount", route.getSlaAtRiskCount());
        summary.put("slaViolationCount", route.getSlaViolationCount());
        summary.put("score", route.getOptimizationScore());
        summary.put("distanceSavedKm", route.getDistanceSavedKm());
        summary.put("timeSavedMinutes", route.getTimeSavedMinutes());
        return summary;
    }

    @Transactional(readOnly = true)
    public List<RouteDecisionAudit> getRouteAuditHistory(Long technicianId) {
        return auditRepository.findByTechnicianIdOrderByTimestampDesc(technicianId);
    }

    /**
     * Finds active assigned jobs for the technician, or if the technician has < 2 jobs,
     * additionally borrows pending/unassigned jobs in the technician's home city so the dispatcher
     * can evaluate an optimal candidate bundle.
     */
    private List<WorkOrder> findActiveJobsForTechnician(Long technicianId) {
        Technician tech = technicianRepository.findById(technicianId).orElse(null);
        if (tech == null) return Collections.emptyList();
        return filterActiveJobsForTechnician(tech, workOrderRepository.findAll());
    }

    /**
     * Stage B in-memory candidate job filtering for route optimization.
     */
    public List<WorkOrder> filterActiveJobsForTechnician(Technician tech, List<WorkOrder> allWorkOrders) {
        if (tech == null || allWorkOrders == null) return Collections.emptyList();
        Long technicianId = tech.getId();

        List<WorkOrder> directlyAssigned = allWorkOrders.stream()
                .filter(wo -> wo.getTechnician() != null && wo.getTechnician().getId() != null && wo.getTechnician().getId().equals(technicianId))
                .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED)
                .collect(Collectors.toList());

        if (directlyAssigned.size() >= 2) {
            return directlyAssigned;
        }

        // If directly assigned jobs are sparse, include nearby open/unassigned jobs in the same city
        List<WorkOrder> result = new ArrayList<>(directlyAssigned);
        List<WorkOrder> unassignedInCity = allWorkOrders.stream()
                .filter(wo -> wo.getTechnician() == null || wo.getStatus() == WorkOrderStatus.NEW)
                .filter(wo -> wo.getCity() != null && tech.getCity() != null && wo.getCity().equalsIgnoreCase(tech.getCity()))
                .filter(wo -> !result.contains(wo))
                .limit(Math.max(0, 4 - result.size()))
                .collect(Collectors.toList());

        result.addAll(unassignedInCity);
        return result;
    }
}

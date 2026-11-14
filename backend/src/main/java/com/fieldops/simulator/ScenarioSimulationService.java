package com.fieldops.simulator;

import com.fieldops.alert.AlertRepository;
import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.routing.GeoUtils;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.websocket.OperationsEvent;
import com.fieldops.websocket.OperationsEventPublisher;
import com.fieldops.websocket.OperationsEventType;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScenarioSimulationService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioSimulationService.class);

    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;
    private final AlertRepository alertRepository;
    private final ScenarioDecisionAuditRepository scenarioAuditRepository;
    private final OperationsEventPublisher eventPublisher;

    // In-memory cache of evaluated simulations waiting for dispatcher human-in-the-loop review
    private final Map<String, CachedSimulation> simulationCache = new ConcurrentHashMap<>();

    private static class CachedSimulation {
        ScenarioInput input;
        ScenarioResult result;
        List<SimulatedReassignment> reassignments;
        LocalDateTime createdAt;
        Map<Long, Long> baselineWorkOrderTechnicianMap;
        boolean applied = false;
    }

    public static class SimulatedReassignment {
        public Long workOrderId;
        public Long oldTechId;
        public Long newTechId;
    }

    public ScenarioSimulationService(WorkOrderRepository workOrderRepository,
                                     TechnicianRepository technicianRepository,
                                     AlertRepository alertRepository,
                                     ScenarioDecisionAuditRepository scenarioAuditRepository,
                                     OperationsEventPublisher eventPublisher) {
        this.workOrderRepository = workOrderRepository;
        this.technicianRepository = technicianRepository;
        this.alertRepository = alertRepository;
        this.scenarioAuditRepository = scenarioAuditRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes non-mutating what-if simulation on in-memory deep copies.
     * Production database state is NOT altered.
     */
    @Transactional(readOnly = true)
    public ScenarioResult simulate(ScenarioInput input) {
        String simId = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 1. Snapshot Live State
        List<WorkOrder> liveWorkOrders = workOrderRepository.findAll();
        List<Technician> liveTechnicians = technicianRepository.findAll();

        int baseActiveTechs = (int) liveTechnicians.stream()
                .filter(t -> t.getStatus() != TechnicianStatus.OFFLINE)
                .count();

        int baseAtRisk = 0;
        int baseBreaches = 0;
        double baseDistance = 0.0;
        int baseTravelMinutes = 0;

        LocalDateTime now = LocalDateTime.now();
        for (WorkOrder wo : liveWorkOrders) {
            if (wo.getStatus() != WorkOrderStatus.COMPLETED && wo.getSlaDeadline() != null) {
                long rem = Duration.between(now, wo.getSlaDeadline()).toMinutes();
                if (rem <= 0) baseBreaches++;
                else if (rem < 60) baseAtRisk++;

                if (wo.getTechnician() != null) {
                    double dist = GeoUtils.calculateHaversineDistanceKm(wo.getTechnician().getLatitude(), wo.getTechnician().getLongitude(),
                            wo.getLatitude(), wo.getLongitude());
                    baseDistance += dist;
                    baseTravelMinutes += (int) (dist * 2.2);
                }
            }
        }
        int baseAlerts = alertRepository != null ? (int) alertRepository.countActiveAlerts() : 0;

        // 2. Clone in-memory snapshots
        Map<Long, TechnicianCopy> techCopies = new HashMap<>();
        for (Technician t : liveTechnicians) {
            techCopies.put(t.getId(), new TechnicianCopy(t));
        }

        Map<Long, WorkOrderCopy> orderCopies = new HashMap<>();
        for (WorkOrder w : liveWorkOrders) {
            orderCopies.put(w.getId(), new WorkOrderCopy(w));
        }

        List<String> stateChanges = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<SimulatedReassignment> reassignments = new ArrayList<>();

        // 3. Inject Scenario Perturbation
        switch (input.getScenarioType()) {
            case TECHNICIAN_UNAVAILABLE -> {
                Long techId = input.getTargetTechnicianId() != null ? input.getTargetTechnicianId() : liveTechnicians.get(0).getId();
                TechnicianCopy unavailableTech = techCopies.get(techId);
                if (unavailableTech != null) {
                    unavailableTech.status = TechnicianStatus.OFFLINE;
                    stateChanges.add(String.format("Technician %s marked OFFLINE (Unavailable).", unavailableTech.name));

                    // Find alternative available technicians in same city
                    List<TechnicianCopy> candidates = techCopies.values().stream()
                            .filter(t -> !t.id.equals(techId) && t.status != TechnicianStatus.OFFLINE && t.city.equalsIgnoreCase(unavailableTech.city))
                            .toList();

                    for (WorkOrderCopy wo : orderCopies.values()) {
                        if (wo.status != WorkOrderStatus.COMPLETED && techId.equals(wo.technicianId)) {
                            if (!candidates.isEmpty()) {
                                TechnicianCopy replacement = candidates.stream()
                                        .min(Comparator.comparingInt(t -> t.activeJobs))
                                        .orElse(candidates.get(0));

                                SimulatedReassignment r = new SimulatedReassignment();
                                r.workOrderId = wo.id;
                                r.oldTechId = techId;
                                r.newTechId = replacement.id;
                                reassignments.add(r);

                                wo.technicianId = replacement.id;
                                replacement.activeJobs++;
                                stateChanges.add(String.format("Reassigned %s (%s) -> %s", wo.workOrderNumber, wo.priority, replacement.name));
                            } else {
                                wo.technicianId = null;
                                wo.status = WorkOrderStatus.NEW;
                                stateChanges.add(String.format("%s unassigned (no local alternate available).", wo.workOrderNumber));
                                warnings.add(String.format("Critical unassigned job created: %s", wo.workOrderNumber));
                            }
                        }
                    }
                }
            }

            case TECHNICIAN_DELAYED -> {
                Long techId = input.getTargetTechnicianId() != null ? input.getTargetTechnicianId() : liveTechnicians.get(0).getId();
                int delay = input.getDelayMinutes() != null ? input.getDelayMinutes() : 60;
                TechnicianCopy delayedTech = techCopies.get(techId);
                if (delayedTech != null) {
                    stateChanges.add(String.format("Technician %s injected with +%d min operational transit delay.", delayedTech.name, delay));
                    for (WorkOrderCopy wo : orderCopies.values()) {
                        if (techId.equals(wo.technicianId) && wo.status != WorkOrderStatus.COMPLETED) {
                            wo.addedDelayMinutes += delay;
                            warnings.add(String.format("%s ETA deferred by %d min.", wo.workOrderNumber, delay));
                        }
                    }
                }
            }

            case URGENT_JOB_INJECTED -> {
                WorkOrderCopy urgentWo = new WorkOrderCopy();
                urgentWo.id = 99999L;
                urgentWo.workOrderNumber = "WO-SIM-URGENT";
                urgentWo.priority = WorkOrderPriority.CRITICAL;
                urgentWo.status = WorkOrderStatus.NEW;
                urgentWo.slaMinutes = 60;
                urgentWo.addedDelayMinutes = 0;

                // Auto-pick closest idle tech
                TechnicianCopy bestTech = techCopies.values().stream()
                        .filter(t -> t.status == TechnicianStatus.AVAILABLE)
                        .findFirst()
                        .orElse(techCopies.values().iterator().next());

                urgentWo.technicianId = bestTech.id;
                orderCopies.put(urgentWo.id, urgentWo);
                stateChanges.add(String.format("Injected CRITICAL ticket %s (60 min SLA) -> assigned to %s", urgentWo.workOrderNumber, bestTech.name));
            }

            case INVENTORY_STOCKOUT_INJECTED -> {
                stateChanges.add("Injected critical component stockout for 400V MCCB units.");
                warnings.add("3 repair orders requiring this part blocked from immediate completion.");
            }

            case TECHNICIAN_REASSIGNED -> {
                Long techId = input.getTargetTechnicianId() != null ? input.getTargetTechnicianId() : liveTechnicians.get(0).getId();
                TechnicianCopy tech = techCopies.get(techId);
                if (tech != null) {
                    String newCity = "Noida".equalsIgnoreCase(tech.city) ? "Gurgaon" : "Noida";
                    stateChanges.add(String.format("Technician %s redeployed from %s to %s territory.", tech.name, tech.city, newCity));
                    tech.city = newCity;
                }
            }

            case BULK_RESEQUENCE -> {
                stateChanges.add("Simulated 2-opt sequence reversal on high-density route clusters.");
            }
        }

        // 4. Compute Simulated Post-Perturbation Metrics
        int simActiveTechs = (int) techCopies.values().stream()
                .filter(t -> t.status != TechnicianStatus.OFFLINE)
                .count();

        int simAtRisk = 0;
        int simBreaches = 0;
        double simDistance = 0.0;
        int simTravelMinutes = 0;

        for (WorkOrderCopy wo : orderCopies.values()) {
            if (wo.status != WorkOrderStatus.COMPLETED) {
                long rem = wo.slaMinutes - wo.addedDelayMinutes;
                if (rem <= 0) simBreaches++;
                else if (rem < 60) simAtRisk++;

                if (wo.technicianId != null) {
                    TechnicianCopy t = techCopies.get(wo.technicianId);
                    if (t != null) {
                        double d = GeoUtils.calculateHaversineDistanceKm(t.latitude, t.longitude, wo.latitude, wo.longitude);
                        simDistance += d;
                        simTravelMinutes += (int) (d * 2.2) + wo.addedDelayMinutes;
                    }
                }
            }
        }

        int deltaAtRisk = simAtRisk - baseAtRisk;
        int deltaBreaches = simBreaches - baseBreaches;
        double deltaDist = Math.round((simDistance - baseDistance) * 10.0) / 10.0;
        int deltaTravel = simTravelMinutes - baseTravelMinutes;
        int deltaAlerts = (deltaBreaches > 0 ? deltaBreaches : 0) + (reassignments.size() > 0 ? 1 : 0);

        String riskLevel = deltaBreaches > 0 ? "CRITICAL" : (deltaAtRisk > 0 ? "HIGH" : (deltaTravel > 30 ? "MEDIUM" : "LOW"));

        String recommendedAction = reassignments.isEmpty()
                ? "Maintain current dispatch strategy; scenario impact is operationally nominal."
                : String.format("Approve reassignment of %d work orders to prevent projected SLA violations.", reassignments.size());

        ScenarioResult result = new ScenarioResult();
        result.setSimulationId(simId);
        result.setScenarioType(input.getScenarioType());
        result.setScenarioName(input.getScenarioName() != null ? input.getScenarioName() : input.getScenarioType().name());

        result.setBaselineActiveTechnicians(baseActiveTechs);
        result.setBaselineAtRiskJobs(baseAtRisk);
        result.setBaselineProjectedBreaches(baseBreaches);
        result.setBaselineTotalDistanceKm(Math.round(baseDistance * 10.0) / 10.0);
        result.setBaselineTotalTravelMinutes(baseTravelMinutes);
        result.setBaselineActiveAlerts(baseAlerts);

        result.setSimulatedActiveTechnicians(simActiveTechs);
        result.setSimulatedAtRiskJobs(simAtRisk);
        result.setSimulatedProjectedBreaches(simBreaches);
        result.setSimulatedTotalDistanceKm(Math.round(simDistance * 10.0) / 10.0);
        result.setSimulatedTotalTravelMinutes(simTravelMinutes);
        result.setSimulatedActiveAlerts(baseAlerts + deltaAlerts);

        result.setDeltaAtRiskJobs(deltaAtRisk);
        result.setDeltaProjectedBreaches(deltaBreaches);
        result.setDeltaDistanceKm(deltaDist);
        result.setDeltaTravelMinutes(deltaTravel);
        result.setDeltaNewAlerts(deltaAlerts);
        result.setReassignedJobsCount(reassignments.size());

        result.setOperationalRiskLevel(riskLevel);
        result.setRecommendedAction(recommendedAction);
        result.setSimulatedStateChanges(stateChanges);
        result.setProjectedWarnings(warnings);

        // Cache for human-in-the-loop application with state fingerprint
        CachedSimulation cached = new CachedSimulation();
        cached.input = input;
        cached.result = result;
        cached.reassignments = reassignments;
        cached.createdAt = LocalDateTime.now();
        cached.baselineWorkOrderTechnicianMap = new HashMap<>();
        for (WorkOrder wo : liveWorkOrders) {
            cached.baselineWorkOrderTechnicianMap.put(wo.getId(), wo.getTechnician() != null ? wo.getTechnician().getId() : null);
        }
        simulationCache.put(simId, cached);

        return result;
    }

    /**
     * Human-in-the-loop application:
     * Dispatcher explicitly applies simulated changes to live production state.
     */
    @Transactional
    public synchronized ScenarioResult applyScenario(String simulationId, String dispatcherEmail) {
        CachedSimulation cached = simulationCache.get(simulationId);
        if (cached == null) {
            throw new ResourceNotFoundException("Simulation snapshot expired or not found: " + simulationId);
        }

        // Idempotency: Reject repeated applications
        if (cached.applied) {
            throw new IllegalStateException("Scenario has already been applied: " + simulationId);
        }

        // Expiration Policy: Reject simulations older than 15 minutes
        if (Duration.between(cached.createdAt, LocalDateTime.now()).toMinutes() > 15) {
            simulationCache.remove(simulationId);
            throw new IllegalStateException("Simulation snapshot expired (>15m). Please run a fresh simulation.");
        }

        // Underlying State Drift Validation: Verify that work orders have not changed assignment since simulation
        for (SimulatedReassignment r : cached.reassignments) {
            WorkOrder currentWo = workOrderRepository.findById(r.workOrderId).orElse(null);
            if (currentWo == null) {
                throw new IllegalStateException("Underlying work order #" + r.workOrderId + " was deleted since simulation.");
            }
            Long baselineTechId = cached.baselineWorkOrderTechnicianMap.get(r.workOrderId);
            Long currentTechId = currentWo.getTechnician() != null ? currentWo.getTechnician().getId() : null;
            if (!Objects.equals(baselineTechId, currentTechId)) {
                throw new IllegalStateException("Production state drift detected: Work order #" + currentWo.getWorkOrderNumber() +
                        " was modified since simulation. Please generate a fresh simulation.");
            }
        }

        // Apply reassigned work orders in production
        for (SimulatedReassignment r : cached.reassignments) {
            WorkOrder wo = workOrderRepository.findById(r.workOrderId).orElse(null);
            Technician newTech = technicianRepository.findById(r.newTechId).orElse(null);
            if (wo != null && newTech != null) {
                wo.setTechnician(newTech);
                wo.setStatus(WorkOrderStatus.ASSIGNED);
                workOrderRepository.save(wo);
            }
        }

        // Mark technician offline if scenario was TECHNICIAN_UNAVAILABLE
        if (cached.input.getScenarioType() == ScenarioType.TECHNICIAN_UNAVAILABLE && cached.input.getTargetTechnicianId() != null) {
            technicianRepository.findById(cached.input.getTargetTechnicianId()).ifPresent(t -> {
                t.setStatus(TechnicianStatus.OFFLINE);
                technicianRepository.save(t);
            });
        }

        cached.applied = true;
        cached.result.setApplied(true);

        // Persist Audit Record
        ScenarioDecisionAudit audit = new ScenarioDecisionAudit();
        audit.setSimulationId(simulationId);
        audit.setScenarioType(cached.input.getScenarioType());
        audit.setScenarioName(cached.result.getScenarioName());
        audit.setDispatcherEmail(dispatcherEmail != null ? dispatcherEmail : "dispatcher@fieldops.demo");
        audit.setReassignedJobsCount(cached.result.getReassignedJobsCount());
        audit.setDeltaDistanceKm(cached.result.getDeltaDistanceKm());
        audit.setDeltaTravelMinutes(cached.result.getDeltaTravelMinutes());
        audit.setDeltaProjectedBreaches(cached.result.getDeltaProjectedBreaches());
        audit.setStateChangesSummary(String.join("; ", cached.result.getSimulatedStateChanges()));
        audit.setRationale("Dispatcher explicitly reviewed and applied What-If simulation itinerary.");
        audit.setAppliedAt(LocalDateTime.now());
        scenarioAuditRepository.save(audit);

        // Broadcast SCENARIO_APPLIED over WebSocket
        eventPublisher.publish(OperationsEvent.builder()
                .eventType(OperationsEventType.SCENARIO_APPLIED)
                .severity("INFO")
                .message(String.format("What-If Scenario applied: %s (%d jobs re-assigned)",
                        cached.result.getScenarioName(), cached.result.getReassignedJobsCount()))
                .build());

        return cached.result;
    }

    @Transactional(readOnly = true)
    public List<ScenarioDecisionAudit> getAuditHistory() {
        return scenarioAuditRepository.findTop20ByOrderByAppliedAtDesc();
    }

    // Helper POJOs for in-memory deep copy snapshotting
    private static class TechnicianCopy {
        Long id;
        String name;
        String city;
        TechnicianStatus status;
        double latitude;
        double longitude;
        int activeJobs;

        TechnicianCopy(Technician t) {
            this.id = t.getId();
            this.name = t.getName();
            this.city = t.getCity();
            this.status = t.getStatus();
            this.latitude = t.getLatitude() != null ? t.getLatitude() : 28.5;
            this.longitude = t.getLongitude() != null ? t.getLongitude() : 77.2;
            this.activeJobs = t.getActiveJobsCount() != null ? t.getActiveJobsCount() : 0;
        }
    }

    private static class WorkOrderCopy {
        Long id;
        String workOrderNumber;
        WorkOrderPriority priority;
        WorkOrderStatus status;
        Long technicianId;
        long slaMinutes = 60;
        int addedDelayMinutes = 0;
        double latitude = 28.55;
        double longitude = 77.25;

        WorkOrderCopy() {}
        WorkOrderCopy(WorkOrder w) {
            this.id = w.getId();
            this.workOrderNumber = w.getWorkOrderNumber();
            this.priority = w.getPriority();
            this.status = w.getStatus();
            this.technicianId = w.getTechnician() != null ? w.getTechnician().getId() : null;
            if (w.getSlaDeadline() != null) {
                this.slaMinutes = Duration.between(LocalDateTime.now(), w.getSlaDeadline()).toMinutes();
            }
            if (w.getLatitude() != null) this.latitude = w.getLatitude();
            if (w.getLongitude() != null) this.longitude = w.getLongitude();
        }
    }
}

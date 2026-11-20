package com.fieldops.intelligence;

import com.fieldops.alert.AlertDTO;
import com.fieldops.alert.AlertService;
import com.fieldops.customer.Customer;
import com.fieldops.inventory.InventoryItem;
import com.fieldops.inventory.InventoryItemRepository;
import com.fieldops.predictive.ModelRegistryEntry;
import com.fieldops.predictive.ModelRegistryService;
import com.fieldops.routing.multistop.RouteDecisionAudit;
import com.fieldops.routing.multistop.RouteDecisionAuditRepository;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stage A: Dedicated Spring-managed service executing all database retrieval operations
 * within a strictly bounded, read-only transaction.
 * Immediately converts JPA entities to detached snapshot instances and commits/closes
 * the transaction so that the database connection is returned to the HikariCP pool
 * prior to any CPU-bound analytical or machine learning computation.
 */
@Service
public class OperationsSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(OperationsSnapshotService.class);

    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final AlertService alertService;
    private final RouteDecisionAuditRepository auditRepository;
    private final ModelRegistryService modelRegistryService;

    public OperationsSnapshotService(WorkOrderRepository workOrderRepository,
                                     TechnicianRepository technicianRepository,
                                     InventoryItemRepository inventoryItemRepository,
                                     AlertService alertService,
                                     RouteDecisionAuditRepository auditRepository,
                                     ModelRegistryService modelRegistryService) {
        this.workOrderRepository = workOrderRepository;
        this.technicianRepository = technicianRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.alertService = alertService;
        this.auditRepository = auditRepository;
        this.modelRegistryService = modelRegistryService;
    }

    @Transactional(readOnly = true)
    public OperationsDataSnapshot captureSnapshot() {
        long tStart = System.nanoTime();

        // 1. Minimum batched repository retrievals
        List<WorkOrder> rawWorkOrders = workOrderRepository.findAll();
        List<Technician> rawTechnicians = technicianRepository.findAll();
        List<InventoryItem> rawInventory = inventoryItemRepository.findAll();
        List<AlertDTO> activeAlerts = alertService != null ? alertService.getActiveAlerts() : Collections.emptyList();

        // 2. Clone technicians into detached POJOs with eager skills collection initialized
        List<Technician> detachedTechnicians = rawTechnicians.stream()
                .map(this::detachTechnician)
                .collect(Collectors.toList());

        Map<Long, Technician> techMap = detachedTechnicians.stream()
                .filter(t -> t.getId() != null)
                .collect(Collectors.toMap(Technician::getId, t -> t, (a, b) -> a));

        // 3. Clone work orders into detached POJOs with associations mapped to detached instances
        List<WorkOrder> detachedWorkOrders = rawWorkOrders.stream()
                .map(wo -> detachWorkOrder(wo, techMap))
                .collect(Collectors.toList());

        // 4. Clone inventory items into detached POJOs
        List<InventoryItem> detachedInventory = rawInventory.stream()
                .map(this::detachInventoryItem)
                .collect(Collectors.toList());

        // 5. Identify top technician and fetch recent route decision audits
        Long topTechId = null;
        Technician topTech = null;
        if (!detachedTechnicians.isEmpty()) {
            topTech = detachedTechnicians.stream()
                    .max(Comparator.comparingInt(t -> t.getActiveJobsCount() != null ? t.getActiveJobsCount() : 0))
                    .orElse(detachedTechnicians.get(0));
            topTechId = topTech.getId();
        }

        List<RouteDecisionAudit> recentAudits = Collections.emptyList();
        if (topTechId != null && auditRepository != null) {
            try {
                recentAudits = auditRepository.findByTechnicianIdOrderByTimestampDesc(topTechId);
            } catch (Exception ex) {
                log.warn("Could not retrieve route audit history for tech {}: {}", topTechId, ex.getMessage());
            }
        }

        // 6. Registered models retrieval
        Map<String, ModelRegistryEntry> registeredModels = Collections.emptyMap();
        if (modelRegistryService != null) {
            try {
                registeredModels = modelRegistryService.getAllRegisteredModels();
            } catch (Exception ex) {
                log.warn("Could not retrieve model registry: {}", ex.getMessage());
            }
        }

        long durationMs = (System.nanoTime() - tStart) / 1_000_000;
        log.debug("Operations snapshot captured in {} ms ({} WOs, {} Techs, {} Items)",
                durationMs, detachedWorkOrders.size(), detachedTechnicians.size(), detachedInventory.size());

        return new OperationsDataSnapshot(
                detachedWorkOrders,
                detachedTechnicians,
                detachedInventory,
                activeAlerts,
                recentAudits,
                registeredModels,
                topTechId,
                topTech,
                durationMs,
                durationMs
        );
    }

    private Technician detachTechnician(Technician t) {
        if (t == null) return null;
        return Technician.builder()
                .id(t.getId())
                .name(t.getName())
                .email(t.getEmail())
                .phone(t.getPhone())
                .avatarUrl(t.getAvatarUrl())
                .status(t.getStatus())
                .city(t.getCity())
                .address(t.getAddress())
                .latitude(t.getLatitude())
                .longitude(t.getLongitude())
                .rating(t.getRating())
                .completedJobs(t.getCompletedJobs())
                .slaSuccessRate(t.getSlaSuccessRate())
                .activeJobsCount(t.getActiveJobsCount())
                .utilizationRate(t.getUtilizationRate())
                .skills(t.getSkills() != null ? new HashSet<>(t.getSkills()) : new HashSet<>())
                .build();
    }

    private WorkOrder detachWorkOrder(WorkOrder w, Map<Long, Technician> techMap) {
        if (w == null) return null;
        Technician detachedTech = null;
        if (w.getTechnician() != null && w.getTechnician().getId() != null) {
            detachedTech = techMap.get(w.getTechnician().getId());
            if (detachedTech == null) {
                detachedTech = detachTechnician(w.getTechnician());
            }
        }

        return WorkOrder.builder()
                .id(w.getId())
                .workOrderNumber(w.getWorkOrderNumber())
                .title(w.getTitle())
                .description(w.getDescription())
                .priority(w.getPriority())
                .status(w.getStatus())
                .customer(w.getCustomer())
                .technician(detachedTech)
                .locationName(w.getLocationName())
                .city(w.getCity())
                .address(w.getAddress())
                .latitude(w.getLatitude())
                .longitude(w.getLongitude())
                .requiredSkills(w.getRequiredSkills() != null ? new HashSet<>(w.getRequiredSkills()) : new HashSet<>())
                .scheduledStartTime(w.getScheduledStartTime())
                .scheduledEndTime(w.getScheduledEndTime())
                .slaDeadline(w.getSlaDeadline())
                .completedAt(w.getCompletedAt())
                .estimatedDurationHours(w.getEstimatedDurationHours())
                .slaBreachProbability(w.getSlaBreachProbability())
                .technicianEtaMinutes(w.getTechnicianEtaMinutes())
                .distanceKm(w.getDistanceKm())
                .serviceNotes(w.getServiceNotes())
                .aiInsight(w.getAiInsight())
                .build();
    }

    private InventoryItem detachInventoryItem(InventoryItem i) {
        if (i == null) return null;
        return InventoryItem.builder()
                .id(i.getId())
                .name(i.getName())
                .sku(i.getSku())
                .category(i.getCategory())
                .availableQuantity(i.getAvailableQuantity())
                .reservedQuantity(i.getReservedQuantity())
                .minThreshold(i.getMinThreshold())
                .unitPrice(i.getUnitPrice())
                .unitOfMeasure(i.getUnitOfMeasure())
                .status(i.getStatus())
                .warehouseLocation(i.getWarehouseLocation())
                .build();
    }
}

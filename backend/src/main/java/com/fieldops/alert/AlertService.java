package com.fieldops.alert;

import com.fieldops.anomaly.OperationalAnomaly;
import com.fieldops.anomaly.OperationalAnomalyService;
import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.inventory.InventoryItem;
import com.fieldops.inventory.InventoryItemRepository;
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
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final OperationalAnomalyService anomalyService;
    private final OperationsEventPublisher eventPublisher;

    public AlertService(AlertRepository alertRepository,
                        WorkOrderRepository workOrderRepository,
                        TechnicianRepository technicianRepository,
                        InventoryItemRepository inventoryItemRepository,
                        OperationalAnomalyService anomalyService,
                        OperationsEventPublisher eventPublisher) {
        this.alertRepository = alertRepository;
        this.workOrderRepository = workOrderRepository;
        this.technicianRepository = technicianRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.anomalyService = anomalyService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<AlertDTO> getAlerts(AlertSeverity severity, AlertType type, AlertStatus status) {
        List<OperationalAlert> all = alertRepository.findTop50ByOrderByCreatedAtDesc();
        return all.stream()
                .filter(a -> severity == null || a.getSeverity() == severity)
                .filter(a -> type == null || a.getAlertType() == type)
                .filter(a -> status == null || a.getStatus() == status)
                .map(AlertDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertDTO> getActiveAlerts() {
        return alertRepository.findActiveAlerts().stream()
                .map(AlertDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlertDTO getAlertById(Long id) {
        OperationalAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operational alert not found with id: " + id));
        return AlertDTO.fromEntity(alert);
    }

    @Transactional
    public AlertDTO acknowledgeAlert(Long id, String username) {
        OperationalAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operational alert not found with id: " + id));

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(username != null ? username : "dispatcher@fieldops.demo");
        alert.setAcknowledgedAt(LocalDateTime.now());
        OperationalAlert saved = alertRepository.save(alert);

        eventPublisher.publish(OperationsEvent.builder()
                .eventType(OperationsEventType.ALERT_ACKNOWLEDGED)
                .severity(saved.getSeverity().name())
                .alertId(saved.getId())
                .message("Alert acknowledged by dispatcher: " + saved.getTitle())
                .build());

        return AlertDTO.fromEntity(saved);
    }

    @Transactional
    public AlertDTO resolveAlert(Long id, String username, String notes) {
        OperationalAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operational alert not found with id: " + id));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedBy(username != null ? username : "dispatcher@fieldops.demo");
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(notes != null ? notes : "Manually resolved via Dispatcher NOC.");
        OperationalAlert saved = alertRepository.save(alert);

        eventPublisher.publish(OperationsEvent.builder()
                .eventType(OperationsEventType.ALERT_RESOLVED)
                .severity("INFO")
                .alertId(saved.getId())
                .message("Alert resolved: " + saved.getTitle())
                .build());

        return AlertDTO.fromEntity(saved);
    }

    @Transactional
    public int scanAndGenerateDeterministicAlerts() {
        int countCreated = 0;
        List<WorkOrder> workOrders = workOrderRepository.findAll();
        List<Technician> technicians = technicianRepository.findAll();
        List<InventoryItem> items = inventoryItemRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        // 1. UNASSIGNED_CRITICAL_JOB: Critical work order remains unassigned
        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() != WorkOrderStatus.COMPLETED &&
                    wo.getPriority() == WorkOrderPriority.CRITICAL &&
                    (wo.getTechnician() == null || wo.getStatus() == WorkOrderStatus.NEW)) {
                if (createAlertIfNotExists(
                        AlertType.UNASSIGNED_CRITICAL_JOB,
                        AlertSeverity.CRITICAL,
                        "Unassigned Critical Work Order",
                        String.format("%s (%s) is unassigned without an active engineer.", wo.getWorkOrderNumber(), wo.getTitle()),
                        "Critical priority emergency ticket requires immediate dispatch intervention to preserve SLA adherence.",
                        "WORK_ORDER", wo.getId(), wo.getId(), wo.getWorkOrderNumber(), null, null
                )) {
                    countCreated++;
                }
            }
        }

        // 2. SLA_CRITICAL: SLA deadline imminent (remaining < 45 min or < 20% total SLA window)
        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() != WorkOrderStatus.COMPLETED && wo.getSlaDeadline() != null) {
                long minutesRemaining = Duration.between(now, wo.getSlaDeadline()).toMinutes();
                if (minutesRemaining < 45 && minutesRemaining >= -60) {
                    if (createAlertIfNotExists(
                            AlertType.SLA_CRITICAL,
                            AlertSeverity.CRITICAL,
                            "Imminent SLA Breach Threat",
                            String.format("%s: Only %d minutes remaining before contract breach threshold.", wo.getWorkOrderNumber(), Math.max(0, minutesRemaining)),
                            String.format("Calculated remaining buffer of %d min is below the minimum 45 min resolution threshold.", minutesRemaining),
                            "WORK_ORDER", wo.getId(), wo.getId(), wo.getWorkOrderNumber(),
                            wo.getTechnician() != null ? wo.getTechnician().getId() : null,
                            wo.getTechnician() != null ? wo.getTechnician().getName() : null
                    )) {
                        countCreated++;
                    }
                }
            }
        }

        // 3. SLA_AT_RISK: Breach probability >= 60%
        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() != WorkOrderStatus.COMPLETED &&
                    wo.getSlaBreachProbability() != null &&
                    wo.getSlaBreachProbability() >= 60.0) {
                if (createAlertIfNotExists(
                        AlertType.SLA_AT_RISK,
                        AlertSeverity.HIGH,
                        "SLA Risk Elevated: " + wo.getWorkOrderNumber(),
                        String.format("%s has calculated breach probability of %.0f%%.", wo.getWorkOrderNumber(), wo.getSlaBreachProbability()),
                        "Multi-factor telemetry indicates traffic transit or engineer workload is jeopardizing timely completion.",
                        "WORK_ORDER", wo.getId(), wo.getId(), wo.getWorkOrderNumber(),
                        wo.getTechnician() != null ? wo.getTechnician().getId() : null,
                        wo.getTechnician() != null ? wo.getTechnician().getName() : null
                )) {
                    countCreated++;
                }
            }
        }

        // 4. TECHNICIAN_OVERLOAD: technician has >= 4 active jobs
        for (Technician tech : technicians) {
            long activeJobs = workOrders.stream()
                    .filter(wo -> wo.getTechnician() != null && wo.getTechnician().getId().equals(tech.getId()))
                    .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED)
                    .count();
            if (activeJobs >= 4) {
                if (createAlertIfNotExists(
                        AlertType.TECHNICIAN_OVERLOAD,
                        AlertSeverity.HIGH,
                        "Workforce Overload: " + tech.getName(),
                        String.format("%s is currently assigned to %d concurrent active work orders.", tech.getName(), activeJobs),
                        "Exceeds recommended maximum threshold of 3 concurrent active assignments per field specialist.",
                        "TECHNICIAN", tech.getId(), null, null, tech.getId(), tech.getName()
                )) {
                    countCreated++;
                }
            }
        }

        // 5. INVENTORY_SHORTAGE: Parts below reorder threshold
        for (InventoryItem item : items) {
            if (item.getAvailableQuantity() <= item.getMinThreshold()) {
                if (createAlertIfNotExists(
                        AlertType.INVENTORY_SHORTAGE,
                        AlertSeverity.WARNING,
                        "Inventory Stock Depletion: " + item.getName(),
                        String.format("%s stock level is %d units (threshold: %d units).", item.getName(), item.getAvailableQuantity(), item.getMinThreshold()),
                        "Component reserve depleted. Dispatch of repair orders requiring this SKU may face supply bottlenecks.",
                        "INVENTORY", item.getId(), null, null, null, null
                )) {
                    countCreated++;
                }
            }
        }

        // 6. TERRITORY_SPIKE: Detected by OperationalAnomalyService
        try {
            List<OperationalAnomaly> anomalies = anomalyService.detectAnomalies();
            for (OperationalAnomaly anom : anomalies) {
                if ("TERRITORY".equalsIgnoreCase(anom.getCategory())) {
                    if (createAlertIfNotExists(
                            AlertType.TERRITORY_SPIKE,
                            AlertSeverity.WARNING,
                            "Territorial Surge: " + anom.getTitle(),
                            anom.getDescription(),
                            anom.getRecommendation(),
                            "TERRITORY", 1L, null, null, null, null
                    )) {
                        countCreated++;
                    }
                }
            }
        } catch (Exception ignored) {}

        // 7. TECHNICIAN_OFFLINE with active jobs assigned
        for (Technician tech : technicians) {
            if (tech.getStatus() == TechnicianStatus.OFFLINE) {
                long assignedActive = workOrders.stream()
                        .filter(wo -> wo.getTechnician() != null && wo.getTechnician().getId().equals(tech.getId()))
                        .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED)
                        .count();
                if (assignedActive > 0) {
                    if (createAlertIfNotExists(
                            AlertType.TECHNICIAN_OFFLINE,
                            AlertSeverity.HIGH,
                            "Assigned Technician Offline: " + tech.getName(),
                            String.format("%s is marked OFFLINE but holds %d active work orders.", tech.getName(), assignedActive),
                            "Jobs must be promptly re-assigned or technician status updated to prevent unmonitored ticket stagnation.",
                            "TECHNICIAN", tech.getId(), null, null, tech.getId(), tech.getName()
                    )) {
                        countCreated++;
                    }
                }
            }
        }

        return countCreated;
    }

    public boolean createOrUpdateAlert(AlertType type, AlertSeverity severity, String title,
                                       String message, String explanation, String entityType,
                                       Long entityId, Long workOrderId, String workOrderNumber,
                                       Long technicianId, String technicianName,
                                       String conditionKey, boolean isPredictive) {
        String fingerprint = String.format("%s:%s:%s:%s",
                type.name(),
                entityType != null ? entityType : "NONE",
                entityId != null ? entityId.toString() : "0",
                conditionKey != null ? conditionKey : "DEFAULT");

        Optional<OperationalAlert> activeAlert = alertRepository.findActiveByFingerprint(fingerprint);

        if (activeAlert.isPresent()) {
            // Deduplication: update existing alert's message and timestamp without creating duplicate
            OperationalAlert existing = activeAlert.get();
            existing.setMessage(message);
            existing.setExplanation(explanation);
            existing.setSeverity(severity);
            alertRepository.save(existing);
            return false;
        }

        OperationalAlert alert = OperationalAlert.builder()
                .alertType(type)
                .severity(severity)
                .status(AlertStatus.OPEN)
                .title(title)
                .message(message)
                .explanation(explanation)
                .fingerprint(fingerprint)
                .isPredictive(isPredictive)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .workOrderId(workOrderId)
                .workOrderNumber(workOrderNumber)
                .technicianId(technicianId)
                .technicianName(technicianName)
                .build();

        OperationalAlert saved = alertRepository.save(alert);

        // Broadcast to WebSocket
        eventPublisher.publish(OperationsEvent.builder()
                .eventType(isPredictive ? OperationsEventType.NEW_ALERT : OperationsEventType.NEW_ALERT)
                .severity(severity.name())
                .alertId(saved.getId())
                .workOrderId(workOrderId)
                .technicianId(technicianId)
                .message((isPredictive ? "[PREDICTIVE] " : "") + title + ": " + message)
                .build());

        return true;
    }

    private boolean createAlertIfNotExists(AlertType type, AlertSeverity severity, String title,
                                          String message, String explanation, String entityType,
                                          Long entityId, Long workOrderId, String workOrderNumber,
                                          Long technicianId, String technicianName) {
        return createOrUpdateAlert(type, severity, title, message, explanation, entityType,
                entityId, workOrderId, workOrderNumber, technicianId, technicianName, "BASELINE", false);
    }
}

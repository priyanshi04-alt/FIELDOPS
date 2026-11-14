package com.fieldops.alert;

import com.fieldops.inventory.InventoryItem;
import com.fieldops.inventory.InventoryItemRepository;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.websocket.OperationsEvent;
import com.fieldops.websocket.OperationsEventPublisher;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AlertServiceUnitTest {

    private List<OperationalAlert> alertStore;
    private List<WorkOrder> workOrderStore;
    private List<Technician> technicianStore;
    private List<InventoryItem> inventoryStore;
    private List<OperationsEvent> publishedEvents;

    private AlertRepository alertRepository;
    private WorkOrderRepository workOrderRepository;
    private TechnicianRepository technicianRepository;
    private InventoryItemRepository inventoryRepository;
    private OperationsEventPublisher eventPublisher;

    private AlertService alertService;
    private long idSequence = 1000L;

    @BeforeEach
    void setUp() {
        alertStore = new ArrayList<>();
        workOrderStore = new ArrayList<>();
        technicianStore = new ArrayList<>();
        inventoryStore = new ArrayList<>();
        publishedEvents = new ArrayList<>();

        alertRepository = (AlertRepository) Proxy.newProxyInstance(
                AlertRepository.class.getClassLoader(),
                new Class<?>[]{AlertRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findAll".equals(name)) return alertStore;
                    if ("findTop50ByOrderByCreatedAtDesc".equals(name)) return alertStore;
                    if ("findActiveAlerts".equals(name)) {
                        return alertStore.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED).toList();
                    }
                    if ("findActiveByFingerprint".equals(name)) {
                        String fp = (String) args[0];
                        return alertStore.stream().filter(a -> Objects.equals(a.getFingerprint(), fp) && a.getStatus() != AlertStatus.RESOLVED).findFirst();
                    }
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        return alertStore.stream().filter(a -> a.getId().equals(id)).findFirst();
                    }
                    if ("findFirstByAlertTypeAndRelatedEntityTypeAndRelatedEntityIdAndStatus".equals(name)) {
                        AlertType at = (AlertType) args[0];
                        String et = (String) args[1];
                        Long eid = (Long) args[2];
                        AlertStatus st = (AlertStatus) args[3];
                        return alertStore.stream().filter(a -> a.getAlertType() == at
                                && Objects.equals(a.getRelatedEntityType(), et)
                                && Objects.equals(a.getRelatedEntityId(), eid)
                                && a.getStatus() == st).findFirst();
                    }
                    if ("save".equals(name)) {
                        OperationalAlert a = (OperationalAlert) args[0];
                        if (a.getId() == null) a.setId(idSequence++);
                        alertStore.removeIf(existing -> existing.getId().equals(a.getId()));
                        alertStore.add(a);
                        return a;
                    }
                    if ("countOpenAlerts".equals(name)) {
                        return alertStore.stream().filter(a -> a.getStatus() == AlertStatus.OPEN).count();
                    }
                    if ("countActiveAlerts".equals(name)) {
                        return alertStore.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED).count();
                    }
                    return null;
                }
        );

        workOrderRepository = (WorkOrderRepository) Proxy.newProxyInstance(
                WorkOrderRepository.class.getClassLoader(),
                new Class<?>[]{WorkOrderRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findAll".equals(name)) return workOrderStore;
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        return workOrderStore.stream().filter(w -> w.getId().equals(id)).findFirst();
                    }
                    return null;
                }
        );

        technicianRepository = (TechnicianRepository) Proxy.newProxyInstance(
                TechnicianRepository.class.getClassLoader(),
                new Class<?>[]{TechnicianRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findAll".equals(name)) return technicianStore;
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        return technicianStore.stream().filter(t -> t.getId().equals(id)).findFirst();
                    }
                    return null;
                }
        );

        inventoryRepository = (InventoryItemRepository) Proxy.newProxyInstance(
                InventoryItemRepository.class.getClassLoader(),
                new Class<?>[]{InventoryItemRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findAll".equals(name)) return inventoryStore;
                    return null;
                }
        );

        eventPublisher = new OperationsEventPublisher(null) {
            @Override
            public void publish(OperationsEvent event) {
                publishedEvents.add(event);
            }
        };

        alertService = new AlertService(
                alertRepository,
                workOrderRepository,
                technicianRepository,
                inventoryRepository,
                null,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Alert Generation: Detects unassigned critical job deterministically")
    void testDetectUnassignedCriticalJob() {
        WorkOrder criticalUnassigned = WorkOrder.builder()
                .id(101L)
                .workOrderNumber("WO-CRIT-99")
                .title("Substation Fire Risk")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.NEW)
                .technician(null)
                .slaDeadline(LocalDateTime.now().plusHours(2))
                .build();

        workOrderStore.add(criticalUnassigned);

        int generated = alertService.scanAndGenerateDeterministicAlerts();
        assertEquals(1, generated);
        assertEquals(1, alertStore.size());
        OperationalAlert alert = alertStore.get(0);
        assertEquals(AlertType.UNASSIGNED_CRITICAL_JOB, alert.getAlertType());
        assertEquals(AlertSeverity.CRITICAL, alert.getSeverity());
        assertEquals(1, publishedEvents.size());
    }

    @Test
    @DisplayName("Alert Lifecycle: OPEN -> ACKNOWLEDGED -> RESOLVED transitions correctly")
    void testAlertLifecycle() {
        OperationalAlert alert = OperationalAlert.builder()
                .id(501L)
                .alertType(AlertType.SLA_CRITICAL)
                .severity(AlertSeverity.CRITICAL)
                .status(AlertStatus.OPEN)
                .title("Imminent SLA Breach")
                .message("WO-1042 has 20 min remaining")
                .build();

        alertStore.add(alert);

        // Acknowledge
        AlertDTO acked = alertService.acknowledgeAlert(501L, "dispatcher@fieldops.demo");
        assertEquals(AlertStatus.ACKNOWLEDGED, acked.getStatus());
        assertEquals("dispatcher@fieldops.demo", acked.getAcknowledgedBy());
        assertNotNull(acked.getAcknowledgedAt());

        // Resolve
        AlertDTO resolved = alertService.resolveAlert(501L, "dispatcher@fieldops.demo", "Technician reached site and reset timer.");
        assertEquals(AlertStatus.RESOLVED, resolved.getStatus());
        assertEquals("dispatcher@fieldops.demo", resolved.getResolvedBy());
        assertEquals("Technician reached site and reset timer.", resolved.getResolutionNotes());
        assertNotNull(resolved.getResolvedAt());
    }

    @Test
    @DisplayName("Alert Engine: Technician Overload triggered when active jobs >= 4")
    void testTechnicianOverloadAlert() {
        Technician tech = Technician.builder()
                .id(105L)
                .name("Kavita Rao")
                .status(TechnicianStatus.BUSY)
                .build();

        technicianStore.add(tech);

        for (int i = 0; i < 4; i++) {
            workOrderStore.add(WorkOrder.builder()
                    .id((long) (i + 1))
                    .workOrderNumber("WO-LOAD-" + i)
                    .title("Job " + i)
                    .priority(WorkOrderPriority.MEDIUM)
                    .status(WorkOrderStatus.IN_PROGRESS)
                    .technician(tech)
                    .build());
        }

        int generated = alertService.scanAndGenerateDeterministicAlerts();
        assertTrue(generated >= 1);
        boolean hasOverloadAlert = alertStore.stream()
                .anyMatch(a -> a.getAlertType() == AlertType.TECHNICIAN_OVERLOAD);
        assertTrue(hasOverloadAlert);
    }

    @Test
    @DisplayName("Alert Deduplication: Subsequent scans update active alert rather than duplicating")
    void testAlertDeduplication() {
        boolean created1 = alertService.createOrUpdateAlert(
                AlertType.SLA_CRITICAL,
                AlertSeverity.CRITICAL,
                "Imminent Breach WO-1042",
                "25 minutes remaining",
                "Contractual deadline warning",
                "WORK_ORDER",
                1042L, 1042L, "WO-1042",
                null, null,
                "SLA_CRIT_KEY",
                false
        );
        assertTrue(created1, "First scan should create alert");
        assertEquals(1, alertStore.size());

        boolean created2 = alertService.createOrUpdateAlert(
                AlertType.SLA_CRITICAL,
                AlertSeverity.CRITICAL,
                "Imminent Breach WO-1042",
                "20 minutes remaining (updated)",
                "Contractual deadline warning",
                "WORK_ORDER",
                1042L, 1042L, "WO-1042",
                null, null,
                "SLA_CRIT_KEY",
                false
        );
        assertFalse(created2, "Second scan with same fingerprint must NOT create duplicate");
        assertEquals(1, alertStore.size(), "Store should still hold exactly 1 alert");
        assertEquals("20 minutes remaining (updated)", alertStore.get(0).getMessage(), "Existing alert should be updated with new message");
    }

    @Test
    @DisplayName("Alert Lifecycle: When an alert is resolved and the condition re-occurs, a new active alert is created")
    void testAlertReactivationAfterResolution() {
        // 1. Initial occurrence
        alertService.createOrUpdateAlert(
                AlertType.INVENTORY_SHORTAGE,
                AlertSeverity.WARNING,
                "Depleted Sensor Stock",
                "Sensor stock is at 2 units",
                "Buffer depleted",
                "INVENTORY",
                99L, null, null, null, null,
                "INV_KEY_99",
                false
        );
        assertEquals(1, alertStore.size());
        OperationalAlert alert = alertStore.get(0);
        assertEquals(AlertStatus.OPEN, alert.getStatus());

        // 2. Dispatcher resolves the alert
        alertService.resolveAlert(alert.getId(), "dispatcher@fieldops.demo", "Restock PO dispatched");
        assertEquals(AlertStatus.RESOLVED, alert.getStatus());

        // 3. Condition re-occurs on next scan
        boolean createdAgain = alertService.createOrUpdateAlert(
                AlertType.INVENTORY_SHORTAGE,
                AlertSeverity.WARNING,
                "Depleted Sensor Stock",
                "Sensor stock is at 1 unit",
                "Buffer depleted again",
                "INVENTORY",
                99L, null, null, null, null,
                "INV_KEY_99",
                false
        );

        assertTrue(createdAgain, "A new alert must be created because the previous one was RESOLVED");
        assertEquals(2, alertStore.size(), "Store should now have 2 records (1 resolved history, 1 newly open)");
        assertEquals(1, alertStore.stream().filter(a -> a.getStatus() == AlertStatus.OPEN).count(), "Only 1 alert should be currently active");
    }
}

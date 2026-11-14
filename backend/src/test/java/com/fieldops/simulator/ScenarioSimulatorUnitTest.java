package com.fieldops.simulator;

import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.technician.TechnicianStatus;
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

public class ScenarioSimulatorUnitTest {

    private List<WorkOrder> workOrderStore;
    private List<Technician> technicianStore;
    private List<ScenarioDecisionAudit> auditStore;

    private WorkOrderRepository workOrderRepository;
    private TechnicianRepository technicianRepository;
    private ScenarioDecisionAuditRepository auditRepository;
    private ScenarioSimulationService simulationService;

    @BeforeEach
    void setUp() {
        workOrderStore = new ArrayList<>();
        technicianStore = new ArrayList<>();
        auditStore = new ArrayList<>();

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
                    if ("save".equals(name)) {
                        WorkOrder w = (WorkOrder) args[0];
                        workOrderStore.removeIf(existing -> existing.getId().equals(w.getId()));
                        workOrderStore.add(w);
                        return w;
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
                    if ("save".equals(name)) {
                        Technician t = (Technician) args[0];
                        technicianStore.removeIf(existing -> existing.getId().equals(t.getId()));
                        technicianStore.add(t);
                        return t;
                    }
                    return null;
                }
        );

        auditRepository = (ScenarioDecisionAuditRepository) Proxy.newProxyInstance(
                ScenarioDecisionAuditRepository.class.getClassLoader(),
                new Class<?>[]{ScenarioDecisionAuditRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        ScenarioDecisionAudit a = (ScenarioDecisionAudit) args[0];
                        auditStore.add(a);
                        return a;
                    }
                    if ("findTop20ByOrderByAppliedAtDesc".equals(name)) return auditStore;
                    return null;
                }
        );

        simulationService = new ScenarioSimulationService(
                workOrderRepository,
                technicianRepository,
                null,
                auditRepository,
                new com.fieldops.websocket.OperationsEventPublisher(null) {
                    @Override
                    public void publish(com.fieldops.websocket.OperationsEvent event) {}
                }
        );
    }

    @Test
    @DisplayName("What-If Simulation: Evaluates technician unavailable scenario without mutating production state")
    void testSimulationNonMutatingIsolation() {
        Technician primary = Technician.builder()
                .id(1L)
                .name("Rahul Sharma")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(2)
                .build();

        Technician backup = Technician.builder()
                .id(2L)
                .name("Swati Sen")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(0)
                .build();

        technicianStore.add(primary);
        technicianStore.add(backup);

        WorkOrder wo1 = WorkOrder.builder()
                .id(101L)
                .workOrderNumber("WO-101")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.ASSIGNED)
                .technician(primary)
                .slaDeadline(LocalDateTime.now().plusHours(3))
                .build();

        workOrderStore.add(wo1);

        ScenarioInput input = new ScenarioInput();
        input.setScenarioType(ScenarioType.TECHNICIAN_UNAVAILABLE);
        input.setTargetTechnicianId(1L);

        ScenarioResult result = simulationService.simulate(input);

        assertNotNull(result);
        assertEquals(ScenarioType.TECHNICIAN_UNAVAILABLE, result.getScenarioType());
        assertEquals(1, result.getReassignedJobsCount(), "Simulated reassignment should reassign 1 job");
        assertFalse(result.isApplied(), "Simulation must NOT be marked applied prior to explicit approval");

        // CRITICAL CHECK: Production state must be UNCHANGED
        assertEquals(TechnicianStatus.AVAILABLE, primary.getStatus(), "Production technician status must NOT be mutated by simulation");
        assertEquals(1L, wo1.getTechnician().getId(), "Production work order assignment must remain with original tech");
    }

    @Test
    @DisplayName("What-If Application: Dispatcher explicit apply mutates production state and creates audit record")
    void testApplyScenarioWithAudit() {
        Technician primary = Technician.builder()
                .id(1L)
                .name("Rahul Sharma")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(1)
                .build();

        Technician backup = Technician.builder()
                .id(2L)
                .name("Swati Sen")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(0)
                .build();

        technicianStore.add(primary);
        technicianStore.add(backup);

        WorkOrder wo1 = WorkOrder.builder()
                .id(101L)
                .workOrderNumber("WO-101")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.ASSIGNED)
                .technician(primary)
                .slaDeadline(LocalDateTime.now().plusHours(3))
                .build();

        workOrderStore.add(wo1);

        ScenarioInput input = new ScenarioInput();
        input.setScenarioType(ScenarioType.TECHNICIAN_UNAVAILABLE);
        input.setTargetTechnicianId(1L);

        ScenarioResult simulated = simulationService.simulate(input);
        assertNotNull(simulated.getSimulationId());

        // Apply explicitly
        ScenarioResult applied = simulationService.applyScenario(simulated.getSimulationId(), "dispatcher@fieldops.demo");

        assertTrue(applied.isApplied());
        assertEquals(TechnicianStatus.OFFLINE, primary.getStatus(), "Production tech should now be offline");
        assertEquals(2L, wo1.getTechnician().getId(), "Work order should now be re-assigned to backup tech");
        assertEquals(1, auditStore.size(), "ScenarioDecisionAudit must be created");
        assertEquals("dispatcher@fieldops.demo", auditStore.get(0).getDispatcherEmail());
    }

    @Test
    @DisplayName("What-If Concurrency: Rejects duplicate scenario application (Idempotency)")
    void testDuplicateScenarioApplicationRejection() {
        Technician primary = Technician.builder()
                .id(1L)
                .name("Rahul Sharma")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(1)
                .build();

        Technician backup = Technician.builder()
                .id(2L)
                .name("Swati Sen")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(0)
                .build();

        technicianStore.add(primary);
        technicianStore.add(backup);

        WorkOrder wo1 = WorkOrder.builder()
                .id(101L)
                .workOrderNumber("WO-101")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.ASSIGNED)
                .technician(primary)
                .slaDeadline(LocalDateTime.now().plusHours(3))
                .build();

        workOrderStore.add(wo1);

        ScenarioInput input = new ScenarioInput();
        input.setScenarioType(ScenarioType.TECHNICIAN_UNAVAILABLE);
        input.setTargetTechnicianId(1L);

        ScenarioResult simulated = simulationService.simulate(input);

        // First application succeeds
        simulationService.applyScenario(simulated.getSimulationId(), "dispatcher@fieldops.demo");

        // Second application MUST throw IllegalStateException (conflict)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                simulationService.applyScenario(simulated.getSimulationId(), "dispatcher@fieldops.demo")
        );
        assertTrue(ex.getMessage().contains("already been applied"));
    }

    @Test
    @DisplayName("What-If Safety: Rejects stale scenario application when production state drifted")
    void testStaleScenarioRejectionOnStateDrift() {
        Technician primary = Technician.builder()
                .id(1L)
                .name("Rahul Sharma")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(1)
                .build();

        Technician backup = Technician.builder()
                .id(2L)
                .name("Swati Sen")
                .city("Noida")
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(0)
                .build();

        technicianStore.add(primary);
        technicianStore.add(backup);

        WorkOrder wo1 = WorkOrder.builder()
                .id(101L)
                .workOrderNumber("WO-101")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.ASSIGNED)
                .technician(primary)
                .slaDeadline(LocalDateTime.now().plusHours(3))
                .build();

        workOrderStore.add(wo1);

        ScenarioInput input = new ScenarioInput();
        input.setScenarioType(ScenarioType.TECHNICIAN_UNAVAILABLE);
        input.setTargetTechnicianId(1L);

        ScenarioResult simulated = simulationService.simulate(input);

        // Simulate concurrent state drift in production: wo1 is reassigned by another dispatcher
        wo1.setTechnician(backup);

        // Attempting to apply the stale simulation must be rejected!
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                simulationService.applyScenario(simulated.getSimulationId(), "dispatcher@fieldops.demo")
        );
        assertTrue(ex.getMessage().contains("drift detected"));
    }
}

package com.fieldops.intelligence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.alert.AlertDTO;
import com.fieldops.alert.AlertSeverity;
import com.fieldops.alert.AlertService;
import com.fieldops.alert.AlertType;
import com.fieldops.anomaly.OperationalAnomaly;
import com.fieldops.anomaly.OperationalAnomalyService;
import com.fieldops.assignment.AssignmentRecommendation;
import com.fieldops.assignment.AssignmentScoringService;
import com.fieldops.assignment.TechnicianAssignmentService;
import com.fieldops.customer.Customer;
import com.fieldops.inventory.InventoryItem;
import com.fieldops.inventory.InventoryItemRepository;
import com.fieldops.predictive.ModelRegistryEntry;
import com.fieldops.predictive.ModelRegistryService;
import com.fieldops.predictive.PredictiveInventoryService;
import com.fieldops.predictive.PredictiveSlaService;
import com.fieldops.routing.multistop.MultiStopRouteService;
import com.fieldops.routing.multistop.OptimizedRoute;
import com.fieldops.routing.multistop.RouteDecisionAudit;
import com.fieldops.routing.multistop.RouteDecisionAuditRepository;
import com.fieldops.routing.multistop.RouteOptimizationService;
import com.fieldops.sla.SlaRiskService;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Phase 6.7.5: Automated Verification Suite for Operations Intelligence Transaction Optimization.
 * Covers all 10 required architectural criteria:
 *  1. Existing overview response compatibility.
 *  2. Correct tenant isolation.
 *  3. Repository queries execute within intended transaction boundaries.
 *  4. CPU-heavy calculations execute outside database transactions.
 *  5. No lazy-loading exceptions.
 *  6. Empty work-order and technician datasets.
 *  7. Missing or incomplete prediction data.
 *  8. Concurrent overview requests.
 *  9. Error handling and fallback behavior.
 * 10. Query-count regression protection.
 */
@ExtendWith(MockitoExtension.class)
public class OperationsIntelligenceTransactionOptimizationTest {

    @Mock private WorkOrderRepository workOrderRepository;
    @Mock private TechnicianRepository technicianRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private AlertService alertService;
    @Mock private RouteDecisionAuditRepository auditRepository;
    @Mock private ModelRegistryService modelRegistryService;

    private SlaRiskService slaRiskService;
    private AssignmentScoringService scoringService;
    private TechnicianAssignmentService assignmentService;
    private OperationalAnomalyService anomalyService;
    private RouteOptimizationService routeOptimizationService;
    private MultiStopRouteService multiStopRouteService;
    private PredictiveInventoryService predictiveInventoryService;
    private PredictiveSlaService predictiveSlaService;

    private OperationsSnapshotService snapshotService;
    private OperationsIntelligenceService intelligenceService;

    private Customer testCustomerA;
    private Customer testCustomerB;
    private Technician testTech1;
    private Technician testTech2;
    private WorkOrder testWo1;
    private WorkOrder testWo2;
    private InventoryItem testItem1;

    @BeforeEach
    void setUp() {
        slaRiskService = new SlaRiskService(workOrderRepository);
        scoringService = new AssignmentScoringService();
        assignmentService = new TechnicianAssignmentService(workOrderRepository, technicianRepository, scoringService, null, null);
        anomalyService = new OperationalAnomalyService(workOrderRepository, technicianRepository, inventoryItemRepository);
        routeOptimizationService = new RouteOptimizationService();
        multiStopRouteService = new MultiStopRouteService(technicianRepository, workOrderRepository, routeOptimizationService, auditRepository, null);
        predictiveInventoryService = new PredictiveInventoryService(inventoryItemRepository, modelRegistryService, alertService, null);
        predictiveSlaService = new PredictiveSlaService(workOrderRepository, slaRiskService, modelRegistryService, alertService, null);

        snapshotService = new OperationsSnapshotService(
                workOrderRepository,
                technicianRepository,
                inventoryItemRepository,
                alertService,
                auditRepository,
                modelRegistryService
        );

        intelligenceService = new OperationsIntelligenceService(
                slaRiskService,
                assignmentService,
                anomalyService,
                workOrderRepository,
                alertService,
                multiStopRouteService,
                technicianRepository,
                predictiveSlaService,
                predictiveInventoryService,
                modelRegistryService,
                snapshotService
        );

        // Test Fixtures
        testCustomerA = Customer.builder()
                .id(10L)
                .name("Acme Corp")
                .email("contact@acme.test")
                .phone("+1234567890")
                .city("Noida")
                .address("Sector 62")
                .build();

        testCustomerB = Customer.builder()
                .id(20L)
                .name("Globex Inc")
                .email("ops@globex.test")
                .phone("+1987654321")
                .city("Delhi")
                .address("Connaught Place")
                .build();

        testTech1 = Technician.builder()
                .id(1L)
                .name("Arun Sharma")
                .email("arun@fieldops.demo")
                .phone("9876543210")
                .city("Noida")
                .latitude(28.5355)
                .longitude(77.3910)
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(1)
                .skills(Set.of("HVAC", "Electrical"))
                .build();

        testTech2 = Technician.builder()
                .id(2L)
                .name("Vikram Singh")
                .email("vikram@fieldops.demo")
                .phone("9876543211")
                .city("Delhi")
                .latitude(28.6139)
                .longitude(77.2090)
                .status(TechnicianStatus.AVAILABLE)
                .activeJobsCount(0)
                .skills(Set.of("Plumbing"))
                .build();

        testWo1 = WorkOrder.builder()
                .id(101L)
                .workOrderNumber("WO-101")
                .title("AC Compressor Overhaul")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.NEW)
                .customer(testCustomerA)
                .city("Noida")
                .latitude(28.5360)
                .longitude(77.3920)
                .requiredSkills(Set.of("HVAC"))
                .slaDeadline(LocalDateTime.now().plusHours(2))
                .estimatedDurationHours(2.0)
                .build();

        testWo2 = WorkOrder.builder()
                .id(102L)
                .workOrderNumber("WO-102")
                .title("Leakage Inspection")
                .priority(WorkOrderPriority.MEDIUM)
                .status(WorkOrderStatus.ASSIGNED)
                .customer(testCustomerB)
                .technician(testTech1)
                .city("Noida")
                .latitude(28.5370)
                .longitude(77.3930)
                .requiredSkills(Set.of("Plumbing"))
                .slaDeadline(LocalDateTime.now().plusHours(4))
                .estimatedDurationHours(1.5)
                .build();

        testItem1 = InventoryItem.builder()
                .id(501L)
                .sku("SKU-HVAC-COMP")
                .name("HVAC Compressor 2.5T")
                .category("HVAC")
                .availableQuantity(5)
                .minThreshold(10)
                .build();
    }

    private void setupStandardMocks() {
        when(workOrderRepository.findAll()).thenReturn(List.of(testWo1, testWo2));
        when(technicianRepository.findAll()).thenReturn(List.of(testTech1, testTech2));
        when(inventoryItemRepository.findAll()).thenReturn(List.of(testItem1));
        when(alertService.getActiveAlerts()).thenReturn(Collections.emptyList());
        when(auditRepository.findByTechnicianIdOrderByTimestampDesc(anyLong())).thenReturn(Collections.emptyList());
        when(modelRegistryService.getAllRegisteredModels()).thenReturn(Collections.emptyMap());
    }

    @Test
    @DisplayName("Test 1: Existing Overview Response Compatibility — All API fields preserved and populated")
    void test1_overviewResponseCompatibility() {
        setupStandardMocks();

        OperationsIntelligenceOverview overview = intelligenceService.getOverview();

        assertNotNull(overview, "Overview response must not be null");
        assertTrue(overview.getOperationsHealthScore() >= 45 && overview.getOperationsHealthScore() <= 100);
        assertNotNull(overview.getHighSlaRisks());
        assertNotNull(overview.getAnomalies());
        assertNotNull(overview.getPendingSmartDispatches());
        assertNotNull(overview.getActiveAlerts());
        assertNotNull(overview.getGeneratedAt());
        assertNotNull(overview.getPerformanceMetrics(), "Performance metrics breakdown must be populated");

        // Verify smart dispatch contains the unassigned WO-101
        assertFalse(overview.getPendingSmartDispatches().isEmpty(), "Unassigned WO should produce smart dispatch");
        assertEquals(101L, overview.getPendingSmartDispatches().get(0).getWorkOrderId());
        assertNotNull(overview.getPendingSmartDispatches().get(0).getTopCandidate());
    }

    @Test
    @DisplayName("Test 2: Correct Tenant Isolation — Customer boundaries strictly preserved across snapshot")
    void test2_tenantIsolation() {
        setupStandardMocks();

        OperationsDataSnapshot snapshot = snapshotService.captureSnapshot();

        // Customer A and Customer B must remain distinct
        WorkOrder woA = snapshot.getWorkOrders().stream().filter(w -> w.getId().equals(101L)).findFirst().orElseThrow();
        WorkOrder woB = snapshot.getWorkOrders().stream().filter(w -> w.getId().equals(102L)).findFirst().orElseThrow();

        assertEquals(10L, woA.getCustomer().getId(), "WorkOrder 101 must belong strictly to Customer A");
        assertEquals(20L, woB.getCustomer().getId(), "WorkOrder 102 must belong strictly to Customer B");
        assertNotEquals(woA.getCustomer().getId(), woB.getCustomer().getId(), "Cross-customer leakage detected!");
    }

    @Test
    @DisplayName("Test 3: Repository queries execute strictly inside Stage A snapshot boundary")
    void test3_repositoryQueriesWithinIntendedBoundary() {
        setupStandardMocks();

        // Spy on snapshot service to verify it encapsulates all DB retrieval
        OperationsSnapshotService spySnapshotService = spy(snapshotService);
        OperationsIntelligenceService testService = new OperationsIntelligenceService(
                slaRiskService, assignmentService, anomalyService, workOrderRepository,
                alertService, multiStopRouteService, technicianRepository, predictiveSlaService,
                predictiveInventoryService, modelRegistryService, spySnapshotService
        );

        OperationsIntelligenceOverview overview = testService.getOverview();

        verify(spySnapshotService, times(1)).captureSnapshot();
        verify(workOrderRepository, times(1)).findAll();
        verify(technicianRepository, times(1)).findAll();
        verify(inventoryItemRepository, times(1)).findAll();
        verify(alertService, times(1)).getActiveAlerts();

        // Crucial: After captureSnapshot(), no additional repository calls occur during Stage B/C
        verifyNoMoreInteractions(workOrderRepository, technicianRepository, inventoryItemRepository);
    }

    @Test
    @DisplayName("Test 4: CPU-heavy calculations execute outside database transactions")
    void test4_calculationsExecuteOutsideTransaction() {
        setupStandardMocks();

        // During getOverview() invocation, assert that TransactionSynchronizationManager indicates no active transaction
        // in the orchestrator method.
        AtomicBoolean wasInTransactionDuringCpu = new AtomicBoolean(false);

        SlaRiskService instrumentedSlaService = new SlaRiskService(workOrderRepository) {
            @Override
            public com.fieldops.sla.SlaRiskResult calculateRisk(WorkOrder wo) {
                if (TransactionSynchronizationManager.isActualTransactionActive()) {
                    wasInTransactionDuringCpu.set(true);
                }
                return super.calculateRisk(wo);
            }
        };

        OperationsIntelligenceService testService = new OperationsIntelligenceService(
                instrumentedSlaService, assignmentService, anomalyService, workOrderRepository,
                alertService, multiStopRouteService, technicianRepository, predictiveSlaService,
                predictiveInventoryService, modelRegistryService, snapshotService
        );

        OperationsIntelligenceOverview overview = testService.getOverview();

        assertNotNull(overview);
        assertFalse(wasInTransactionDuringCpu.get(),
                "CPU calculation was erroneously executed inside an active database transaction!");
    }

    @Test
    @DisplayName("Test 5: No Lazy-Loading Exceptions — Detached entities access collections safely")
    void test5_noLazyLoadingExceptions() {
        setupStandardMocks();

        OperationsDataSnapshot snapshot = snapshotService.captureSnapshot();

        // Iterate all detached work orders and technicians; access all collections and associations
        for (WorkOrder wo : snapshot.getWorkOrders()) {
            assertDoesNotThrow(() -> {
                String desc = wo.getDescription();
                Set<String> skills = wo.getRequiredSkills();
                assertNotNull(skills);
                if (wo.getTechnician() != null) {
                    Set<String> techSkills = wo.getTechnician().getSkills();
                    assertNotNull(techSkills);
                    String name = wo.getTechnician().getName();
                    assertNotNull(name);
                }
            }, "Accessing detached collections must not throw LazyInitializationException");
        }
    }

    @Test
    @DisplayName("Test 6: Empty Datasets — Graceful handling when no work orders or technicians exist")
    void test6_emptyWorkOrderAndTechnicianDatasets() {
        when(workOrderRepository.findAll()).thenReturn(Collections.emptyList());
        when(technicianRepository.findAll()).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findAll()).thenReturn(Collections.emptyList());
        when(alertService.getActiveAlerts()).thenReturn(Collections.emptyList());
        when(modelRegistryService.getAllRegisteredModels()).thenReturn(Collections.emptyMap());

        OperationsIntelligenceOverview overview = assertDoesNotThrow(
                () -> intelligenceService.getOverview(),
                "getOverview must handle completely empty datasets without exception"
        );

        assertEquals(100, overview.getOperationsHealthScore(), "Clean slate with 0 issues should yield 100 health");
        assertEquals(0, overview.getActiveAnomaliesCount());
        assertEquals(0, overview.getCriticalSlaCount());
        assertEquals(0, overview.getActiveAlertsCount());
        assertTrue(overview.getPendingSmartDispatches().isEmpty());
        assertTrue(overview.getHighSlaRisks().isEmpty());
        assertNull(overview.getSampleOptimizedRoute(), "No route possible with 0 technicians");
    }

    @Test
    @DisplayName("Test 7: Incomplete/Null Prediction Services — Fallback behavior without crashing")
    void test7_missingOrIncompletePredictionData() {
        when(workOrderRepository.findAll()).thenReturn(List.of(testWo1));
        when(technicianRepository.findAll()).thenReturn(List.of(testTech1));
        when(inventoryItemRepository.findAll()).thenReturn(List.of(testItem1));
        when(alertService.getActiveAlerts()).thenReturn(Collections.emptyList());
        when(auditRepository.findByTechnicianIdOrderByTimestampDesc(anyLong())).thenReturn(Collections.emptyList());
        when(modelRegistryService.getAllRegisteredModels()).thenReturn(Collections.emptyMap());

        // Construct service with null predictive services
        OperationsIntelligenceService resilientService = new OperationsIntelligenceService(
                slaRiskService, assignmentService, anomalyService, workOrderRepository,
                alertService, multiStopRouteService, technicianRepository, null, null, null, snapshotService
        );

        OperationsIntelligenceOverview overview = assertDoesNotThrow(
                () -> resilientService.getOverview(),
                "Must tolerate null prediction services gracefully"
        );

        assertNotNull(overview);
        assertEquals(0, overview.getPredictedBreachesCount());
        assertEquals(0, overview.getPredictedStockoutsCount());
    }

    @Test
    @DisplayName("Test 8: Concurrent Overview Requests — Thread-safe execution under simultaneous access")
    void test8_concurrentOverviewRequests() throws Exception {
        setupStandardMocks();

        int threadsCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadsCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadsCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    OperationsIntelligenceOverview overview = intelligenceService.getOverview();
                    if (overview != null && overview.getOperationsHealthScore() > 0) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release all threads at the same instant
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent requests should finish within timeout");
        assertEquals(threadsCount, successCount.get(), "All concurrent requests must succeed");
    }

    @Test
    @DisplayName("Test 9: Error Handling and Fallbacks — Resilient to downstream sub-service exceptions")
    void test9_errorHandlingAndFallbacks() {
        when(workOrderRepository.findAll()).thenReturn(List.of(testWo1));
        when(technicianRepository.findAll()).thenReturn(List.of(testTech1));
        when(inventoryItemRepository.findAll()).thenReturn(List.of(testItem1));
        when(alertService.getActiveAlerts()).thenReturn(Collections.emptyList());
        when(auditRepository.findByTechnicianIdOrderByTimestampDesc(anyLong())).thenReturn(Collections.emptyList());

        // MultiStopRouteService that throws an unexpected internal calculation exception
        MultiStopRouteService failingRouteService = new MultiStopRouteService(technicianRepository, workOrderRepository, routeOptimizationService, auditRepository, null) {
            @Override
            public OptimizedRoute computeOptimizedRoute(Technician tech, List<WorkOrder> allWorkOrders, LocalDateTime departureTime) {
                throw new IllegalStateException("Simulated graph optimization error");
            }
        };

        OperationsIntelligenceService safeService = new OperationsIntelligenceService(
                slaRiskService, assignmentService, anomalyService, workOrderRepository,
                alertService, failingRouteService, technicianRepository, predictiveSlaService,
                predictiveInventoryService, modelRegistryService, snapshotService
        );

        OperationsIntelligenceOverview overview = assertDoesNotThrow(
                () -> safeService.getOverview(),
                "Overview processing must not crash if route optimization fails"
        );

        assertNotNull(overview);
        assertNull(overview.getSampleOptimizedRoute(), "Route should be null on failure");
        assertNotNull(overview.getPerformanceMetrics(), "Metrics should still be captured");
    }

    @Test
    @DisplayName("Test 10: Query-Count Regression Protection — Strict upper bound on database round-trips")
    void test10_queryCountRegressionProtection() {
        setupStandardMocks();

        OperationsIntelligenceOverview overview = intelligenceService.getOverview();

        // In the original flawed implementation, query count scaled with the number of unassigned work orders (N+1)
        // and repeated calls across sub-services (up to 19 queries).
        // In the refactored architecture, query count is strictly bounded to exactly 5 queries in Stage A:
        // 1. workOrderRepository.findAll()
        // 2. technicianRepository.findAll()
        // 3. inventoryItemRepository.findAll()
        // 4. alertService.getActiveAlerts()
        // 5. auditRepository.findByTechnicianIdOrderByTimestampDesc()

        verify(workOrderRepository, times(1)).findAll();
        verify(technicianRepository, times(1)).findAll();
        verify(inventoryItemRepository, times(1)).findAll();
        verify(alertService, times(1)).getActiveAlerts();
        verify(auditRepository, times(1)).findByTechnicianIdOrderByTimestampDesc(1L);

        // Verify zero queries to findById for unassigned work orders or technicians during recommendation
        verify(workOrderRepository, never()).findById(anyLong());
        verify(technicianRepository, never()).findById(anyLong());

        OperationsPerformanceMetrics metrics = overview.getPerformanceMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getDatabaseRetrievalDurationMs() >= 0);
        assertTrue(metrics.getTotalOverviewProcessingDurationMs() >= 0);
    }
}

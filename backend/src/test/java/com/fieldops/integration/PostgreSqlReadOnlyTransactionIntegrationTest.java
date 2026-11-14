package com.fieldops.integration;

import com.fieldops.alert.AlertService;
import com.fieldops.intelligence.OperationsIntelligenceOverview;
import com.fieldops.intelligence.OperationsIntelligenceService;
import com.fieldops.predictive.PredictiveSlaResultDTO;
import com.fieldops.predictive.PredictiveSlaService;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostgreSQL-Specific Integration Test for SEC-6301.
 *
 * Verifies that read-only overview and predictive evaluation operations
 * do not execute database writes inside PostgreSQL read-only transactions
 * (which in PostgreSQL enforces SQLState 25006: cannot execute INSERT/UPDATE/DELETE in a read-only transaction).
 *
 * Enabled when POSTGRES_INTEGRATION_TEST=true is set in the environment
 * (e.g., CI workflow with PostgreSQL 17 container or local PostgreSQL instance).
 */
@SpringBootTest
@ActiveProfiles("pgtest")
@EnabledIfEnvironmentVariable(named = "POSTGRES_INTEGRATION_TEST", matches = "true")
public class PostgreSqlReadOnlyTransactionIntegrationTest {

    @Autowired
    private OperationsIntelligenceService operationsIntelligenceService;

    @Autowired
    private PredictiveSlaService predictiveSlaService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Test
    @DisplayName("SEC-6301: Operations Intelligence Overview runs cleanly under read-only transaction on PostgreSQL")
    @Transactional(readOnly = true)
    void getOverview_InReadOnlyTransaction_ShouldNotTriggerSqlState25006() {
        // Must succeed without throwing CannotCreateTransactionException or PSQLException (SQLState 25006)
        OperationsIntelligenceOverview overview = operationsIntelligenceService.getOverview();

        assertNotNull(overview, "Overview must not be null");
        assertTrue(overview.getOperationsHealthScore() >= 0, "Operations health score must be non-negative");
        assertNotNull(overview.getActiveAlerts(), "Active alerts must be defined");
    }

    @Test
    @DisplayName("SEC-6301: Pure predictive SLA computation produces risk without database write side-effects")
    @Transactional(readOnly = true)
    void computePredictiveRisk_InReadOnlyTransaction_ShouldNotTriggerWrites() {
        List<WorkOrder> workOrders = workOrderRepository.findAll();
        if (!workOrders.isEmpty()) {
            WorkOrder wo = workOrders.get(0);
            PredictiveSlaResultDTO risk = predictiveSlaService.computePredictiveRisk(wo);

            assertNotNull(risk, "Predictive risk result must not be null");
            assertNotNull(risk.getMlRiskAssessment(), "Breach risk level must be defined");
            assertTrue(risk.getMlBreachProbability() >= 0.0 && risk.getMlBreachProbability() <= 1.0,
                    "Breach probability must be between 0.0 and 1.0");
        }
    }

    @Test
    @DisplayName("SEC-6301: Dedicated alert scanning operates within explicit write-capable transaction")
    void scanAndGenerateAlerts_ShouldPersistSuccessfullyInWriteTransaction() {
        // Alert scanning is invoked via write-capable workflow
        assertDoesNotThrow(() -> alertService.scanAndGenerateDeterministicAlerts(),
                "Deterministic alert scanning must execute cleanly in a write-capable workflow");
    }
}

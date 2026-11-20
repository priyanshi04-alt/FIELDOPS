package com.fieldops.intelligence;

import com.fieldops.alert.AlertDTO;
import com.fieldops.inventory.InventoryItem;
import com.fieldops.predictive.ModelRegistryEntry;
import com.fieldops.routing.multistop.RouteDecisionAudit;
import com.fieldops.technician.Technician;
import com.fieldops.workorder.WorkOrder;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Immutable snapshot containing detached domain models and metadata captured
 * strictly within the read-only database transaction boundary (Stage A).
 * All collections are initialized and detached from Hibernate session to guarantee
 * zero LazyInitializationExceptions during downstream CPU processing (Stage B).
 */
public class OperationsDataSnapshot {

    private final List<WorkOrder> workOrders;
    private final List<Technician> technicians;
    private final List<InventoryItem> inventoryItems;
    private final List<AlertDTO> activeAlerts;
    private final List<RouteDecisionAudit> recentRouteAudits;
    private final Map<String, ModelRegistryEntry> modelRegistry;
    private final Long topTechnicianId;
    private final Technician topTechnician;
    private final LocalDateTime capturedAt;
    private final long retrievalDurationMs;
    private final long transactionDurationMs;

    public OperationsDataSnapshot(List<WorkOrder> workOrders,
                                  List<Technician> technicians,
                                  List<InventoryItem> inventoryItems,
                                  List<AlertDTO> activeAlerts,
                                  List<RouteDecisionAudit> recentRouteAudits,
                                  Map<String, ModelRegistryEntry> modelRegistry,
                                  Long topTechnicianId,
                                  Technician topTechnician,
                                  long retrievalDurationMs,
                                  long transactionDurationMs) {
        this.workOrders = workOrders != null ? Collections.unmodifiableList(workOrders) : Collections.emptyList();
        this.technicians = technicians != null ? Collections.unmodifiableList(technicians) : Collections.emptyList();
        this.inventoryItems = inventoryItems != null ? Collections.unmodifiableList(inventoryItems) : Collections.emptyList();
        this.activeAlerts = activeAlerts != null ? Collections.unmodifiableList(activeAlerts) : Collections.emptyList();
        this.recentRouteAudits = recentRouteAudits != null ? Collections.unmodifiableList(recentRouteAudits) : Collections.emptyList();
        this.modelRegistry = modelRegistry != null ? Collections.unmodifiableMap(modelRegistry) : Collections.emptyMap();
        this.topTechnicianId = topTechnicianId;
        this.topTechnician = topTechnician;
        this.capturedAt = LocalDateTime.now();
        this.retrievalDurationMs = retrievalDurationMs;
        this.transactionDurationMs = transactionDurationMs;
    }

    public List<WorkOrder> getWorkOrders() { return workOrders; }
    public List<Technician> getTechnicians() { return technicians; }
    public List<InventoryItem> getInventoryItems() { return inventoryItems; }
    public List<AlertDTO> getActiveAlerts() { return activeAlerts; }
    public List<RouteDecisionAudit> getRecentRouteAudits() { return recentRouteAudits; }
    public Map<String, ModelRegistryEntry> getModelRegistry() { return modelRegistry; }
    public Long getTopTechnicianId() { return topTechnicianId; }
    public Technician getTopTechnician() { return topTechnician; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public long getRetrievalDurationMs() { return retrievalDurationMs; }
    public long getTransactionDurationMs() { return transactionDurationMs; }
}

package com.fieldops.anomaly;

import com.fieldops.inventory.InventoryItem;
import com.fieldops.inventory.InventoryItemRepository;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperationalAnomalyService {

    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public OperationalAnomalyService(WorkOrderRepository workOrderRepository,
                                     TechnicianRepository technicianRepository,
                                     InventoryItemRepository inventoryItemRepository) {
        this.workOrderRepository = workOrderRepository;
        this.technicianRepository = technicianRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OperationalAnomaly> detectAnomalies() {
        List<OperationalAnomaly> list = new ArrayList<>();

        List<WorkOrder> allWorkOrders = workOrderRepository.findAll();
        List<Technician> allTechnicians = technicianRepository.findAll();
        List<InventoryItem> allInventory = inventoryItemRepository.findAll();

        // 1. TECHNICIAN OVERLOAD ANOMALY
        // Baseline: 2.0 active jobs/technician. Trigger if technician >= 4 jobs (+100% overload).
        for (Technician tech : allTechnicians) {
            int active = tech.getActiveJobsCount() != null ? tech.getActiveJobsCount() : 0;
            if (active >= 4) {
                list.add(new OperationalAnomaly(
                        "ANOM-TECH-" + tech.getId(),
                        String.format("Technician %s Overloaded", tech.getName()),
                        String.format("Technician %s currently assigned %d active concurrent jobs (baseline normal is 2 jobs). High fatigue and SLA breach hazard.", tech.getName(), active),
                        "HIGH",
                        "WORKLOAD",
                        "TECHNICIAN",
                        String.valueOf(tech.getId()),
                        2.0,
                        (double) active,
                        "concurrent jobs",
                        "Redistribute pending work orders to nearby idle engineers in " + tech.getCity() + "."
                ));
            }
        }

        // 2. REGIONAL WORKLOAD CONCENTRATION SPIKE
        // Check if any single territory accounts for > 40% of all open tickets
        List<WorkOrder> openOrders = allWorkOrders.stream()
                .filter(w -> w.getStatus() != WorkOrderStatus.COMPLETED)
                .collect(Collectors.toList());

        if (!openOrders.isEmpty()) {
            Map<String, Long> cityCounts = openOrders.stream()
                    .filter(w -> w.getCity() != null)
                    .collect(Collectors.groupingBy(WorkOrder::getCity, Collectors.counting()));

            long totalOpen = openOrders.size();
            for (Map.Entry<String, Long> entry : cityCounts.entrySet()) {
                double pct = (double) entry.getValue() / totalOpen * 100.0;
                if (pct > 38.0) {
                    list.add(new OperationalAnomaly(
                            "ANOM-TERR-" + entry.getKey().toUpperCase(),
                            String.format("Territory Demand Spike: %s (+%.0f%%)", entry.getKey(), pct),
                            String.format("%s territory accounts for %d out of %d active jobs (%.1f%% of entire fleet volume). Capacity strained.", entry.getKey(), entry.getValue(), totalOpen, pct),
                            "HIGH",
                            "TERRITORY",
                            "TERRITORY",
                            entry.getKey(),
                            20.0,
                            pct,
                            "% total open volume",
                            "Authorize temporary inter-city cross-dispatch from adjacent hubs."
                    ));
                }
            }
        }

        // 3. UNASSIGNED CRITICAL WORK ORDERS (SLA BREACH THREAT)
        long unassignedCritical = openOrders.stream()
                .filter(w -> w.getPriority() == WorkOrderPriority.CRITICAL && w.getTechnician() == null)
                .count();

        if (unassignedCritical > 0) {
            list.add(new OperationalAnomaly(
                    "ANOM-SLA-UNASSIGNED-CRIT",
                    String.format("%d Unassigned Critical Work Orders", unassignedCritical),
                    String.format("There are %d CRITICAL severity tickets in NEW status with no field engineer assigned.", unassignedCritical),
                    "CRITICAL",
                    "SLA",
                    "WORK_ORDER",
                    "FLEET",
                    0.0,
                    (double) unassignedCritical,
                    "unassigned tickets",
                    "Use Smart Dispatch immediately to trigger rapid candidate allocation."
            ));
        }

        // 4. CRITICAL INVENTORY DEPLETION BLOCKING DISPATCH
        List<InventoryItem> zeroStock = allInventory.stream()
                .filter(i -> i.getAvailableQuantity() != null && i.getAvailableQuantity() == 0)
                .collect(Collectors.toList());

        if (!zeroStock.isEmpty()) {
            list.add(new OperationalAnomaly(
                    "ANOM-INV-STOCKOUT",
                    String.format("%d Essential SKUs Completely Depleted", zeroStock.size()),
                    String.format("%d inventory parts have 0 units in depot. Field dispatches requiring these parts will experience indefinite delays.", zeroStock.size()),
                    "WARNING",
                    "INVENTORY",
                    "INVENTORY",
                    "DEPOT",
                    0.0,
                    (double) zeroStock.size(),
                    "stockout SKUs",
                    "Issue expedited purchase orders for " + zeroStock.get(0).getName() + "."
            ));
        }

        // 5. UNUSUALLY HIGH AVERAGE DURATION TICKETS
        long highDurationTickets = openOrders.stream()
                .filter(w -> w.getEstimatedDurationHours() != null && w.getEstimatedDurationHours() >= 3.5)
                .count();

        if (highDurationTickets >= 3) {
            list.add(new OperationalAnomaly(
                    "ANOM-TIME-DURATION",
                    String.format("%d Long-Duration Heavy Maintenance Tickets", highDurationTickets),
                    String.format("%d work orders currently require 3.5+ hours of on-site service, tieing up senior engineering bandwidth.", highDurationTickets),
                    "INFO",
                    "TIMING",
                    "WORK_ORDER",
                    "FLEET",
                    1.5,
                    (double) highDurationTickets,
                    "heavy orders",
                    "Consider 2-technician pairing to compress total resolution duration."
            ));
        }

        return list;
    }
}

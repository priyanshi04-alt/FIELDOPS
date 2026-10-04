package com.fieldops.analytics;

import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianDTO;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.workorder.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;

    public DashboardService(WorkOrderRepository workOrderRepository, TechnicianRepository technicianRepository) {
        this.workOrderRepository = workOrderRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewDTO getDashboardOverview() {
        List<WorkOrder> allWorkOrders = workOrderRepository.findAll();
        List<Technician> allTechnicians = technicianRepository.findAll();

        long activeJobs = allWorkOrders.stream()
                .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED && wo.getStatus() != WorkOrderStatus.CANCELLED)
                .count();

        long slaAtRisk = allWorkOrders.stream()
                .filter(wo -> wo.getSlaBreachProbability() != null && wo.getSlaBreachProbability() >= 60.0 &&
                        wo.getStatus() != WorkOrderStatus.COMPLETED && wo.getStatus() != WorkOrderStatus.CANCELLED)
                .count();

        long availableTechnicians = allTechnicians.stream()
                .filter(t -> t.getStatus() == TechnicianStatus.AVAILABLE)
                .count();

        long completedToday = allWorkOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.COMPLETED)
                .count();

        Map<String, Long> pipelineCounts = new LinkedHashMap<>();
        for (WorkOrderStatus status : WorkOrderStatus.values()) {
            long count = allWorkOrders.stream().filter(wo -> wo.getStatus() == status).count();
            pipelineCounts.put(status.name(), count);
        }

        List<WorkOrderDTO> highRisk = allWorkOrders.stream()
                .filter(wo -> wo.getSlaBreachProbability() != null && wo.getSlaBreachProbability() >= 60.0 &&
                        wo.getStatus() != WorkOrderStatus.COMPLETED && wo.getStatus() != WorkOrderStatus.CANCELLED)
                .sorted((a, b) -> Double.compare(b.getSlaBreachProbability(), a.getSlaBreachProbability()))
                .limit(6)
                .map(WorkOrderDTO::fromEntity)
                .collect(Collectors.toList());

        List<WorkOrderDTO> activeWOs = allWorkOrders.stream()
                .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED && wo.getStatus() != WorkOrderStatus.CANCELLED)
                .map(WorkOrderDTO::fromEntity)
                .collect(Collectors.toList());

        List<Map<String, Object>> utilizationData = allTechnicians.stream()
                .limit(8)
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", t.getName().split(" ")[0]);
                    map.put("fullName", t.getName());
                    map.put("utilization", t.getUtilizationRate() != null ? t.getUtilizationRate() : 75.0);
                    map.put("jobs", t.getActiveJobsCount());
                    return map;
                })
                .collect(Collectors.toList());

        List<RecentActivityDTO> activities = createRecentActivities();

        return DashboardOverviewDTO.builder()
                .activeJobs(activeJobs)
                .activeJobsTrend("+12% vs last week")
                .slaAtRisk(slaAtRisk)
                .slaAtRiskTrend("-3% critical alerts")
                .availableTechnicians(availableTechnicians)
                .totalTechnicians((long) allTechnicians.size())
                .technicianAvailabilityTrend(availableTechnicians + " ready for dispatch")
                .completedToday(completedToday)
                .completedTodayTrend("+18% resolution efficiency")
                .pipelineCounts(pipelineCounts)
                .highRiskJobs(highRisk)
                .technicians(allTechnicians.stream().map(TechnicianDTO::fromEntity).collect(Collectors.toList()))
                .activeWorkOrders(activeWOs)
                .technicianUtilization(utilizationData)
                .recentActivities(activities)
                .build();
    }

    private List<RecentActivityDTO> createRecentActivities() {
        List<RecentActivityDTO> list = new ArrayList<>();
        list.add(RecentActivityDTO.builder()
                .id(1L)
                .title("SLA warning triggered")
                .description("WO-1042 reached 82% SLA breach probability in Gurgaon sector 44")
                .type("SLA_WARNING")
                .timeAgo("3m ago")
                .timestamp(LocalDateTime.now().minusMinutes(3))
                .actorName("SLA Engine")
                .build());

        list.add(RecentActivityDTO.builder()
                .id(2L)
                .title("Rahul Sharma accepted WO-1042")
                .description("Dispatched for Critical HVAC Inverter Failure")
                .type("WO_ASSIGNED")
                .timeAgo("14m ago")
                .timestamp(LocalDateTime.now().minusMinutes(14))
                .actorName("Rahul Sharma")
                .build());

        list.add(RecentActivityDTO.builder()
                .id(3L)
                .title("WO-1039 marked Completed")
                .description("Fiber optic splice verified with 0.02dB loss threshold")
                .type("WO_COMPLETED")
                .timeAgo("35m ago")
                .timestamp(LocalDateTime.now().minusMinutes(35))
                .actorName("Priya Verma")
                .build());

        list.add(RecentActivityDTO.builder()
                .id(4L)
                .title("Inventory threshold alert")
                .description("10Gbps SFP+ Transceiver stock dipped below safety limit (8 units remaining)")
                .type("INVENTORY_UPDATED")
                .timeAgo("1h ago")
                .timestamp(LocalDateTime.now().minusHours(1))
                .actorName("Inventory Central")
                .build());

        list.add(RecentActivityDTO.builder()
                .id(5L)
                .title("Technician route optimized")
                .description("Amit Patel rerouted to avoid Delhi-Gurgaon Expressway congestion (-18 min ETA)")
                .type("ROUTE_OPTIMIZED")
                .timeAgo("2h ago")
                .timestamp(LocalDateTime.now().minusHours(2))
                .actorName("Dispatch AI")
                .build());

        return list;
    }
}

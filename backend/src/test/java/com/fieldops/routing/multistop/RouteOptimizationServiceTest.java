package com.fieldops.routing.multistop;

import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouteOptimizationServiceTest {

    private RouteOptimizationService routeOptimizationService;
    private Technician technician;

    @BeforeEach
    void setUp() {
        routeOptimizationService = new RouteOptimizationService();
        technician = Technician.builder()
                .id(104L)
                .name("Rahul Sharma")
                .city("Delhi")
                .status(TechnicianStatus.AVAILABLE)
                .latitude(28.6139)
                .longitude(77.2090)
                .build();
    }

    @Test
    @DisplayName("Edge Case: Zero jobs returns empty route gracefully with 100 score")
    void testZeroJobs() {
        OptimizedRoute result = routeOptimizationService.optimizeRoute(technician, Collections.emptyList(), LocalDateTime.now());
        assertNotNull(result);
        assertEquals(0, result.getTotalStops());
        assertEquals(0.0, result.getTotalDistanceKm());
        assertEquals(100, result.getOptimizationScore());
        assertTrue(result.getExplanation().contains("No assigned or pending work orders"));
    }

    @Test
    @DisplayName("Edge Case: Single job creates single leg and stop")
    void testSingleJob() {
        WorkOrder wo = WorkOrder.builder()
                .id(101L)
                .workOrderNumber("WO-101")
                .title("Power Supply Replacement")
                .priority(WorkOrderPriority.HIGH)
                .status(WorkOrderStatus.NEW)
                .latitude(28.6304)
                .longitude(77.2177)
                .slaDeadline(LocalDateTime.now().plusHours(4))
                .estimatedDurationHours(1.0)
                .build();

        OptimizedRoute route = routeOptimizationService.optimizeRoute(technician, List.of(wo), LocalDateTime.now());
        assertNotNull(route);
        assertEquals(1, route.getTotalStops());
        assertEquals(1, route.getLegs().size());
        assertTrue(route.getTotalDistanceKm() > 0);
        assertEquals("WO-101", route.getStops().get(0).getWorkOrderNumber());
        assertEquals("ON_TRACK", route.getStops().get(0).getSlaStatus());
    }

    @Test
    @DisplayName("Edge Case: Two jobs with varying SLAs prioritize urgent critical deadline")
    void testTwoJobsSlaPriority() {
        LocalDateTime now = LocalDateTime.now();

        // Far job but critical with imminent 1-hour SLA
        WorkOrder urgentWo = WorkOrder.builder()
                .id(201L)
                .workOrderNumber("WO-URGENT")
                .title("Substation Emergency")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.NEW)
                .latitude(28.7041)
                .longitude(77.1025)
                .slaDeadline(now.plusHours(1))
                .estimatedDurationHours(1.0)
                .build();

        // Close job but low priority with 8-hour SLA
        WorkOrder relaxedWo = WorkOrder.builder()
                .id(202L)
                .workOrderNumber("WO-RELAXED")
                .title("Routine Cleaning")
                .priority(WorkOrderPriority.LOW)
                .status(WorkOrderStatus.NEW)
                .latitude(28.6200)
                .longitude(77.2100)
                .slaDeadline(now.plusHours(8))
                .estimatedDurationHours(1.0)
                .build();

        OptimizedRoute route = routeOptimizationService.optimizeRoute(technician, List.of(relaxedWo, urgentWo), now);
        assertEquals(2, route.getTotalStops());
        // Urgent job should be scheduled first to avoid contract violation
        assertEquals("WO-URGENT", route.getStops().get(0).getWorkOrderNumber());
        assertEquals("WO-RELAXED", route.getStops().get(1).getWorkOrderNumber());
    }

    @Test
    @DisplayName("Multi-stop: 4 jobs optimized with measurable savings and explanation")
    void testMultiStopRouteOptimization() {
        LocalDateTime now = LocalDateTime.now();

        List<WorkOrder> jobs = new ArrayList<>();
        // Stop A - Connaught Place
        jobs.add(WorkOrder.builder()
                .id(1L).workOrderNumber("WO-1042").title("Critical Cooling").priority(WorkOrderPriority.CRITICAL)
                .latitude(28.6315).longitude(77.2167).slaDeadline(now.plusHours(2)).estimatedDurationHours(1.5).build());

        // Stop B - South Extension
        jobs.add(WorkOrder.builder()
                .id(2L).workOrderNumber("WO-1051").title("Pump Sensor Calibration").priority(WorkOrderPriority.HIGH)
                .latitude(28.5684).longitude(77.2209).slaDeadline(now.plusHours(4)).estimatedDurationHours(1.0).build());

        // Stop C - Nehru Place
        jobs.add(WorkOrder.builder()
                .id(3L).workOrderNumber("WO-1068").title("Switchboard Diagnostics").priority(WorkOrderPriority.MEDIUM)
                .latitude(28.5494).longitude(77.2536).slaDeadline(now.plusHours(6)).estimatedDurationHours(1.5).build());

        // Stop D - Noida Sector 62
        jobs.add(WorkOrder.builder()
                .id(4L).workOrderNumber("WO-1073").title("Filter Inspection").priority(WorkOrderPriority.LOW)
                .latitude(28.6280).longitude(77.3649).slaDeadline(now.plusHours(8)).estimatedDurationHours(1.0).build());

        OptimizedRoute route = routeOptimizationService.optimizeRoute(technician, jobs, now);

        assertEquals(4, route.getTotalStops());
        assertEquals(4, route.getLegs().size());
        assertTrue(route.getTotalDistanceKm() > 10.0);
        assertTrue(route.getTotalDurationMinutes() > 100);
        assertNotNull(route.getProjectedCompletionTime());
        assertTrue(route.getOptimizationScore() >= 50);
        assertNotNull(route.getExplanation());
        assertTrue(route.getExplanation().contains("Rahul Sharma"));
    }

    @Test
    @DisplayName("Edge Case: Identical coordinates and missing coordinates handled safely")
    void testIdenticalAndMissingCoordinates() {
        LocalDateTime now = LocalDateTime.now();

        WorkOrder job1 = WorkOrder.builder()
                .id(301L).workOrderNumber("WO-IDENT-1").title("Server A").priority(WorkOrderPriority.HIGH)
                .latitude(28.6139).longitude(77.2090).slaDeadline(now.plusHours(3)).build();

        WorkOrder job2 = WorkOrder.builder()
                .id(302L).workOrderNumber("WO-IDENT-2").title("Server B").priority(WorkOrderPriority.HIGH)
                .latitude(28.6139).longitude(77.2090).slaDeadline(now.plusHours(3)).build();

        WorkOrder job3 = WorkOrder.builder()
                .id(303L).workOrderNumber("WO-MISSING-GEO").title("Remote Depot").priority(WorkOrderPriority.MEDIUM)
                .latitude(null).longitude(null).slaDeadline(now.plusHours(4)).build();

        OptimizedRoute route = routeOptimizationService.optimizeRoute(technician, List.of(job1, job2, job3), now);
        assertEquals(3, route.getTotalStops());
        assertTrue(route.getTotalDistanceKm() >= 0);
    }

    @Test
    @DisplayName("Edge Case: Impossible SLA flags VIOLATION deterministically without failure")
    void testImpossibleSlaDetection() {
        LocalDateTime now = LocalDateTime.now();

        // Impossible SLA: expired 30 minutes ago
        WorkOrder expiredJob = WorkOrder.builder()
                .id(401L).workOrderNumber("WO-BREACH").title("Breached Transformer").priority(WorkOrderPriority.CRITICAL)
                .latitude(28.6500).longitude(77.2300).slaDeadline(now.minusMinutes(30)).estimatedDurationHours(1.0).build();

        OptimizedRoute route = routeOptimizationService.optimizeRoute(technician, List.of(expiredJob), now);
        assertEquals(1, route.getTotalStops());
        assertEquals("VIOLATION", route.getStops().get(0).getSlaStatus());
        assertEquals(1, route.getSlaViolationCount());
        assertTrue(route.getOptimizationScore() <= 70);
    }
}

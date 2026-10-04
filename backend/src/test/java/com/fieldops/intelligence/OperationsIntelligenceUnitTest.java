package com.fieldops.intelligence;

import com.fieldops.assignment.AssignmentScoringService;
import com.fieldops.assignment.CandidateTechnicianScore;
import com.fieldops.routing.GeoUtils;
import com.fieldops.sla.SlaRiskResult;
import com.fieldops.sla.SlaRiskService;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OperationsIntelligenceUnitTest {

    private AssignmentScoringService scoringService;
    private SlaRiskService slaRiskService;

    @BeforeEach
    void setUp() {
        scoringService = new AssignmentScoringService();
        slaRiskService = new SlaRiskService(null);
    }

    @Test
    @DisplayName("Smart Assignment: Fully qualified, nearby, available technician scores higher than unqualified tech")
    void testTechnicianAssignmentScoring() {
        WorkOrder wo = WorkOrder.builder()
                .id(1L)
                .workOrderNumber("WO-1058")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.NEW)
                .latitude(28.5355)
                .longitude(77.3910) // Noida
                .requiredSkills(Set.of("HVAC", "Electrical"))
                .slaDeadline(LocalDateTime.now().plusHours(2))
                .build();

        // Qualified tech 3km away, available, 0 workload
        Technician qualifiedTech = Technician.builder()
                .id(101L)
                .name("Rahul Sharma")
                .status(TechnicianStatus.AVAILABLE)
                .latitude(28.5450)
                .longitude(77.3850)
                .skills(Set.of("HVAC", "Electrical", "Diagnostics"))
                .activeJobsCount(0)
                .rating(4.9)
                .slaSuccessRate(98.0)
                .build();

        // Unqualified tech 25km away, busy, 3 workload
        Technician unqualifiedTech = Technician.builder()
                .id(102L)
                .name("Farhan Akhtar")
                .status(TechnicianStatus.BUSY)
                .latitude(28.7041)
                .longitude(77.1025)
                .skills(Set.of("Plumbing"))
                .activeJobsCount(3)
                .rating(4.2)
                .slaSuccessRate(85.0)
                .build();

        CandidateTechnicianScore score1 = scoringService.evaluateCandidate(wo, qualifiedTech);
        CandidateTechnicianScore score2 = scoringService.evaluateCandidate(wo, unqualifiedTech);

        assertTrue(score1.getTotalScore() > 80, "Qualified candidate should score > 80");
        assertTrue(score2.getTotalScore() < 50, "Unqualified far candidate should score < 50");
        assertTrue(score1.getTotalScore() > score2.getTotalScore(), "Qualified technician must rank strictly above unqualified technician");
        assertEquals(35, score1.getSkillScore(), "Full skills match should yield max 35 skill points");
        assertEquals(0, score2.getSkillScore(), "Zero matching skills should yield 0 skill points");
    }

    @Test
    @DisplayName("SLA Risk: Expired or tight deadline with far tech yields high risk, relaxed deadline yields low risk")
    void testSlaRiskEvaluation() {
        // Critical job, only 15 min remaining, technician 22 km away
        Technician busyFarTech = Technician.builder()
                .id(201L)
                .name("Amit Kumar")
                .latitude(28.7041)
                .longitude(77.1025)
                .activeJobsCount(4)
                .build();

        WorkOrder tightOrder = WorkOrder.builder()
                .id(2L)
                .workOrderNumber("WO-1042")
                .priority(WorkOrderPriority.CRITICAL)
                .status(WorkOrderStatus.IN_PROGRESS)
                .technician(busyFarTech)
                .latitude(28.4595)
                .longitude(77.0266)
                .slaDeadline(LocalDateTime.now().plusMinutes(15))
                .estimatedDurationHours(3.5)
                .build();

        SlaRiskResult highRisk = slaRiskService.calculateRisk(tightOrder);
        assertTrue(highRisk.getRiskScore() >= 60, "Tight SLA and far tech must produce HIGH or CRITICAL risk");
        assertTrue(highRisk.getRiskLevel().equals("HIGH") || highRisk.getRiskLevel().equals("CRITICAL"));

        // Low priority job, 8 hours remaining, nearby available tech
        Technician nearTech = Technician.builder()
                .id(202L)
                .name("Karan Joshi")
                .latitude(28.4600)
                .longitude(77.0270)
                .activeJobsCount(0)
                .build();

        WorkOrder safeOrder = WorkOrder.builder()
                .id(3L)
                .workOrderNumber("WO-1099")
                .priority(WorkOrderPriority.LOW)
                .status(WorkOrderStatus.ASSIGNED)
                .technician(nearTech)
                .latitude(28.4595)
                .longitude(77.0266)
                .slaDeadline(LocalDateTime.now().plusHours(8))
                .estimatedDurationHours(1.0)
                .build();

        SlaRiskResult safeRisk = slaRiskService.calculateRisk(safeOrder);
        assertTrue(safeRisk.getRiskScore() < 30, "Low priority nearby job must have LOW risk");
        assertEquals("LOW", safeRisk.getRiskLevel());
    }

    @Test
    @DisplayName("Route Intelligence: Haversine distance and transit estimation calculations")
    void testGeoAndTransitCalculations() {
        // Distance between Connaught Place (28.6315, 77.2167) and Cyber Hub Gurgaon (28.4950, 77.0895) ~ 19.5 km
        double dist = GeoUtils.calculateHaversineDistanceKm(28.6315, 77.2167, 28.4950, 77.0895);
        assertTrue(dist > 15.0 && dist < 25.0, "Distance should be approximately 19-20 km");

        int eta = GeoUtils.estimateTravelMinutes(dist);
        assertTrue(eta >= 35 && eta <= 65, "Transit time for ~20 km in city traffic should be ~45-55 mins");

        // Edge case: same coordinates
        double zeroDist = GeoUtils.calculateHaversineDistanceKm(28.5, 77.2, 28.5, 77.2);
        assertTrue(zeroDist <= 0.2, "Identical coordinates should yield near 0 distance");
    }

    @Test
    @DisplayName("Work Order Completed: Produces 0 SLA risk")
    void testCompletedWorkOrderZeroRisk() {
        WorkOrder done = WorkOrder.builder()
                .id(4L)
                .workOrderNumber("WO-1001")
                .status(WorkOrderStatus.COMPLETED)
                .priority(WorkOrderPriority.HIGH)
                .build();

        SlaRiskResult result = slaRiskService.calculateRisk(done);
        assertEquals(0, result.getRiskScore(), "Completed job must have 0 risk");
        assertEquals("LOW", result.getRiskLevel());
    }
}

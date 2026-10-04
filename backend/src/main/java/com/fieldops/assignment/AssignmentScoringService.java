package com.fieldops.assignment;

import com.fieldops.routing.GeoUtils;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.workorder.WorkOrder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AssignmentScoringService {

    public CandidateTechnicianScore evaluateCandidate(WorkOrder workOrder, Technician tech) {
        CandidateTechnicianScore score = new CandidateTechnicianScore();
        score.setTechnicianId(tech.getId());
        score.setName(tech.getName());
        score.setAvatarUrl(tech.getAvatarUrl());
        score.setStatus(tech.getStatus());
        score.setRating(tech.getRating());

        List<AssignmentFactor> factorList = new ArrayList<>();

        // 1. SKILL MATCH (35%)
        Set<String> required = workOrder.getRequiredSkills() != null ? workOrder.getRequiredSkills() : Collections.emptySet();
        Set<String> techSkills = tech.getSkills() != null ? tech.getSkills() : Collections.emptySet();

        Set<String> matched = new HashSet<>();
        Set<String> missing = new HashSet<>();

        int skillScore = 35;
        if (!required.isEmpty()) {
            for (String req : required) {
                boolean has = techSkills.stream().anyMatch(s -> s.equalsIgnoreCase(req.trim()));
                if (has) matched.add(req);
                else missing.add(req);
            }
            double ratio = (double) matched.size() / required.size();
            skillScore = (int) Math.round(ratio * 35.0);
            factorList.add(new AssignmentFactor("SKILL_MATCH", skillScore, 35,
                    missing.isEmpty()
                            ? String.format("Has all %d required skill competencies (%s).", required.size(), String.join(", ", matched))
                            : String.format("Matches %d of %d skills. Missing: %s", matched.size(), required.size(), String.join(", ", missing))));
        } else {
            factorList.add(new AssignmentFactor("SKILL_MATCH", 35, 35, "Standard operational scope (no special certifications required)."));
        }
        score.setSkillScore(skillScore);
        score.setMatchedSkills(matched);
        score.setMissingSkills(missing);

        // 2. DISTANCE (25%)
        double distanceKm = GeoUtils.calculateHaversineDistanceKm(
                tech.getLatitude(), tech.getLongitude(),
                workOrder.getLatitude(), workOrder.getLongitude()
        );
        score.setDistanceKm(distanceKm);
        int etaMinutes = GeoUtils.estimateTravelMinutes(distanceKm);
        score.setEstimatedEtaMinutes(etaMinutes);

        int distanceScore;
        if (distanceKm <= 3.0) {
            distanceScore = 25;
            factorList.add(new AssignmentFactor("DISTANCE", 25, 25,
                    String.format("Immediate operational proximity: %.1f km away (~%d min transit).", distanceKm, etaMinutes)));
        } else if (distanceKm <= 7.0) {
            distanceScore = 20;
            factorList.add(new AssignmentFactor("DISTANCE", 20, 25,
                    String.format("Close vicinity: %.1f km away (~%d min transit).", distanceKm, etaMinutes)));
        } else if (distanceKm <= 15.0) {
            distanceScore = 14;
            factorList.add(new AssignmentFactor("DISTANCE", 14, 25,
                    String.format("Moderate distance: %.1f km away (~%d min transit).", distanceKm, etaMinutes)));
        } else if (distanceKm <= 30.0) {
            distanceScore = 7;
            factorList.add(new AssignmentFactor("DISTANCE", 7, 25,
                    String.format("Long-range transit: %.1f km away (~%d min transit).", distanceKm, etaMinutes)));
        } else {
            distanceScore = 2;
            factorList.add(new AssignmentFactor("DISTANCE", 2, 25,
                    String.format("Extreme perimeter transit: %.1f km away.", distanceKm)));
        }
        score.setDistanceScore(distanceScore);

        // 3. AVAILABILITY (15%)
        int availabilityScore;
        if (tech.getStatus() == TechnicianStatus.AVAILABLE) {
            availabilityScore = 15;
            factorList.add(new AssignmentFactor("AVAILABILITY", 15, 15, "Technician is on-duty and immediately available."));
        } else if (tech.getStatus() == TechnicianStatus.ON_ROUTE) {
            availabilityScore = 9;
            factorList.add(new AssignmentFactor("AVAILABILITY", 9, 15, "Technician currently on route; can queue as next task."));
        } else if (tech.getStatus() == TechnicianStatus.BUSY) {
            availabilityScore = 5;
            factorList.add(new AssignmentFactor("AVAILABILITY", 5, 15, "Technician actively engaged in on-site servicing."));
        } else {
            availabilityScore = 0;
            factorList.add(new AssignmentFactor("AVAILABILITY", 0, 15, "Technician currently off-duty."));
        }
        score.setAvailabilityScore(availabilityScore);

        // 4. CURRENT WORKLOAD (10%)
        int activeJobs = tech.getActiveJobsCount() != null ? tech.getActiveJobsCount() : 0;
        int workloadScore;
        if (activeJobs == 0) {
            workloadScore = 10;
            factorList.add(new AssignmentFactor("WORKLOAD", 10, 10, "Zero active backlog. Optimal capacity."));
        } else if (activeJobs == 1) {
            workloadScore = 8;
            factorList.add(new AssignmentFactor("WORKLOAD", 8, 10, "Light backlog: 1 concurrent active ticket."));
        } else if (activeJobs == 2) {
            workloadScore = 5;
            factorList.add(new AssignmentFactor("WORKLOAD", 5, 10, "Moderate backlog: 2 active tickets."));
        } else {
            workloadScore = 2;
            factorList.add(new AssignmentFactor("WORKLOAD", 2, 10, String.format("High backlog: %d active tickets queued.", activeJobs)));
        }
        score.setWorkloadScore(workloadScore);

        // 5. SLA FEASIBILITY (10%)
        long slaRemainingMin = 180;
        if (workOrder.getSlaDeadline() != null) {
            Duration dur = Duration.between(LocalDateTime.now(), workOrder.getSlaDeadline());
            slaRemainingMin = dur.toMinutes();
        }

        int slaScore;
        if (slaRemainingMin <= 0) {
            slaScore = 2;
            factorList.add(new AssignmentFactor("SLA_FEASIBILITY", 2, 10, "Ticket is already past contractual deadline."));
        } else if (etaMinutes < (slaRemainingMin - 30)) {
            slaScore = 10;
            factorList.add(new AssignmentFactor("SLA_FEASIBILITY", 10, 10,
                    String.format("Ample arrival margin (%d min ETA vs %d min SLA remaining).", etaMinutes, slaRemainingMin)));
        } else if (etaMinutes < slaRemainingMin) {
            slaScore = 6;
            factorList.add(new AssignmentFactor("SLA_FEASIBILITY", 6, 10,
                    String.format("Tight arrival window (%d min ETA vs %d min SLA remaining).", etaMinutes, slaRemainingMin)));
        } else {
            slaScore = 1;
            factorList.add(new AssignmentFactor("SLA_FEASIBILITY", 1, 10,
                    String.format("High SLA breach hazard: ETA (%d min) exceeds remaining SLA (%d min).", etaMinutes, slaRemainingMin)));
        }
        score.setSlaFeasibilityScore(slaScore);

        // 6. HISTORICAL PERFORMANCE (5%)
        double rating = tech.getRating() != null ? tech.getRating() : 4.5;
        double slaSuccess = tech.getSlaSuccessRate() != null ? tech.getSlaSuccessRate() : 95.0;
        int perfScore;
        if (rating >= 4.8 && slaSuccess >= 95.0) {
            perfScore = 5;
            factorList.add(new AssignmentFactor("PERFORMANCE", 5, 5,
                    String.format("Top-tier track record: %.1f★ CSAT, %.1f%% on-time SLA history.", rating, slaSuccess)));
        } else if (rating >= 4.5 && slaSuccess >= 90.0) {
            perfScore = 4;
            factorList.add(new AssignmentFactor("PERFORMANCE", 4, 5,
                    String.format("Strong track record: %.1f★ CSAT, %.1f%% on-time SLA history.", rating, slaSuccess)));
        } else {
            perfScore = 2;
            factorList.add(new AssignmentFactor("PERFORMANCE", 2, 5,
                    String.format("Standard reliability metrics: %.1f★ CSAT.", rating)));
        }
        score.setPerformanceScore(perfScore);

        int total = skillScore + distanceScore + availabilityScore + workloadScore + slaScore + perfScore;
        score.setTotalScore(Math.min(100, Math.max(0, total)));
        score.setFactors(factorList);

        return score;
    }
}

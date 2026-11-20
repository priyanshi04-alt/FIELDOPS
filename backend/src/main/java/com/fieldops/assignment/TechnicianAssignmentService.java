package com.fieldops.assignment;

import com.fieldops.audit.DecisionAuditService;
import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.technician.TechnicianStatus;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderDTO;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.fieldops.websocket.OperationsEvent;
import com.fieldops.websocket.OperationsEventPublisher;
import com.fieldops.websocket.OperationsEventType;

@Service
public class TechnicianAssignmentService {

    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;
    private final AssignmentScoringService scoringService;
    private final DecisionAuditService auditService;
    private final OperationsEventPublisher eventPublisher;

    public TechnicianAssignmentService(WorkOrderRepository workOrderRepository,
                                       TechnicianRepository technicianRepository,
                                       AssignmentScoringService scoringService,
                                       DecisionAuditService auditService,
                                       OperationsEventPublisher eventPublisher) {
        this.workOrderRepository = workOrderRepository;
        this.technicianRepository = technicianRepository;
        this.scoringService = scoringService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public AssignmentRecommendation getRecommendation(Long workOrderId) {
        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + workOrderId));

        return computeRecommendation(wo, technicianRepository.findAll());
    }

    /**
     * Stage B in-memory candidate evaluation and ranking.
     * Operates 100% in-memory without initiating database queries or holding transactions.
     */
    public AssignmentRecommendation computeRecommendation(WorkOrder wo, List<Technician> allTechnicians) {
        if (wo == null) {
            return null;
        }

        List<Technician> techList = allTechnicians != null ? allTechnicians : List.of();
        List<CandidateTechnicianScore> candidates = techList.stream()
                .map(tech -> scoringService.evaluateCandidate(wo, tech))
                .sorted(Comparator.comparingInt(CandidateTechnicianScore::getTotalScore).reversed())
                .collect(Collectors.toList());

        CandidateTechnicianScore top = candidates.isEmpty() ? null : candidates.get(0);

        return new AssignmentRecommendation(
                wo.getId(),
                wo.getWorkOrderNumber(),
                wo.getPriority() != null ? wo.getPriority().name() : "MEDIUM",
                top,
                candidates
        );
    }

    @Transactional
    public WorkOrderDTO acceptRecommendation(Long workOrderId, Long technicianId, String dispatcherEmail) {
        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + workOrderId));

        AssignmentRecommendation recommendation = getRecommendation(workOrderId);
        CandidateTechnicianScore topCandidate = recommendation.getTopCandidate();

        Long targetTechId = technicianId != null ? technicianId : (topCandidate != null ? topCandidate.getTechnicianId() : null);
        if (targetTechId == null) {
            throw new IllegalArgumentException("No technician available to accept for work order " + workOrderId);
        }

        Technician tech = technicianRepository.findById(targetTechId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + targetTechId));

        CandidateTechnicianScore assignedScore = recommendation.getCandidates().stream()
                .filter(c -> c.getTechnicianId().equals(targetTechId))
                .findFirst()
                .orElse(topCandidate);

        wo.setTechnician(tech);
        if (wo.getStatus() == WorkOrderStatus.NEW) {
            wo.setStatus(WorkOrderStatus.ASSIGNED);
        }
        if (assignedScore != null) {
            wo.setDistanceKm(assignedScore.getDistanceKm());
            wo.setTechnicianEtaMinutes(assignedScore.getEstimatedEtaMinutes());
        }

        // Update technician active job count
        tech.setActiveJobsCount((tech.getActiveJobsCount() != null ? tech.getActiveJobsCount() : 0) + 1);
        if (tech.getStatus() == TechnicianStatus.AVAILABLE) {
            tech.setStatus(TechnicianStatus.ON_ROUTE);
        }
        technicianRepository.save(tech);
        WorkOrder saved = workOrderRepository.save(wo);

        // Audit Trail
        String explanation = String.format("Accepted recommended assignment of %s (Match Score: %d%%)",
                tech.getName(), assignedScore != null ? assignedScore.getTotalScore() : 90);
        auditService.recordEvent(
                wo.getId(),
                wo.getWorkOrderNumber(),
                "RECOMMENDATION_ACCEPTED",
                topCandidate != null ? topCandidate.getTechnicianId() : null,
                topCandidate != null ? topCandidate.getName() : null,
                tech.getId(),
                tech.getName(),
                assignedScore != null ? assignedScore.getTotalScore() : 90,
                explanation,
                dispatcherEmail
        );

        eventPublisher.publish(OperationsEvent.builder()
                .eventType(OperationsEventType.WORK_ORDER_ASSIGNED)
                .severity("INFO")
                .workOrderId(saved.getId())
                .technicianId(tech.getId())
                .message(String.format("Work order %s dispatched to %s (Match: %d%%)",
                        saved.getWorkOrderNumber(), tech.getName(), assignedScore != null ? assignedScore.getTotalScore() : 90))
                .build());

        return WorkOrderDTO.fromEntity(saved);
    }

    @Transactional
    public WorkOrderDTO rejectRecommendation(Long workOrderId, Long selectedTechnicianId, String reason, String dispatcherEmail) {
        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + workOrderId));

        AssignmentRecommendation recommendation = getRecommendation(workOrderId);
        CandidateTechnicianScore topCandidate = recommendation.getTopCandidate();

        Technician selectedTech = null;
        Integer score = null;
        if (selectedTechnicianId != null) {
            selectedTech = technicianRepository.findById(selectedTechnicianId)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + selectedTechnicianId));
            wo.setTechnician(selectedTech);
            if (wo.getStatus() == WorkOrderStatus.NEW) {
                wo.setStatus(WorkOrderStatus.ASSIGNED);
            }
            selectedTech.setActiveJobsCount((selectedTech.getActiveJobsCount() != null ? selectedTech.getActiveJobsCount() : 0) + 1);
            technicianRepository.save(selectedTech);

            CandidateTechnicianScore match = recommendation.getCandidates().stream()
                    .filter(c -> c.getTechnicianId().equals(selectedTechnicianId))
                    .findFirst().orElse(null);
            if (match != null) {
                score = match.getTotalScore();
                wo.setDistanceKm(match.getDistanceKm());
                wo.setTechnicianEtaMinutes(match.getEstimatedEtaMinutes());
            }
        }

        WorkOrder saved = workOrderRepository.save(wo);

        // Audit Trail
        String explanation = String.format("Dispatcher rejected recommendation %s. Reason: %s",
                topCandidate != null ? topCandidate.getName() : "None",
                reason != null && !reason.isBlank() ? reason : "Operational manual override"
        );

        auditService.recordEvent(
                wo.getId(),
                wo.getWorkOrderNumber(),
                "RECOMMENDATION_REJECTED",
                topCandidate != null ? topCandidate.getTechnicianId() : null,
                topCandidate != null ? topCandidate.getName() : null,
                selectedTech != null ? selectedTech.getId() : null,
                selectedTech != null ? selectedTech.getName() : null,
                score,
                explanation,
                dispatcherEmail
        );

        return WorkOrderDTO.fromEntity(saved);
    }
}

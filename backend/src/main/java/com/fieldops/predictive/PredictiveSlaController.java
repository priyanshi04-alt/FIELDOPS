package com.fieldops.predictive;

import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla/predictive")
public class PredictiveSlaController {

    private final PredictiveSlaService predictiveSlaService;
    private final WorkOrderRepository workOrderRepository;

    public PredictiveSlaController(PredictiveSlaService predictiveSlaService,
                                   WorkOrderRepository workOrderRepository) {
        this.predictiveSlaService = predictiveSlaService;
        this.workOrderRepository = workOrderRepository;
    }

    @GetMapping("/{workOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public PredictiveSlaResultDTO getPredictiveSla(
            @PathVariable Long workOrderId,
            Authentication authentication
    ) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"))) {
            WorkOrder wo = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + workOrderId));
            String callerEmail = authentication.getName();
            if (wo.getTechnician() == null || !com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, wo.getTechnician().getEmail())) {
                throw new AccessDeniedException("Access denied: You can only view predictive SLA risk for work orders assigned to you.");
            }
        }
        return predictiveSlaService.evaluatePredictiveSla(workOrderId);
    }

    @GetMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public List<PredictiveSlaResultDTO> getBatchPredictiveSla() {
        return predictiveSlaService.evaluateAllActive();
    }
}

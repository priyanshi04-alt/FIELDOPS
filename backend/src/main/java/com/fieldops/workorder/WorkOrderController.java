package com.fieldops.workorder;

import com.fieldops.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<WorkOrderDTO>>> getWorkOrders(
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) WorkOrderPriority priority,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        List<WorkOrderDTO> list = workOrderService.getAllWorkOrders(status, priority, technicianId, customerId, search);

        // IDOR Isolation for CUSTOMER role: Only return work orders belonging to caller's email
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CUSTOMER"))) {
            String callerEmail = authentication.getName();
            list = list.stream()
                    .filter(wo -> wo.getCustomer() != null && com.fieldops.common.IdentityMatcher.matchesCustomer(callerEmail, wo.getCustomer().getEmail()))
                    .toList();
        }

        // IDOR Isolation for TECHNICIAN role: If technician views list, filter to their assigned jobs
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"))) {
            String callerEmail = authentication.getName();
            list = list.stream()
                    .filter(wo -> wo.getTechnician() != null && com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, wo.getTechnician().getEmail()))
                    .toList();
        }

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> getWorkOrder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        WorkOrderDTO dto = workOrderService.getWorkOrderById(id);

        // Verify resource ownership if caller is CUSTOMER
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CUSTOMER"))) {
            String callerEmail = authentication.getName();
            if (dto.getCustomer() == null || !com.fieldops.common.IdentityMatcher.matchesCustomer(callerEmail, dto.getCustomer().getEmail())) {
                throw new AccessDeniedException("Access denied: You do not own this work order.");
            }
        }

        // Verify resource assignment if caller is TECHNICIAN
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"))) {
            String callerEmail = authentication.getName();
            if (dto.getTechnician() == null || !com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, dto.getTechnician().getEmail())) {
                throw new AccessDeniedException("Access denied: You are not assigned to this work order.");
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/by-number/{number}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> getWorkOrderByNumber(
            @PathVariable String number,
            Authentication authentication
    ) {
        WorkOrderDTO dto = workOrderService.getWorkOrderByNumber(number);

        // Verify resource ownership if caller is CUSTOMER
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CUSTOMER"))) {
            String callerEmail = authentication.getName();
            if (dto.getCustomer() == null || !com.fieldops.common.IdentityMatcher.matchesCustomer(callerEmail, dto.getCustomer().getEmail())) {
                throw new AccessDeniedException("Access denied: You do not own this work order.");
            }
        }

        // Verify resource assignment if caller is TECHNICIAN
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"))) {
            String callerEmail = authentication.getName();
            if (dto.getTechnician() == null || !com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, dto.getTechnician().getEmail())) {
                throw new AccessDeniedException("Access denied: You are not assigned to this work order.");
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/sla-at-risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<WorkOrderDTO>>> getHighRiskWorkOrders() {
        List<WorkOrderDTO> list = workOrderService.getHighRiskWorkOrders();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> createWorkOrder(@Valid @RequestBody WorkOrderCreateRequest request) {
        WorkOrderDTO dto = workOrderService.createWorkOrder(request);
        return ResponseEntity.ok(ApiResponse.ok("Work order created successfully", dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> updateWorkOrder(
            @PathVariable Long id,
            @RequestBody WorkOrderUpdateRequest request,
            Authentication authentication
    ) {
        boolean isTechnician = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"));

        if (isTechnician) {
            WorkOrderDTO existing = workOrderService.getWorkOrderById(id);
            String callerEmail = authentication.getName();
            if (existing.getTechnician() == null || !com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, existing.getTechnician().getEmail())) {
                throw new AccessDeniedException("Access denied: You are not assigned to this work order.");
            }

            // Prevent privilege escalation: technicians cannot modify dispatch-controlled fields
            if (request.getTechnicianId() != null || request.getPriority() != null ||
                    request.getSlaBreachProbability() != null || request.getTitle() != null ||
                    request.getDescription() != null || request.getScheduledStartTime() != null ||
                    request.getScheduledEndTime() != null || request.getTechnicianEtaMinutes() != null) {
                throw new AccessDeniedException("Access denied: Technicians cannot modify dispatch-controlled fields (technicianId, priority, SLA risk, scheduling, title/description).");
            }

            WorkOrderStatusUpdateRequest statusReq = new WorkOrderStatusUpdateRequest(
                    request.getStatus(),
                    request.getServiceNotes(),
                    null
            );
            WorkOrderDTO dto = workOrderService.updateWorkOrderStatus(id, statusReq, callerEmail, true);
            return ResponseEntity.ok(ApiResponse.ok("Work order updated successfully", dto));
        }

        WorkOrderDTO dto = workOrderService.updateWorkOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Work order updated successfully", dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> updateWorkOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderStatusUpdateRequest request,
            Authentication authentication
    ) {
        boolean isTechnician = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"));
        String callerEmail = authentication != null ? authentication.getName() : null;

        WorkOrderDTO dto = workOrderService.updateWorkOrderStatus(id, request, callerEmail, isTechnician);
        return ResponseEntity.ok(ApiResponse.ok("Work order status updated successfully", dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrder(@PathVariable Long id) {
        workOrderService.deleteWorkOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Work order deleted successfully", null));
    }
}

package com.fieldops.workorder;

import com.fieldops.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final com.fieldops.customer.CustomerRepository customerRepository;

    public WorkOrderController(WorkOrderService workOrderService,
                               com.fieldops.customer.CustomerRepository customerRepository) {
        this.workOrderService = workOrderService;
        this.customerRepository = customerRepository;
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "id", "priority", "status", "slaBreachProbability", "workOrderNumber", "slaDeadline", "title"
    );

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<WorkOrderDTO>>> getWorkOrders(
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) WorkOrderPriority priority,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            throw new IllegalArgumentException("Invalid sort field: " + sort + ". Allowed: " + ALLOWED_SORT_FIELDS);
        }

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        String customerEmail = null;
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CUSTOMER"))) {
            customerEmail = authentication.getName();
        }

        String technicianEmail = null;
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"))) {
            technicianEmail = authentication.getName();
        }

        Page<WorkOrderDTO> pagedResult = workOrderService.getWorkOrdersPaged(
                status, priority, technicianId, customerId, search, customerEmail, technicianEmail, pageRequest
        );

        ApiResponse<List<WorkOrderDTO>> response = ApiResponse.ok(pagedResult.getContent());
        response.setPage(pagedResult.getNumber());
        response.setSize(pagedResult.getSize());
        response.setTotalElements(pagedResult.getTotalElements());
        response.setTotalPages(pagedResult.getTotalPages());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pagedResult.getTotalElements()))
                .header("X-Page-Number", String.valueOf(pagedResult.getNumber()))
                .header("X-Page-Size", String.valueOf(pagedResult.getSize()))
                .header("X-Total-Pages", String.valueOf(pagedResult.getTotalPages()))
                .body(response);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> createWorkOrder(
            @Valid @RequestBody WorkOrderCreateRequest request,
            Authentication authentication
    ) {
        boolean isCustomer = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CUSTOMER"));

        if (isCustomer) {
            String callerEmail = authentication.getName();
            List<com.fieldops.customer.Customer> customers = customerRepository.findAll();
            com.fieldops.customer.Customer callerCustomer = customers.stream()
                    .filter(c -> com.fieldops.common.IdentityMatcher.matchesCustomer(callerEmail, c.getEmail()))
                    .findFirst()
                    .orElseThrow(() -> new AccessDeniedException("Access denied: No customer profile found matching authenticated user."));

            // If customerId was supplied, reject cross-customer IDOR attempts
            if (request.getCustomerId() != null && !request.getCustomerId().equals(callerCustomer.getId())) {
                throw new AccessDeniedException("Access denied: You cannot create work orders for other customers.");
            }

            // Always derive customer ID from authenticated identity
            request.setCustomerId(callerCustomer.getId());

            // Prevent customers from self-assigning technicians
            request.setTechnicianId(null);
        }

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

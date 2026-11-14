package com.fieldops.customer;

import com.fieldops.common.ApiResponse;
import com.fieldops.workorder.WorkOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final WorkOrderRepository workOrderRepository;

    public CustomerController(CustomerService customerService, WorkOrderRepository workOrderRepository) {
        this.customerService = customerService;
        this.workOrderRepository = workOrderRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getCustomers(
            @RequestParam(required = false) String city
    ) {
        List<CustomerDTO> customers = customerService.getAllCustomers(city);
        return ResponseEntity.ok(ApiResponse.ok(customers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'CUSTOMER', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomer(
            @PathVariable Long id,
            Authentication authentication
    ) {
        CustomerDTO customer = customerService.getCustomerById(id);

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Access denied: Authentication required.");
        }

        boolean isCustomer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CUSTOMER"));
        boolean isTechnician = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"));
        boolean isStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_DISPATCHER"));

        String callerEmail = authentication.getName();

        if (isCustomer) {
            if (!com.fieldops.common.IdentityMatcher.matchesCustomer(callerEmail, customer.getEmail())) {
                throw new AccessDeniedException("Access denied: You can only access your own customer profile.");
            }
        } else if (isTechnician) {
            boolean hasAssignedJob = workOrderRepository.findByCustomerId(id).stream()
                    .anyMatch(wo -> wo.getTechnician() != null && com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, wo.getTechnician().getEmail()));
            if (!hasAssignedJob) {
                throw new AccessDeniedException("Access denied: Technicians can only view customer profiles with assigned work orders.");
            }
        } else if (!isStaff) {
            throw new AccessDeniedException("Access denied: Insufficient privileges to view customer profile.");
        }

        return ResponseEntity.ok(ApiResponse.ok(customer));
    }
}

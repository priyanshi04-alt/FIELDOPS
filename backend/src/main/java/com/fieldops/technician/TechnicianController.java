package com.fieldops.technician;

import com.fieldops.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

    private final TechnicianService technicianService;

    public TechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<TechnicianDTO>>> getTechnicians(
            @RequestParam(required = false) TechnicianStatus status,
            @RequestParam(required = false) String city
    ) {
        List<TechnicianDTO> technicians = technicianService.getAllTechnicians(status, city);
        return ResponseEntity.ok(ApiResponse.ok(technicians));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<TechnicianDTO>> getTechnician(
            @PathVariable Long id,
            Authentication authentication
    ) {
        TechnicianDTO dto = technicianService.getTechnicianById(id);

        // IDOR Isolation: TECHNICIAN role can only access their own profile
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"))) {
            String callerEmail = authentication.getName();
            if (!com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, dto.getEmail())) {
                throw new AccessDeniedException("Access denied: You can only access your own technician profile.");
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<TechnicianDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam TechnicianStatus status,
            Authentication authentication
    ) {
        TechnicianDTO existing = technicianService.getTechnicianById(id);

        // If caller is TECHNICIAN, verify they are modifying their own status
        if (authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TECHNICIAN"))) {
            String callerEmail = authentication.getName();
            if (!com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, existing.getEmail())) {
                throw new AccessDeniedException("Access denied: You can only update your own dispatch status.");
            }
        }

        TechnicianDTO dto = technicianService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Status updated", dto));
    }
}

package com.fieldops.routing.multistop;

import com.fieldops.common.ApiResponse;
import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/technicians")
public class RouteOptimizationController {

    private final MultiStopRouteService multiStopRouteService;
    private final TechnicianRepository technicianRepository;

    public RouteOptimizationController(MultiStopRouteService multiStopRouteService,
                                       TechnicianRepository technicianRepository) {
        this.multiStopRouteService = multiStopRouteService;
        this.technicianRepository = technicianRepository;
    }

    @GetMapping("/{id}/optimized-route")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<OptimizedRoute>> getOptimizedRoute(
            @PathVariable Long id,
            Authentication authentication
    ) {
        verifyTechnicianRouteAccess(id, authentication);
        OptimizedRoute route = multiStopRouteService.getOptimizedRoute(id);
        return ResponseEntity.ok(ApiResponse.ok(route));
    }

    @PostMapping("/{id}/optimize-route")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<OptimizedRoute>> optimizeRoute(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "dispatcher@fieldops.demo";
        OptimizedRoute route = multiStopRouteService.optimizeAndAuditRoute(id, email);
        return ResponseEntity.ok(ApiResponse.ok("Deterministic multi-stop route generated and audited", route));
    }

    @PostMapping("/{id}/apply-route")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<OptimizedRoute>> applyRoute(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "dispatcher@fieldops.demo";
        OptimizedRoute route = multiStopRouteService.applyOptimizedRoute(id, email);
        return ResponseEntity.ok(ApiResponse.ok("Optimized route sequence applied to technician schedule", route));
    }

    @GetMapping("/{id}/route-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRouteSummary(
            @PathVariable Long id,
            Authentication authentication
    ) {
        verifyTechnicianRouteAccess(id, authentication);
        Map<String, Object> summary = multiStopRouteService.getRouteSummary(id);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/{id}/route-audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<RouteDecisionAudit>>> getRouteAudit(@PathVariable Long id) {
        List<RouteDecisionAudit> history = multiStopRouteService.getRouteAuditHistory(id);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    private void verifyTechnicianRouteAccess(Long technicianId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Access denied: Authentication required.");
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"))) {
            Technician tech = technicianRepository.findById(technicianId)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));
            String callerEmail = authentication.getName();
            if (!com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, tech.getEmail())) {
                throw new AccessDeniedException("Access denied: You can only access your own assigned route.");
            }
        }
    }
}

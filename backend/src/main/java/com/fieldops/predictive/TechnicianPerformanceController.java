package com.fieldops.predictive;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianPerformanceController {

    private final TechnicianPerformanceService technicianPerformanceService;

    public TechnicianPerformanceController(TechnicianPerformanceService technicianPerformanceService) {
        this.technicianPerformanceService = technicianPerformanceService;
    }

    @GetMapping("/{id}/performance")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'TECHNICIAN')")
    public TechnicianPerformanceDTO getPerformance(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication
    ) {
        TechnicianPerformanceDTO dto = technicianPerformanceService.evaluateTechnician(id);
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"))) {
            String callerEmail = authentication.getName();
            if (!callerEmail.equalsIgnoreCase(dto.getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only view your own performance scorecard.");
            }
        }
        return dto;
    }

    @GetMapping("/performance/leaderboard")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public List<TechnicianPerformanceDTO> getLeaderboard() {
        return technicianPerformanceService.getLeaderboard();
    }
}

package com.fieldops.alert;

import com.fieldops.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final com.fieldops.predictive.PredictiveSlaService predictiveSlaService;
    private final com.fieldops.predictive.PredictiveInventoryService predictiveInventoryService;
    private final com.fieldops.predictive.TechnicianPerformanceService technicianPerformanceService;

    public AlertController(AlertService alertService,
                           @org.springframework.beans.factory.annotation.Autowired(required = false)
                           com.fieldops.predictive.PredictiveSlaService predictiveSlaService,
                           @org.springframework.beans.factory.annotation.Autowired(required = false)
                           com.fieldops.predictive.PredictiveInventoryService predictiveInventoryService,
                           @org.springframework.beans.factory.annotation.Autowired(required = false)
                           com.fieldops.predictive.TechnicianPerformanceService technicianPerformanceService) {
        this.alertService = alertService;
        this.predictiveSlaService = predictiveSlaService;
        this.predictiveInventoryService = predictiveInventoryService;
        this.technicianPerformanceService = technicianPerformanceService;
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getAlerts(
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) AlertType type,
            @RequestParam(required = false) AlertStatus status
    ) {
        List<AlertDTO> list = alertService.getAlerts(severity, type, status);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/active")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getActiveAlerts() {
        List<AlertDTO> active = alertService.getActiveAlerts();
        return ResponseEntity.ok(ApiResponse.ok(active));
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<AlertDTO>> getAlertById(@PathVariable Long id) {
        AlertDTO dto = alertService.getAlertById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/{id}/acknowledge")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<AlertDTO>> acknowledgeAlert(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "dispatcher@fieldops.demo";
        AlertDTO updated = alertService.acknowledgeAlert(id, email);
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged", updated));
    }

    @PostMapping("/{id}/resolve")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<AlertDTO>> resolveAlert(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : "dispatcher@fieldops.demo";
        String notes = body != null ? body.get("notes") : null;
        AlertDTO updated = alertService.resolveAlert(id, email, notes);
        return ResponseEntity.ok(ApiResponse.ok("Alert resolved", updated));
    }

    @PostMapping("/scan")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scanAlerts() {
        int generated = alertService.scanAndGenerateDeterministicAlerts();
        if (predictiveSlaService != null) {
            generated += predictiveSlaService.scanAndGeneratePredictiveAlerts();
        }
        if (predictiveInventoryService != null) {
            generated += predictiveInventoryService.scanAndGenerateInventoryAlerts();
        }
        if (technicianPerformanceService != null) {
            generated += technicianPerformanceService.scanAndGeneratePerformanceAlerts();
        }
        List<AlertDTO> active = alertService.getActiveAlerts();
        return ResponseEntity.ok(ApiResponse.ok("Scan complete", Map.of(
                "newAlertsCount", generated,
                "totalActiveAlerts", active.size()
        )));
    }
}

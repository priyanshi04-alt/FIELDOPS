package com.fieldops.intelligence;

import com.fieldops.anomaly.OperationalAnomaly;
import com.fieldops.anomaly.OperationalAnomalyService;
import com.fieldops.assignment.AssignmentRecommendation;
import com.fieldops.assignment.TechnicianAssignmentService;
import com.fieldops.audit.DecisionAuditLog;
import com.fieldops.audit.DecisionAuditService;
import com.fieldops.common.ApiResponse;
import com.fieldops.routing.RouteEstimate;
import com.fieldops.routing.RouteIntelligenceService;
import com.fieldops.sla.SlaRiskResult;
import com.fieldops.sla.SlaRiskService;
import com.fieldops.workorder.WorkOrderDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OperationsIntelligenceController {

    private final TechnicianAssignmentService assignmentService;
    private final SlaRiskService slaRiskService;
    private final RouteIntelligenceService routeIntelligenceService;
    private final OperationalAnomalyService anomalyService;
    private final OperationsIntelligenceService operationsIntelligenceService;
    private final DecisionAuditService auditService;

    public OperationsIntelligenceController(TechnicianAssignmentService assignmentService,
                                            SlaRiskService slaRiskService,
                                            RouteIntelligenceService routeIntelligenceService,
                                            OperationalAnomalyService anomalyService,
                                            OperationsIntelligenceService operationsIntelligenceService,
                                            DecisionAuditService auditService) {
        this.assignmentService = assignmentService;
        this.slaRiskService = slaRiskService;
        this.routeIntelligenceService = routeIntelligenceService;
        this.anomalyService = anomalyService;
        this.operationsIntelligenceService = operationsIntelligenceService;
        this.auditService = auditService;
    }

    @GetMapping("/work-orders/{id}/assignment-recommendation")
    public ResponseEntity<ApiResponse<AssignmentRecommendation>> getAssignmentRecommendation(@PathVariable Long id) {
        AssignmentRecommendation rec = assignmentService.getRecommendation(id);
        return ResponseEntity.ok(ApiResponse.ok(rec));
    }

    @GetMapping("/work-orders/{id}/sla-risk")
    public ResponseEntity<ApiResponse<SlaRiskResult>> getSlaRisk(@PathVariable Long id) {
        SlaRiskResult res = slaRiskService.evaluateSlaRisk(id);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/work-orders/{id}/route-estimate")
    public ResponseEntity<ApiResponse<RouteEstimate>> getRouteEstimate(
            @PathVariable Long id,
            @RequestParam(required = false) Long technicianId
    ) {
        RouteEstimate est = routeIntelligenceService.getRouteEstimate(id, technicianId);
        return ResponseEntity.ok(ApiResponse.ok(est));
    }

    @PostMapping("/work-orders/{id}/accept-recommendation")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> acceptRecommendation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            Authentication authentication
    ) {
        Long techId = null;
        if (body != null && body.containsKey("technicianId") && body.get("technicianId") != null) {
            techId = Long.valueOf(body.get("technicianId").toString());
        }
        String dispatcherEmail = authentication != null ? authentication.getName() : "dispatcher@fieldops.demo";
        WorkOrderDTO dto = assignmentService.acceptRecommendation(id, techId, dispatcherEmail);
        return ResponseEntity.ok(ApiResponse.ok("Technician successfully assigned and dispatched.", dto));
    }

    @PostMapping("/work-orders/{id}/reject-recommendation")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> rejectRecommendation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            Authentication authentication
    ) {
        Long selectedTechId = null;
        String reason = null;
        if (body != null) {
            if (body.containsKey("selectedTechnicianId") && body.get("selectedTechnicianId") != null) {
                selectedTechId = Long.valueOf(body.get("selectedTechnicianId").toString());
            }
            if (body.containsKey("reason") && body.get("reason") != null) {
                reason = body.get("reason").toString();
            }
        }
        String dispatcherEmail = authentication != null ? authentication.getName() : "dispatcher@fieldops.demo";
        WorkOrderDTO dto = assignmentService.rejectRecommendation(id, selectedTechId, reason, dispatcherEmail);
        return ResponseEntity.ok(ApiResponse.ok("Recommendation rejection and audit event logged.", dto));
    }

    @GetMapping("/work-orders/{id}/audit-trail")
    public ResponseEntity<ApiResponse<List<DecisionAuditLog>>> getWorkOrderAuditTrail(@PathVariable Long id) {
        List<DecisionAuditLog> logs = auditService.getAuditLogsForWorkOrder(id);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @GetMapping("/intelligence/anomalies")
    public ResponseEntity<ApiResponse<List<OperationalAnomaly>>> getAnomalies() {
        List<OperationalAnomaly> list = anomalyService.detectAnomalies();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/intelligence/overview")
    public ResponseEntity<ApiResponse<OperationsIntelligenceOverview>> getIntelligenceOverview() {
        OperationsIntelligenceOverview overview = operationsIntelligenceService.getOverview();
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }
}

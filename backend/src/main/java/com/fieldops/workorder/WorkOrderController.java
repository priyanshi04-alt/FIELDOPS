package com.fieldops.workorder;

import com.fieldops.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<WorkOrderDTO>>> getWorkOrders(
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) WorkOrderPriority priority,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String search
    ) {
        List<WorkOrderDTO> list = workOrderService.getAllWorkOrders(status, priority, technicianId, customerId, search);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> getWorkOrder(@PathVariable Long id) {
        WorkOrderDTO dto = workOrderService.getWorkOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/by-number/{number}")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> getWorkOrderByNumber(@PathVariable String number) {
        WorkOrderDTO dto = workOrderService.getWorkOrderByNumber(number);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/sla-at-risk")
    public ResponseEntity<ApiResponse<List<WorkOrderDTO>>> getHighRiskWorkOrders() {
        List<WorkOrderDTO> list = workOrderService.getHighRiskWorkOrders();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkOrderDTO>> createWorkOrder(@Valid @RequestBody WorkOrderCreateRequest request) {
        WorkOrderDTO dto = workOrderService.createWorkOrder(request);
        return ResponseEntity.ok(ApiResponse.ok("Work order created successfully", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> updateWorkOrder(
            @PathVariable Long id,
            @RequestBody WorkOrderUpdateRequest request
    ) {
        WorkOrderDTO dto = workOrderService.updateWorkOrder(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Work order updated successfully", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrder(@PathVariable Long id) {
        workOrderService.deleteWorkOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Work order deleted successfully", null));
    }
}

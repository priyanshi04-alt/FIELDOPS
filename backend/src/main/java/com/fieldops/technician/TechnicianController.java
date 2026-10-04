package com.fieldops.technician;

import com.fieldops.common.ApiResponse;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<TechnicianDTO>>> getTechnicians(
            @RequestParam(required = false) TechnicianStatus status,
            @RequestParam(required = false) String city
    ) {
        List<TechnicianDTO> technicians = technicianService.getAllTechnicians(status, city);
        return ResponseEntity.ok(ApiResponse.ok(technicians));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TechnicianDTO>> getTechnician(@PathVariable Long id) {
        TechnicianDTO dto = technicianService.getTechnicianById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TechnicianDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam TechnicianStatus status
    ) {
        TechnicianDTO dto = technicianService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Status updated", dto));
    }
}

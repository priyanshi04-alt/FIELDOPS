package com.fieldops.inventory;

import com.fieldops.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryDTO>>> getInventory(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) InventoryItemStatus status
    ) {
        List<InventoryDTO> items = inventoryService.getAllItems(category, status);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDTO>> getItem(@PathVariable Long id) {
        InventoryDTO item = inventoryService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustRequest request
    ) {
        InventoryDTO updated = inventoryService.adjustStock(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Stock updated successfully", updated));
    }
}

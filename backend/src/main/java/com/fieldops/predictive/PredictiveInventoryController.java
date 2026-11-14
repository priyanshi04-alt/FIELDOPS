package com.fieldops.predictive;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class PredictiveInventoryController {

    private final PredictiveInventoryService predictiveInventoryService;

    public PredictiveInventoryController(PredictiveInventoryService predictiveInventoryService) {
        this.predictiveInventoryService = predictiveInventoryService;
    }

    @GetMapping("/predictions")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public List<InventoryForecastDTO> getAllPredictions() {
        return predictiveInventoryService.getAllForecasts();
    }

    @GetMapping("/{id}/forecast")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public InventoryForecastDTO getForecast(@PathVariable Long id) {
        return predictiveInventoryService.getForecast(id);
    }

    @GetMapping("/replenishment-recommendations")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public List<InventoryForecastDTO> getRecommendations() {
        return predictiveInventoryService.getReplenishmentRecommendations();
    }
}

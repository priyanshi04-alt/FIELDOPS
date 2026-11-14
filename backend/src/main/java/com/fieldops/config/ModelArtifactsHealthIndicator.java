package com.fieldops.config;

import com.fieldops.predictive.ModelRegistryService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ModelArtifactsHealthIndicator implements HealthIndicator {

    private final ModelRegistryService modelRegistryService;

    public ModelArtifactsHealthIndicator(ModelRegistryService modelRegistryService) {
        this.modelRegistryService = modelRegistryService;
    }

    @Override
    public Health health() {
        boolean inventoryLoaded = modelRegistryService.getInventoryModelConfig() != null;
        boolean slaLoaded = modelRegistryService.getSlaModelConfig() != null;

        if (inventoryLoaded && slaLoaded) {
            return Health.up()
                    .withDetail("inventoryForecastModel", "LOADED")
                    .withDetail("slaBreachModel", "LOADED")
                    .build();
        } else {
            return Health.down()
                    .withDetail("inventoryForecastModel", inventoryLoaded ? "LOADED" : "MISSING")
                    .withDetail("slaBreachModel", slaLoaded ? "LOADED" : "MISSING")
                    .build();
        }
    }
}

package com.fieldops.predictive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ModelRegistryService {

    private static final Logger log = LoggerFactory.getLogger(ModelRegistryService.class);

    private final ResourceLoader resourceLoader;
    private final ModelRegistryRepository modelRegistryRepository;
    private final ObjectMapper objectMapper;

    private JsonNode inventoryModelConfig;
    private JsonNode slaModelConfig;

    public ModelRegistryService(ResourceLoader resourceLoader,
                                ModelRegistryRepository modelRegistryRepository,
                                ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.modelRegistryRepository = modelRegistryRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        loadInventoryModel();
        loadSlaModel();
    }

    private void loadInventoryModel() {
        try {
            Resource res = resourceLoader.getResource("classpath:models/inventory_forecast_model.json");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    inventoryModelConfig = objectMapper.readTree(is);
                    syncRegistryEntry("INVENTORY_FORECAST", inventoryModelConfig,
                            inventoryModelConfig.path("metrics").path("modelMae").asDouble(3.01), "MAE (units)",
                            inventoryModelConfig.path("metrics").path("modelR2").asDouble(0.687), "R² Score");
                    log.info("Loaded Inventory Forecast Model v{} ({})",
                            inventoryModelConfig.path("modelVersion").asText(),
                            inventoryModelConfig.path("algorithm").asText());
                }
            }
        } catch (Exception e) {
            log.warn("Could not load inventory model config: {}", e.getMessage());
        }
    }

    private void loadSlaModel() {
        try {
            Resource res = resourceLoader.getResource("classpath:models/sla_breach_model.json");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    slaModelConfig = objectMapper.readTree(is);
                    syncRegistryEntry("SLA_BREACH_CLASSIFIER", slaModelConfig,
                            slaModelConfig.path("metrics").path("modelAuc").asDouble(0.981), "ROC-AUC",
                            slaModelConfig.path("metrics").path("modelF1").asDouble(0.942), "F1-Score");
                    log.info("Loaded SLA Breach Classifier v{} ({})",
                            slaModelConfig.path("modelVersion").asText(),
                            slaModelConfig.path("algorithm").asText());
                }
            }
        } catch (Exception e) {
            log.warn("Could not load SLA model config: {}", e.getMessage());
        }
    }

    private void syncRegistryEntry(String key, JsonNode config,
                                   double primaryVal, String primaryName,
                                   double secondaryVal, String secondaryName) {
        ModelRegistryEntry entry = modelRegistryRepository.findByModelKey(key)
                .orElseGet(ModelRegistryEntry::new);

        entry.setModelKey(key);
        entry.setModelName(config.path("modelName").asText(key));
        entry.setModelVersion(config.path("modelVersion").asText("1.0.0"));
        entry.setAlgorithm(config.path("algorithm").asText("Statistical Regression / Classification"));
        entry.setEvaluationDataset(config.path("evaluationDataset").asText("Synthetic Simulation Telemetry"));
        entry.setSyntheticDisclaimer(config.path("syntheticDisclaimer").asText("DEMO / SIMULATION DATA: Synthetic demonstration dataset."));
        entry.setPrimaryMetricValue(primaryVal);
        entry.setPrimaryMetricName(primaryName);
        entry.setSecondaryMetricValue(secondaryVal);
        entry.setSecondaryMetricName(secondaryName);
        entry.setLoadedAt(LocalDateTime.now());

        modelRegistryRepository.save(entry);
    }

    public JsonNode getInventoryModelConfig() {
        return inventoryModelConfig;
    }

    public JsonNode getSlaModelConfig() {
        return slaModelConfig;
    }

    public Map<String, ModelRegistryEntry> getAllRegisteredModels() {
        Map<String, ModelRegistryEntry> map = new HashMap<>();
        for (ModelRegistryEntry e : modelRegistryRepository.findAll()) {
            map.put(e.getModelKey(), e);
        }
        return map;
    }
}

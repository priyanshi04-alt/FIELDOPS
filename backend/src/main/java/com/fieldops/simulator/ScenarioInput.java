package com.fieldops.simulator;

import java.util.HashMap;
import java.util.Map;

public class ScenarioInput {

    private ScenarioType scenarioType;
    private String scenarioName;
    private Long targetTechnicianId;
    private Long targetWorkOrderId;
    private Long targetInventoryItemId;
    private Integer delayMinutes;
    private Long fallbackTechnicianId;
    private Map<String, Object> parameters = new HashMap<>();

    public ScenarioInput() {}

    public ScenarioType getScenarioType() { return scenarioType; }
    public void setScenarioType(ScenarioType scenarioType) { this.scenarioType = scenarioType; }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }

    public Long getTargetTechnicianId() { return targetTechnicianId; }
    public void setTargetTechnicianId(Long targetTechnicianId) { this.targetTechnicianId = targetTechnicianId; }

    public Long getTargetWorkOrderId() { return targetWorkOrderId; }
    public void setTargetWorkOrderId(Long targetWorkOrderId) { this.targetWorkOrderId = targetWorkOrderId; }

    public Long getTargetInventoryItemId() { return targetInventoryItemId; }
    public void setTargetInventoryItemId(Long targetInventoryItemId) { this.targetInventoryItemId = targetInventoryItemId; }

    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }

    public Long getFallbackTechnicianId() { return fallbackTechnicianId; }
    public void setFallbackTechnicianId(Long fallbackTechnicianId) { this.fallbackTechnicianId = fallbackTechnicianId; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}

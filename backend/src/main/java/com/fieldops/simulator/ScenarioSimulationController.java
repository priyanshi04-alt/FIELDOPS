package com.fieldops.simulator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulator")
public class ScenarioSimulationController {

    private final ScenarioSimulationService simulationService;

    public ScenarioSimulationController(ScenarioSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/simulate")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ScenarioResult runSimulation(@RequestBody ScenarioInput input) {
        return simulationService.simulate(input);
    }

    @PostMapping("/apply/{simulationId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ScenarioResult applySimulation(@PathVariable String simulationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth != null ? auth.getName() : "dispatcher@fieldops.demo";
        return simulationService.applyScenario(simulationId, userEmail);
    }

    @GetMapping("/history")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public List<ScenarioDecisionAudit> getAuditHistory() {
        return simulationService.getAuditHistory();
    }
}

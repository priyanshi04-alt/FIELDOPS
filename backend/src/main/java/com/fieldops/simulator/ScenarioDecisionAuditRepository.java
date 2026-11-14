package com.fieldops.simulator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioDecisionAuditRepository extends JpaRepository<ScenarioDecisionAudit, Long> {
    List<ScenarioDecisionAudit> findTop20ByOrderByAppliedAtDesc();
}

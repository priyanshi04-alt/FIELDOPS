package com.fieldops.routing.multistop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteDecisionAuditRepository extends JpaRepository<RouteDecisionAudit, Long> {
    List<RouteDecisionAudit> findByTechnicianIdOrderByTimestampDesc(Long technicianId);
    List<RouteDecisionAudit> findTop20ByOrderByTimestampDesc();
}

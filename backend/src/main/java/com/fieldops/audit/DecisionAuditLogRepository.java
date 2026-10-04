package com.fieldops.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecisionAuditLogRepository extends JpaRepository<DecisionAuditLog, Long> {
    List<DecisionAuditLog> findByWorkOrderIdOrderByTimestampDesc(Long workOrderId);
    List<DecisionAuditLog> findTop20ByOrderByTimestampDesc();
}

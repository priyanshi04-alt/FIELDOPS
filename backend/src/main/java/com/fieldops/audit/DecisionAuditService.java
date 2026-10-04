package com.fieldops.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DecisionAuditService {

    private final DecisionAuditLogRepository auditRepository;

    public DecisionAuditService(DecisionAuditLogRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public DecisionAuditLog recordEvent(Long workOrderId, String workOrderNumber, String decisionType,
                                        Long recommendedTechId, String recommendedTechName,
                                        Long selectedTechId, String selectedTechName,
                                        Integer score, String explanation, String dispatcherEmail) {
        DecisionAuditLog log = DecisionAuditLog.builder()
                .workOrderId(workOrderId)
                .workOrderNumber(workOrderNumber)
                .decisionType(decisionType)
                .recommendedTechnicianId(recommendedTechId)
                .recommendedTechnicianName(recommendedTechName)
                .selectedTechnicianId(selectedTechId)
                .selectedTechnicianName(selectedTechName)
                .score(score)
                .explanation(explanation)
                .dispatcherEmail(dispatcherEmail != null ? dispatcherEmail : "dispatcher@fieldops.demo")
                .build();

        return auditRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<DecisionAuditLog> getAuditLogsForWorkOrder(Long workOrderId) {
        return auditRepository.findByWorkOrderIdOrderByTimestampDesc(workOrderId);
    }

    @Transactional(readOnly = true)
    public List<DecisionAuditLog> getRecentAuditLogs() {
        return auditRepository.findTop20ByOrderByTimestampDesc();
    }
}

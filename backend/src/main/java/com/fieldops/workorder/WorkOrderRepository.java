package com.fieldops.workorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Optional<WorkOrder> findByWorkOrderNumber(String workOrderNumber);
    List<WorkOrder> findByStatus(WorkOrderStatus status);
    List<WorkOrder> findByPriority(WorkOrderPriority priority);
    List<WorkOrder> findByTechnicianId(Long technicianId);
    List<WorkOrder> findByCustomerId(Long customerId);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.slaBreachProbability >= 60.0 AND wo.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY wo.slaBreachProbability DESC")
    List<WorkOrder> findHighRiskWorkOrders();

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countActiveJobs();

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.slaBreachProbability >= 60.0 AND wo.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countSlaAtRisk();

    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status = 'COMPLETED'")
    long countCompletedJobs();
}

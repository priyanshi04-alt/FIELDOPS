package com.fieldops.workorder;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WorkOrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private WorkOrderStatus status;

    private String serviceNotes;

    private LocalDateTime completedAt;

    public WorkOrderStatusUpdateRequest() {}

    public WorkOrderStatusUpdateRequest(WorkOrderStatus status, String serviceNotes, LocalDateTime completedAt) {
        this.status = status;
        this.serviceNotes = serviceNotes;
        this.completedAt = completedAt;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public String getServiceNotes() {
        return serviceNotes;
    }

    public void setServiceNotes(String serviceNotes) {
        this.serviceNotes = serviceNotes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

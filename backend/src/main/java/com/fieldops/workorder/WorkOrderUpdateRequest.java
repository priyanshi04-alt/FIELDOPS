package com.fieldops.workorder;

import java.time.LocalDateTime;

public class WorkOrderUpdateRequest {
    private String title;
    private String description;
    private WorkOrderPriority priority;
    private WorkOrderStatus status;
    private Long technicianId;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private String serviceNotes;
    private Integer technicianEtaMinutes;
    private Double slaBreachProbability;

    public WorkOrderUpdateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public WorkOrderPriority getPriority() { return priority; }
    public void setPriority(WorkOrderPriority priority) { this.priority = priority; }
    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
    public LocalDateTime getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }
    public String getServiceNotes() { return serviceNotes; }
    public void setServiceNotes(String serviceNotes) { this.serviceNotes = serviceNotes; }
    public Integer getTechnicianEtaMinutes() { return technicianEtaMinutes; }
    public void setTechnicianEtaMinutes(Integer technicianEtaMinutes) { this.technicianEtaMinutes = technicianEtaMinutes; }
    public Double getSlaBreachProbability() { return slaBreachProbability; }
    public void setSlaBreachProbability(Double slaBreachProbability) { this.slaBreachProbability = slaBreachProbability; }
}

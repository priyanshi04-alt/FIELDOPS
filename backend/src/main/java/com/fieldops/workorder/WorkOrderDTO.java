package com.fieldops.workorder;

import com.fieldops.customer.CustomerDTO;
import com.fieldops.technician.TechnicianDTO;

import java.time.LocalDateTime;
import java.util.Set;

public class WorkOrderDTO {
    private Long id;
    private String workOrderNumber;
    private String title;
    private String description;
    private WorkOrderPriority priority;
    private WorkOrderStatus status;
    private CustomerDTO customer;
    private TechnicianDTO technician;
    private String locationName;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private Set<String> requiredSkills;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime slaDeadline;
    private LocalDateTime completedAt;
    private Double estimatedDurationHours;
    private Double slaBreachProbability;
    private Integer technicianEtaMinutes;
    private Double distanceKm;
    private String serviceNotes;
    private String aiInsight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkOrderDTO() {}

    public WorkOrderDTO(Long id, String workOrderNumber, String title, String description, WorkOrderPriority priority,
                        WorkOrderStatus status, CustomerDTO customer, TechnicianDTO technician, String locationName,
                        String city, String address, Double latitude, Double longitude, Set<String> requiredSkills,
                        LocalDateTime scheduledStartTime, LocalDateTime scheduledEndTime, LocalDateTime slaDeadline,
                        LocalDateTime completedAt, Double estimatedDurationHours, Double slaBreachProbability,
                        Integer technicianEtaMinutes, Double distanceKm, String serviceNotes, String aiInsight,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.workOrderNumber = workOrderNumber;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.customer = customer;
        this.technician = technician;
        this.locationName = locationName;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.requiredSkills = requiredSkills;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.slaDeadline = slaDeadline;
        this.completedAt = completedAt;
        this.estimatedDurationHours = estimatedDurationHours;
        this.slaBreachProbability = slaBreachProbability;
        this.technicianEtaMinutes = technicianEtaMinutes;
        this.distanceKm = distanceKm;
        this.serviceNotes = serviceNotes;
        this.aiInsight = aiInsight;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WorkOrderDTO fromEntity(WorkOrder wo) {
        if (wo == null) return null;
        return new WorkOrderDTO(
                wo.getId(),
                wo.getWorkOrderNumber(),
                wo.getTitle(),
                wo.getDescription(),
                wo.getPriority(),
                wo.getStatus(),
                CustomerDTO.fromEntity(wo.getCustomer()),
                TechnicianDTO.fromEntity(wo.getTechnician()),
                wo.getLocationName(),
                wo.getCity(),
                wo.getAddress(),
                wo.getLatitude(),
                wo.getLongitude(),
                wo.getRequiredSkills(),
                wo.getScheduledStartTime(),
                wo.getScheduledEndTime(),
                wo.getSlaDeadline(),
                wo.getCompletedAt(),
                wo.getEstimatedDurationHours(),
                wo.getSlaBreachProbability(),
                wo.getTechnicianEtaMinutes(),
                wo.getDistanceKm(),
                wo.getServiceNotes(),
                wo.getAiInsight(),
                wo.getCreatedAt(),
                wo.getUpdatedAt()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public WorkOrderPriority getPriority() { return priority; }
    public void setPriority(WorkOrderPriority priority) { this.priority = priority; }
    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }
    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }
    public TechnicianDTO getTechnician() { return technician; }
    public void setTechnician(TechnicianDTO technician) { this.technician = technician; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Set<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Set<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
    public LocalDateTime getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }
    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public Double getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(Double estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }
    public Double getSlaBreachProbability() { return slaBreachProbability; }
    public void setSlaBreachProbability(Double slaBreachProbability) { this.slaBreachProbability = slaBreachProbability; }
    public Integer getTechnicianEtaMinutes() { return technicianEtaMinutes; }
    public void setTechnicianEtaMinutes(Integer technicianEtaMinutes) { this.technicianEtaMinutes = technicianEtaMinutes; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public String getServiceNotes() { return serviceNotes; }
    public void setServiceNotes(String serviceNotes) { this.serviceNotes = serviceNotes; }
    public String getAiInsight() { return aiInsight; }
    public void setAiInsight(String aiInsight) { this.aiInsight = aiInsight; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

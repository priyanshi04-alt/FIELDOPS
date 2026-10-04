package com.fieldops.workorder;

import com.fieldops.customer.Customer;
import com.fieldops.technician.Technician;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String workOrderNumber;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status = WorkOrderStatus.NEW;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "technician_id")
    private Technician technician;

    private String locationName;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_order_skills", joinColumns = @JoinColumn(name = "work_order_id"))
    @Column(name = "skill")
    private Set<String> requiredSkills = new HashSet<>();

    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime slaDeadline;
    private LocalDateTime completedAt;

    private Double estimatedDurationHours = 2.0;
    private Double slaBreachProbability = 15.0;
    private Integer technicianEtaMinutes = 30;
    private Double distanceKm = 5.0;

    @Column(length = 2000)
    private String serviceNotes;

    @Column(length = 1000)
    private String aiInsight;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public WorkOrder() {}

    public WorkOrder(Long id, String workOrderNumber, String title, String description, WorkOrderPriority priority,
                     WorkOrderStatus status, Customer customer, Technician technician, String locationName,
                     String city, String address, Double latitude, Double longitude, Set<String> requiredSkills,
                     LocalDateTime scheduledStartTime, LocalDateTime scheduledEndTime, LocalDateTime slaDeadline,
                     LocalDateTime completedAt, Double estimatedDurationHours, Double slaBreachProbability,
                     Integer technicianEtaMinutes, Double distanceKm, String serviceNotes, String aiInsight) {
        this.id = id;
        this.workOrderNumber = workOrderNumber;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status != null ? status : WorkOrderStatus.NEW;
        this.customer = customer;
        this.technician = technician;
        this.locationName = locationName;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new HashSet<>();
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.slaDeadline = slaDeadline;
        this.completedAt = completedAt;
        this.estimatedDurationHours = estimatedDurationHours != null ? estimatedDurationHours : 2.0;
        this.slaBreachProbability = slaBreachProbability != null ? slaBreachProbability : 15.0;
        this.technicianEtaMinutes = technicianEtaMinutes;
        this.distanceKm = distanceKm;
        this.serviceNotes = serviceNotes;
        this.aiInsight = aiInsight;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String workOrderNumber;
        private String title;
        private String description;
        private WorkOrderPriority priority;
        private WorkOrderStatus status = WorkOrderStatus.NEW;
        private Customer customer;
        private Technician technician;
        private String locationName;
        private String city;
        private String address;
        private Double latitude;
        private Double longitude;
        private Set<String> requiredSkills = new HashSet<>();
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private LocalDateTime slaDeadline;
        private LocalDateTime completedAt;
        private Double estimatedDurationHours = 2.0;
        private Double slaBreachProbability = 15.0;
        private Integer technicianEtaMinutes = 30;
        private Double distanceKm = 5.0;
        private String serviceNotes;
        private String aiInsight;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder workOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder priority(WorkOrderPriority priority) { this.priority = priority; return this; }
        public Builder status(WorkOrderStatus status) { this.status = status; return this; }
        public Builder customer(Customer customer) { this.customer = customer; return this; }
        public Builder technician(Technician technician) { this.technician = technician; return this; }
        public Builder locationName(String locationName) { this.locationName = locationName; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder latitude(Double latitude) { this.latitude = latitude; return this; }
        public Builder longitude(Double longitude) { this.longitude = longitude; return this; }
        public Builder requiredSkills(Set<String> requiredSkills) { this.requiredSkills = requiredSkills; return this; }
        public Builder scheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; return this; }
        public Builder scheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; return this; }
        public Builder slaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder estimatedDurationHours(Double estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; return this; }
        public Builder slaBreachProbability(Double slaBreachProbability) { this.slaBreachProbability = slaBreachProbability; return this; }
        public Builder technicianEtaMinutes(Integer technicianEtaMinutes) { this.technicianEtaMinutes = technicianEtaMinutes; return this; }
        public Builder distanceKm(Double distanceKm) { this.distanceKm = distanceKm; return this; }
        public Builder serviceNotes(String serviceNotes) { this.serviceNotes = serviceNotes; return this; }
        public Builder aiInsight(String aiInsight) { this.aiInsight = aiInsight; return this; }

        public WorkOrder build() {
            return new WorkOrder(id, workOrderNumber, title, description, priority, status, customer, technician,
                    locationName, city, address, latitude, longitude, requiredSkills, scheduledStartTime,
                    scheduledEndTime, slaDeadline, completedAt, estimatedDurationHours, slaBreachProbability,
                    technicianEtaMinutes, distanceKm, serviceNotes, aiInsight);
        }
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
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Technician getTechnician() { return technician; }
    public void setTechnician(Technician technician) { this.technician = technician; }
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

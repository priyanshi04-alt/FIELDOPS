package com.fieldops.technician;

import java.time.LocalDateTime;
import java.util.Set;

public class TechnicianDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private TechnicianStatus status;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer completedJobs;
    private Double slaSuccessRate;
    private Integer activeJobsCount;
    private Double utilizationRate;
    private Set<String> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TechnicianDTO() {}

    public TechnicianDTO(Long id, String name, String email, String phone, String avatarUrl, TechnicianStatus status,
                         String city, String address, Double latitude, Double longitude, Double rating,
                         Integer completedJobs, Double slaSuccessRate, Integer activeJobsCount, Double utilizationRate,
                         Set<String> skills, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.completedJobs = completedJobs;
        this.slaSuccessRate = slaSuccessRate;
        this.activeJobsCount = activeJobsCount;
        this.utilizationRate = utilizationRate;
        this.skills = skills;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TechnicianDTO fromEntity(Technician t) {
        if (t == null) return null;
        return new TechnicianDTO(
                t.getId(),
                t.getName(),
                t.getEmail(),
                t.getPhone(),
                t.getAvatarUrl(),
                t.getStatus(),
                t.getCity(),
                t.getAddress(),
                t.getLatitude(),
                t.getLongitude(),
                t.getRating(),
                t.getCompletedJobs(),
                t.getSlaSuccessRate(),
                t.getActiveJobsCount(),
                t.getUtilizationRate(),
                t.getSkills(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public TechnicianStatus getStatus() { return status; }
    public void setStatus(TechnicianStatus status) { this.status = status; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getCompletedJobs() { return completedJobs; }
    public void setCompletedJobs(Integer completedJobs) { this.completedJobs = completedJobs; }
    public Double getSlaSuccessRate() { return slaSuccessRate; }
    public void setSlaSuccessRate(Double slaSuccessRate) { this.slaSuccessRate = slaSuccessRate; }
    public Integer getActiveJobsCount() { return activeJobsCount; }
    public void setActiveJobsCount(Integer activeJobsCount) { this.activeJobsCount = activeJobsCount; }
    public Double getUtilizationRate() { return utilizationRate; }
    public void setUtilizationRate(Double utilizationRate) { this.utilizationRate = utilizationRate; }
    public Set<String> getSkills() { return skills; }
    public void setSkills(Set<String> skills) { this.skills = skills; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

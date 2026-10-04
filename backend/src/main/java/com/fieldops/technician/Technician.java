package com.fieldops.technician;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "technicians")
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechnicianStatus status = TechnicianStatus.AVAILABLE;

    @Column(nullable = false)
    private String city;

    private String address;
    private Double latitude;
    private Double longitude;
    private Double rating = 4.8;
    private Integer completedJobs = 0;
    private Double slaSuccessRate = 95.0;
    private Integer activeJobsCount = 0;
    private Double utilizationRate = 65.0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technician_skills", joinColumns = @JoinColumn(name = "technician_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Technician() {}

    public Technician(Long id, String name, String email, String phone, String avatarUrl, TechnicianStatus status,
                      String city, String address, Double latitude, Double longitude, Double rating,
                      Integer completedJobs, Double slaSuccessRate, Integer activeJobsCount, Double utilizationRate,
                      Set<String> skills) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.status = status != null ? status : TechnicianStatus.AVAILABLE;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating != null ? rating : 4.8;
        this.completedJobs = completedJobs != null ? completedJobs : 0;
        this.slaSuccessRate = slaSuccessRate != null ? slaSuccessRate : 95.0;
        this.activeJobsCount = activeJobsCount != null ? activeJobsCount : 0;
        this.utilizationRate = utilizationRate != null ? utilizationRate : 65.0;
        this.skills = skills != null ? skills : new HashSet<>();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String avatarUrl;
        private TechnicianStatus status = TechnicianStatus.AVAILABLE;
        private String city;
        private String address;
        private Double latitude;
        private Double longitude;
        private Double rating = 4.8;
        private Integer completedJobs = 0;
        private Double slaSuccessRate = 95.0;
        private Integer activeJobsCount = 0;
        private Double utilizationRate = 65.0;
        private Set<String> skills = new HashSet<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder status(TechnicianStatus status) { this.status = status; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder latitude(Double latitude) { this.latitude = latitude; return this; }
        public Builder longitude(Double longitude) { this.longitude = longitude; return this; }
        public Builder rating(Double rating) { this.rating = rating; return this; }
        public Builder completedJobs(Integer completedJobs) { this.completedJobs = completedJobs; return this; }
        public Builder slaSuccessRate(Double slaSuccessRate) { this.slaSuccessRate = slaSuccessRate; return this; }
        public Builder activeJobsCount(Integer activeJobsCount) { this.activeJobsCount = activeJobsCount; return this; }
        public Builder utilizationRate(Double utilizationRate) { this.utilizationRate = utilizationRate; return this; }
        public Builder skills(Set<String> skills) { this.skills = skills; return this; }

        public Technician build() {
            return new Technician(id, name, email, phone, avatarUrl, status, city, address, latitude, longitude,
                    rating, completedJobs, slaSuccessRate, activeJobsCount, utilizationRate, skills);
        }
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

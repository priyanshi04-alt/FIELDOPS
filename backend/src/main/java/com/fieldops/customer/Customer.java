package com.fieldops.customer;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String city;

    private String address;
    private Double latitude;
    private Double longitude;
    private Integer openJobsCount = 0;
    private Integer completedJobsCount = 0;
    private Double satisfactionScore = 4.9;
    private LocalDateTime lastServiceDate;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Customer() {}

    public Customer(Long id, String name, String company, String email, String phone, String city,
                    String address, Double latitude, Double longitude, Integer openJobsCount,
                    Integer completedJobsCount, Double satisfactionScore, LocalDateTime lastServiceDate,
                    String notes) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openJobsCount = openJobsCount != null ? openJobsCount : 0;
        this.completedJobsCount = completedJobsCount != null ? completedJobsCount : 0;
        this.satisfactionScore = satisfactionScore != null ? satisfactionScore : 4.9;
        this.lastServiceDate = lastServiceDate;
        this.notes = notes;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String company;
        private String email;
        private String phone;
        private String city;
        private String address;
        private Double latitude;
        private Double longitude;
        private Integer openJobsCount = 0;
        private Integer completedJobsCount = 0;
        private Double satisfactionScore = 4.9;
        private LocalDateTime lastServiceDate;
        private String notes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder company(String company) { this.company = company; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder latitude(Double latitude) { this.latitude = latitude; return this; }
        public Builder longitude(Double longitude) { this.longitude = longitude; return this; }
        public Builder openJobsCount(Integer openJobsCount) { this.openJobsCount = openJobsCount; return this; }
        public Builder completedJobsCount(Integer completedJobsCount) { this.completedJobsCount = completedJobsCount; return this; }
        public Builder satisfactionScore(Double satisfactionScore) { this.satisfactionScore = satisfactionScore; return this; }
        public Builder lastServiceDate(LocalDateTime lastServiceDate) { this.lastServiceDate = lastServiceDate; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public Customer build() {
            return new Customer(id, name, company, email, phone, city, address, latitude, longitude,
                    openJobsCount, completedJobsCount, satisfactionScore, lastServiceDate, notes);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getOpenJobsCount() { return openJobsCount; }
    public void setOpenJobsCount(Integer openJobsCount) { this.openJobsCount = openJobsCount; }
    public Integer getCompletedJobsCount() { return completedJobsCount; }
    public void setCompletedJobsCount(Integer completedJobsCount) { this.completedJobsCount = completedJobsCount; }
    public Double getSatisfactionScore() { return satisfactionScore; }
    public void setSatisfactionScore(Double satisfactionScore) { this.satisfactionScore = satisfactionScore; }
    public LocalDateTime getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDateTime lastServiceDate) { this.lastServiceDate = lastServiceDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

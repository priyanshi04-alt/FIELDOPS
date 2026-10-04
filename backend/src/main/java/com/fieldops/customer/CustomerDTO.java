package com.fieldops.customer;

import java.time.LocalDateTime;

public class CustomerDTO {
    private Long id;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer openJobsCount;
    private Integer completedJobsCount;
    private Double satisfactionScore;
    private LocalDateTime lastServiceDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerDTO() {}

    public CustomerDTO(Long id, String name, String company, String email, String phone, String city,
                       String address, Double latitude, Double longitude, Integer openJobsCount,
                       Integer completedJobsCount, Double satisfactionScore, LocalDateTime lastServiceDate,
                       String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openJobsCount = openJobsCount;
        this.completedJobsCount = completedJobsCount;
        this.satisfactionScore = satisfactionScore;
        this.lastServiceDate = lastServiceDate;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CustomerDTO fromEntity(Customer c) {
        if (c == null) return null;
        return new CustomerDTO(
                c.getId(),
                c.getName(),
                c.getCompany(),
                c.getEmail(),
                c.getPhone(),
                c.getCity(),
                c.getAddress(),
                c.getLatitude(),
                c.getLongitude(),
                c.getOpenJobsCount(),
                c.getCompletedJobsCount(),
                c.getSatisfactionScore(),
                c.getLastServiceDate(),
                c.getNotes(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
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

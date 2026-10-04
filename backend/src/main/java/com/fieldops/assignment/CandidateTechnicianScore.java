package com.fieldops.assignment;

import com.fieldops.technician.TechnicianStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CandidateTechnicianScore {

    private Long technicianId;
    private String name;
    private String avatarUrl;
    private TechnicianStatus status;
    private Double rating;
    private Double distanceKm;
    private Integer estimatedEtaMinutes;
    private Integer totalScore; // 0 - 100
    private Integer skillScore; // max 35
    private Integer distanceScore; // max 25
    private Integer availabilityScore; // max 15
    private Integer workloadScore; // max 10
    private Integer slaFeasibilityScore; // max 10
    private Integer performanceScore; // max 5
    private Set<String> matchedSkills;
    private Set<String> missingSkills;
    private List<AssignmentFactor> factors = new ArrayList<>();

    public CandidateTechnicianScore() {}

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public TechnicianStatus getStatus() { return status; }
    public void setStatus(TechnicianStatus status) { this.status = status; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getEstimatedEtaMinutes() { return estimatedEtaMinutes; }
    public void setEstimatedEtaMinutes(Integer estimatedEtaMinutes) { this.estimatedEtaMinutes = estimatedEtaMinutes; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getSkillScore() { return skillScore; }
    public void setSkillScore(Integer skillScore) { this.skillScore = skillScore; }

    public Integer getDistanceScore() { return distanceScore; }
    public void setDistanceScore(Integer distanceScore) { this.distanceScore = distanceScore; }

    public Integer getAvailabilityScore() { return availabilityScore; }
    public void setAvailabilityScore(Integer availabilityScore) { this.availabilityScore = availabilityScore; }

    public Integer getWorkloadScore() { return workloadScore; }
    public void setWorkloadScore(Integer workloadScore) { this.workloadScore = workloadScore; }

    public Integer getSlaFeasibilityScore() { return slaFeasibilityScore; }
    public void setSlaFeasibilityScore(Integer slaFeasibilityScore) { this.slaFeasibilityScore = slaFeasibilityScore; }

    public Integer getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(Integer performanceScore) { this.performanceScore = performanceScore; }

    public Set<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(Set<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public Set<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(Set<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<AssignmentFactor> getFactors() { return factors; }
    public void setFactors(List<AssignmentFactor> factors) { this.factors = factors; }
}

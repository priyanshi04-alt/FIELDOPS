package com.fieldops.routing.multistop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OptimizedRoute {

    private Long technicianId;
    private String technicianName;
    private String technicianStatus;
    private String originAddress;
    private Double originLat;
    private Double originLon;
    private List<RouteStop> stops = new ArrayList<>();
    private List<RouteLeg> legs = new ArrayList<>();

    private Integer totalStops = 0;
    private Double totalDistanceKm = 0.0;
    private Integer totalTravelTimeMinutes = 0;
    private Integer totalServiceTimeMinutes = 0;
    private Integer totalDurationMinutes = 0;
    private LocalDateTime startTime;
    private LocalDateTime projectedCompletionTime;

    private Integer slaCompliantCount = 0;
    private Integer slaAtRiskCount = 0;
    private Integer slaViolationCount = 0;

    private Integer optimizationScore = 100;
    private Double baselineDistanceKm = 0.0;
    private Integer baselineTravelTimeMinutes = 0;
    private Double distanceSavedKm = 0.0;
    private Integer timeSavedMinutes = 0;
    private String explanation;
    private Boolean isApplied = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public OptimizedRoute() {}

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getTechnicianStatus() {
        return technicianStatus;
    }

    public void setTechnicianStatus(String technicianStatus) {
        this.technicianStatus = technicianStatus;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public Double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(Double originLat) {
        this.originLat = originLat;
    }

    public Double getOriginLon() {
        return originLon;
    }

    public void setOriginLon(Double originLon) {
        this.originLon = originLon;
    }

    public List<RouteStop> getStops() {
        return stops;
    }

    public void setStops(List<RouteStop> stops) {
        this.stops = stops;
    }

    public List<RouteLeg> getLegs() {
        return legs;
    }

    public void setLegs(List<RouteLeg> legs) {
        this.legs = legs;
    }

    public Integer getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(Integer totalStops) {
        this.totalStops = totalStops;
    }

    public Double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public Integer getTotalTravelTimeMinutes() {
        return totalTravelTimeMinutes;
    }

    public void setTotalTravelTimeMinutes(Integer totalTravelTimeMinutes) {
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
    }

    public Integer getTotalServiceTimeMinutes() {
        return totalServiceTimeMinutes;
    }

    public void setTotalServiceTimeMinutes(Integer totalServiceTimeMinutes) {
        this.totalServiceTimeMinutes = totalServiceTimeMinutes;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getProjectedCompletionTime() {
        return projectedCompletionTime;
    }

    public void setProjectedCompletionTime(LocalDateTime projectedCompletionTime) {
        this.projectedCompletionTime = projectedCompletionTime;
    }

    public Integer getSlaCompliantCount() {
        return slaCompliantCount;
    }

    public void setSlaCompliantCount(Integer slaCompliantCount) {
        this.slaCompliantCount = slaCompliantCount;
    }

    public Integer getSlaAtRiskCount() {
        return slaAtRiskCount;
    }

    public void setSlaAtRiskCount(Integer slaAtRiskCount) {
        this.slaAtRiskCount = slaAtRiskCount;
    }

    public Integer getSlaViolationCount() {
        return slaViolationCount;
    }

    public void setSlaViolationCount(Integer slaViolationCount) {
        this.slaViolationCount = slaViolationCount;
    }

    public Integer getOptimizationScore() {
        return optimizationScore;
    }

    public void setOptimizationScore(Integer optimizationScore) {
        this.optimizationScore = optimizationScore;
    }

    public Double getBaselineDistanceKm() {
        return baselineDistanceKm;
    }

    public void setBaselineDistanceKm(Double baselineDistanceKm) {
        this.baselineDistanceKm = baselineDistanceKm;
    }

    public Integer getBaselineTravelTimeMinutes() {
        return baselineTravelTimeMinutes;
    }

    public void setBaselineTravelTimeMinutes(Integer baselineTravelTimeMinutes) {
        this.baselineTravelTimeMinutes = baselineTravelTimeMinutes;
    }

    public Double getDistanceSavedKm() {
        return distanceSavedKm;
    }

    public void setDistanceSavedKm(Double distanceSavedKm) {
        this.distanceSavedKm = distanceSavedKm;
    }

    public Integer getTimeSavedMinutes() {
        return timeSavedMinutes;
    }

    public void setTimeSavedMinutes(Integer timeSavedMinutes) {
        this.timeSavedMinutes = timeSavedMinutes;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Boolean getIsApplied() {
        return isApplied;
    }

    public void setIsApplied(Boolean isApplied) {
        this.isApplied = isApplied;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

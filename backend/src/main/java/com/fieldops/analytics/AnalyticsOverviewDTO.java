package com.fieldops.analytics;

import java.util.List;
import java.util.Map;

public class AnalyticsOverviewDTO {
    private Double overallSlaRate;
    private Double avgResolutionHours;
    private Long totalJobsCompleted;
    private Double averageRating;
    private List<Map<String, Object>> jobsCompletedOverTime;
    private List<Map<String, Object>> slaRateTrend;
    private List<Map<String, Object>> jobsByPriority;
    private List<Map<String, Object>> jobsByRegion;
    private List<Map<String, Object>> technicianPerformance;
    private List<Map<String, Object>> inventoryConsumption;

    public AnalyticsOverviewDTO() {}

    public AnalyticsOverviewDTO(Double overallSlaRate, Double avgResolutionHours, Long totalJobsCompleted,
                                Double averageRating, List<Map<String, Object>> jobsCompletedOverTime,
                                List<Map<String, Object>> slaRateTrend, List<Map<String, Object>> jobsByPriority,
                                List<Map<String, Object>> jobsByRegion, List<Map<String, Object>> technicianPerformance,
                                List<Map<String, Object>> inventoryConsumption) {
        this.overallSlaRate = overallSlaRate;
        this.avgResolutionHours = avgResolutionHours;
        this.totalJobsCompleted = totalJobsCompleted;
        this.averageRating = averageRating;
        this.jobsCompletedOverTime = jobsCompletedOverTime;
        this.slaRateTrend = slaRateTrend;
        this.jobsByPriority = jobsByPriority;
        this.jobsByRegion = jobsByRegion;
        this.technicianPerformance = technicianPerformance;
        this.inventoryConsumption = inventoryConsumption;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Double overallSlaRate;
        private Double avgResolutionHours;
        private Long totalJobsCompleted;
        private Double averageRating;
        private List<Map<String, Object>> jobsCompletedOverTime;
        private List<Map<String, Object>> slaRateTrend;
        private List<Map<String, Object>> jobsByPriority;
        private List<Map<String, Object>> jobsByRegion;
        private List<Map<String, Object>> technicianPerformance;
        private List<Map<String, Object>> inventoryConsumption;

        public Builder overallSlaRate(Double rate) { this.overallSlaRate = rate; return this; }
        public Builder avgResolutionHours(Double hours) { this.avgResolutionHours = hours; return this; }
        public Builder totalJobsCompleted(Long count) { this.totalJobsCompleted = count; return this; }
        public Builder averageRating(Double rating) { this.averageRating = rating; return this; }
        public Builder jobsCompletedOverTime(List<Map<String, Object>> jobs) { this.jobsCompletedOverTime = jobs; return this; }
        public Builder slaRateTrend(List<Map<String, Object>> trend) { this.slaRateTrend = trend; return this; }
        public Builder jobsByPriority(List<Map<String, Object>> priority) { this.jobsByPriority = priority; return this; }
        public Builder jobsByRegion(List<Map<String, Object>> region) { this.jobsByRegion = region; return this; }
        public Builder technicianPerformance(List<Map<String, Object>> perf) { this.technicianPerformance = perf; return this; }
        public Builder inventoryConsumption(List<Map<String, Object>> inv) { this.inventoryConsumption = inv; return this; }

        public AnalyticsOverviewDTO build() {
            return new AnalyticsOverviewDTO(overallSlaRate, avgResolutionHours, totalJobsCompleted, averageRating,
                    jobsCompletedOverTime, slaRateTrend, jobsByPriority, jobsByRegion, technicianPerformance, inventoryConsumption);
        }
    }

    public Double getOverallSlaRate() { return overallSlaRate; }
    public void setOverallSlaRate(Double overallSlaRate) { this.overallSlaRate = overallSlaRate; }
    public Double getAvgResolutionHours() { return avgResolutionHours; }
    public void setAvgResolutionHours(Double avgResolutionHours) { this.avgResolutionHours = avgResolutionHours; }
    public Long getTotalJobsCompleted() { return totalJobsCompleted; }
    public void setTotalJobsCompleted(Long totalJobsCompleted) { this.totalJobsCompleted = totalJobsCompleted; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public List<Map<String, Object>> getJobsCompletedOverTime() { return jobsCompletedOverTime; }
    public void setJobsCompletedOverTime(List<Map<String, Object>> jobsCompletedOverTime) { this.jobsCompletedOverTime = jobsCompletedOverTime; }
    public List<Map<String, Object>> getSlaRateTrend() { return slaRateTrend; }
    public void setSlaRateTrend(List<Map<String, Object>> slaRateTrend) { this.slaRateTrend = slaRateTrend; }
    public List<Map<String, Object>> getJobsByPriority() { return jobsByPriority; }
    public void setJobsByPriority(List<Map<String, Object>> jobsByPriority) { this.jobsByPriority = jobsByPriority; }
    public List<Map<String, Object>> getJobsByRegion() { return jobsByRegion; }
    public void setJobsByRegion(List<Map<String, Object>> jobsByRegion) { this.jobsByRegion = jobsByRegion; }
    public List<Map<String, Object>> getTechnicianPerformance() { return technicianPerformance; }
    public void setTechnicianPerformance(List<Map<String, Object>> technicianPerformance) { this.technicianPerformance = technicianPerformance; }
    public List<Map<String, Object>> getInventoryConsumption() { return inventoryConsumption; }
    public void setInventoryConsumption(List<Map<String, Object>> inventoryConsumption) { this.inventoryConsumption = inventoryConsumption; }
}

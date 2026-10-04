package com.fieldops.analytics;

import com.fieldops.technician.TechnicianDTO;
import com.fieldops.workorder.WorkOrderDTO;

import java.util.List;
import java.util.Map;

public class DashboardOverviewDTO {
    private Long activeJobs;
    private String activeJobsTrend;
    private Long slaAtRisk;
    private String slaAtRiskTrend;
    private Long availableTechnicians;
    private Long totalTechnicians;
    private String technicianAvailabilityTrend;
    private Long completedToday;
    private String completedTodayTrend;
    private Map<String, Long> pipelineCounts;
    private List<WorkOrderDTO> highRiskJobs;
    private List<TechnicianDTO> technicians;
    private List<WorkOrderDTO> activeWorkOrders;
    private List<RecentActivityDTO> recentActivities;
    private List<Map<String, Object>> technicianUtilization;

    public DashboardOverviewDTO() {}

    public DashboardOverviewDTO(Long activeJobs, String activeJobsTrend, Long slaAtRisk, String slaAtRiskTrend,
                                Long availableTechnicians, Long totalTechnicians, String technicianAvailabilityTrend,
                                Long completedToday, String completedTodayTrend, Map<String, Long> pipelineCounts,
                                List<WorkOrderDTO> highRiskJobs, List<TechnicianDTO> technicians,
                                List<WorkOrderDTO> activeWorkOrders, List<RecentActivityDTO> recentActivities,
                                List<Map<String, Object>> technicianUtilization) {
        this.activeJobs = activeJobs;
        this.activeJobsTrend = activeJobsTrend;
        this.slaAtRisk = slaAtRisk;
        this.slaAtRiskTrend = slaAtRiskTrend;
        this.availableTechnicians = availableTechnicians;
        this.totalTechnicians = totalTechnicians;
        this.technicianAvailabilityTrend = technicianAvailabilityTrend;
        this.completedToday = completedToday;
        this.completedTodayTrend = completedTodayTrend;
        this.pipelineCounts = pipelineCounts;
        this.highRiskJobs = highRiskJobs;
        this.technicians = technicians;
        this.activeWorkOrders = activeWorkOrders;
        this.recentActivities = recentActivities;
        this.technicianUtilization = technicianUtilization;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long activeJobs;
        private String activeJobsTrend;
        private Long slaAtRisk;
        private String slaAtRiskTrend;
        private Long availableTechnicians;
        private Long totalTechnicians;
        private String technicianAvailabilityTrend;
        private Long completedToday;
        private String completedTodayTrend;
        private Map<String, Long> pipelineCounts;
        private List<WorkOrderDTO> highRiskJobs;
        private List<TechnicianDTO> technicians;
        private List<WorkOrderDTO> activeWorkOrders;
        private List<RecentActivityDTO> recentActivities;
        private List<Map<String, Object>> technicianUtilization;

        public Builder activeJobs(Long activeJobs) { this.activeJobs = activeJobs; return this; }
        public Builder activeJobsTrend(String activeJobsTrend) { this.activeJobsTrend = activeJobsTrend; return this; }
        public Builder slaAtRisk(Long slaAtRisk) { this.slaAtRisk = slaAtRisk; return this; }
        public Builder slaAtRiskTrend(String slaAtRiskTrend) { this.slaAtRiskTrend = slaAtRiskTrend; return this; }
        public Builder availableTechnicians(Long availableTechnicians) { this.availableTechnicians = availableTechnicians; return this; }
        public Builder totalTechnicians(Long totalTechnicians) { this.totalTechnicians = totalTechnicians; return this; }
        public Builder technicianAvailabilityTrend(String trend) { this.technicianAvailabilityTrend = trend; return this; }
        public Builder completedToday(Long completedToday) { this.completedToday = completedToday; return this; }
        public Builder completedTodayTrend(String trend) { this.completedTodayTrend = trend; return this; }
        public Builder pipelineCounts(Map<String, Long> counts) { this.pipelineCounts = counts; return this; }
        public Builder highRiskJobs(List<WorkOrderDTO> jobs) { this.highRiskJobs = jobs; return this; }
        public Builder technicians(List<TechnicianDTO> techs) { this.technicians = techs; return this; }
        public Builder activeWorkOrders(List<WorkOrderDTO> orders) { this.activeWorkOrders = orders; return this; }
        public Builder recentActivities(List<RecentActivityDTO> activities) { this.recentActivities = activities; return this; }
        public Builder technicianUtilization(List<Map<String, Object>> util) { this.technicianUtilization = util; return this; }

        public DashboardOverviewDTO build() {
            return new DashboardOverviewDTO(activeJobs, activeJobsTrend, slaAtRisk, slaAtRiskTrend, availableTechnicians,
                    totalTechnicians, technicianAvailabilityTrend, completedToday, completedTodayTrend,
                    pipelineCounts, highRiskJobs, technicians, activeWorkOrders, recentActivities, technicianUtilization);
        }
    }

    public Long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(Long activeJobs) { this.activeJobs = activeJobs; }
    public String getActiveJobsTrend() { return activeJobsTrend; }
    public void setActiveJobsTrend(String activeJobsTrend) { this.activeJobsTrend = activeJobsTrend; }
    public Long getSlaAtRisk() { return slaAtRisk; }
    public void setSlaAtRisk(Long slaAtRisk) { this.slaAtRisk = slaAtRisk; }
    public String getSlaAtRiskTrend() { return slaAtRiskTrend; }
    public void setSlaAtRiskTrend(String slaAtRiskTrend) { this.slaAtRiskTrend = slaAtRiskTrend; }
    public Long getAvailableTechnicians() { return availableTechnicians; }
    public void setAvailableTechnicians(Long availableTechnicians) { this.availableTechnicians = availableTechnicians; }
    public Long getTotalTechnicians() { return totalTechnicians; }
    public void setTotalTechnicians(Long totalTechnicians) { this.totalTechnicians = totalTechnicians; }
    public String getTechnicianAvailabilityTrend() { return technicianAvailabilityTrend; }
    public void setTechnicianAvailabilityTrend(String trend) { this.technicianAvailabilityTrend = trend; }
    public Long getCompletedToday() { return completedToday; }
    public void setCompletedToday(Long completedToday) { this.completedToday = completedToday; }
    public String getCompletedTodayTrend() { return completedTodayTrend; }
    public void setCompletedTodayTrend(String trend) { this.completedTodayTrend = trend; }
    public Map<String, Long> getPipelineCounts() { return pipelineCounts; }
    public void setPipelineCounts(Map<String, Long> counts) { this.pipelineCounts = counts; }
    public List<WorkOrderDTO> getHighRiskJobs() { return highRiskJobs; }
    public void setHighRiskJobs(List<WorkOrderDTO> jobs) { this.highRiskJobs = jobs; }
    public List<TechnicianDTO> getTechnicians() { return technicians; }
    public void setTechnicians(List<TechnicianDTO> technicians) { this.technicians = technicians; }
    public List<WorkOrderDTO> getActiveWorkOrders() { return activeWorkOrders; }
    public void setActiveWorkOrders(List<WorkOrderDTO> orders) { this.activeWorkOrders = orders; }
    public List<RecentActivityDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivityDTO> activities) { this.recentActivities = activities; }
    public List<Map<String, Object>> getTechnicianUtilization() { return technicianUtilization; }
    public void setTechnicianUtilization(List<Map<String, Object>> util) { this.technicianUtilization = util; }
}

package com.fieldops.routing;

public class RouteEstimate {

    private Long workOrderId;
    private Long technicianId;
    private String technicianName;
    private Double distanceKm;
    private Integer estimatedTravelMinutes;
    private Long slaRemainingMinutes;
    private String travelFeasibility; // FEASIBLE, AT_RISK, UNFEASIBLE
    private Double technicianLatitude;
    private Double technicianLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String destinationAddress;

    public RouteEstimate() {}

    public RouteEstimate(Long workOrderId, Long technicianId, String technicianName, Double distanceKm,
                         Integer estimatedTravelMinutes, Long slaRemainingMinutes, String travelFeasibility,
                         Double technicianLatitude, Double technicianLongitude, Double destinationLatitude,
                         Double destinationLongitude, String destinationAddress) {
        this.workOrderId = workOrderId;
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.distanceKm = distanceKm;
        this.estimatedTravelMinutes = estimatedTravelMinutes;
        this.slaRemainingMinutes = slaRemainingMinutes;
        this.travelFeasibility = travelFeasibility;
        this.technicianLatitude = technicianLatitude;
        this.technicianLongitude = technicianLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationAddress = destinationAddress;
    }

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getEstimatedTravelMinutes() { return estimatedTravelMinutes; }
    public void setEstimatedTravelMinutes(Integer estimatedTravelMinutes) { this.estimatedTravelMinutes = estimatedTravelMinutes; }

    public Long getSlaRemainingMinutes() { return slaRemainingMinutes; }
    public void setSlaRemainingMinutes(Long slaRemainingMinutes) { this.slaRemainingMinutes = slaRemainingMinutes; }

    public String getTravelFeasibility() { return travelFeasibility; }
    public void setTravelFeasibility(String travelFeasibility) { this.travelFeasibility = travelFeasibility; }

    public Double getTechnicianLatitude() { return technicianLatitude; }
    public void setTechnicianLatitude(Double technicianLatitude) { this.technicianLatitude = technicianLatitude; }

    public Double getTechnicianLongitude() { return technicianLongitude; }
    public void setTechnicianLongitude(Double technicianLongitude) { this.technicianLongitude = technicianLongitude; }

    public Double getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(Double destinationLatitude) { this.destinationLatitude = destinationLatitude; }

    public Double getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(Double destinationLongitude) { this.destinationLongitude = destinationLongitude; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
}

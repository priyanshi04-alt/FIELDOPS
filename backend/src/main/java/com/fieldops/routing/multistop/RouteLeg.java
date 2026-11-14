package com.fieldops.routing.multistop;

public class RouteLeg {

    private Integer legNumber;
    private String fromName;
    private String toName;
    private Double fromLat;
    private Double fromLon;
    private Double toLat;
    private Double toLon;
    private Double distanceKm;
    private Integer travelTimeMinutes;

    public RouteLeg() {}

    public RouteLeg(Integer legNumber, String fromName, String toName,
                    Double fromLat, Double fromLon, Double toLat, Double toLon,
                    Double distanceKm, Integer travelTimeMinutes) {
        this.legNumber = legNumber;
        this.fromName = fromName;
        this.toName = toName;
        this.fromLat = fromLat;
        this.fromLon = fromLon;
        this.toLat = toLat;
        this.toLon = toLon;
        this.distanceKm = distanceKm;
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public Integer getLegNumber() {
        return legNumber;
    }

    public void setLegNumber(Integer legNumber) {
        this.legNumber = legNumber;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Double getFromLat() {
        return fromLat;
    }

    public void setFromLat(Double fromLat) {
        this.fromLat = fromLat;
    }

    public Double getFromLon() {
        return fromLon;
    }

    public void setFromLon(Double fromLon) {
        this.fromLon = fromLon;
    }

    public Double getToLat() {
        return toLat;
    }

    public void setToLat(Double toLat) {
        this.toLat = toLat;
    }

    public Double getToLon() {
        return toLon;
    }

    public void setToLon(Double toLon) {
        this.toLon = toLon;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public void setTravelTimeMinutes(Integer travelTimeMinutes) {
        this.travelTimeMinutes = travelTimeMinutes;
    }
}

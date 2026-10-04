package com.fieldops.routing;

public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates great-circle distance between two GPS coordinates using Haversine formula.
     */
    public static double calculateHaversineDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 8.0; // fallback standard urban distance if coordinates missing
        }
        if (lat1.equals(lat2) && lon1.equals(lon2)) {
            return 0.1;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;
        return Math.round(distance * 10.0) / 10.0;
    }

    /**
     * Estimates urban transit time in minutes given distance in km and city traffic multiplier.
     * Assumes average city speed of ~25 km/h + 5 min dispatch/parking buffer.
     */
    public static int estimateTravelMinutes(double distanceKm) {
        if (distanceKm <= 0) return 5;
        double travelHours = distanceKm / 24.0; // ~24 km/h metropolitan average
        int transitMinutes = (int) Math.round(travelHours * 60) + 4; // buffer
        return Math.max(5, transitMinutes);
    }
}

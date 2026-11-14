package com.fieldops.routing.multistop;

import com.fieldops.routing.GeoUtils;
import com.fieldops.technician.Technician;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderPriority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteOptimizationService {

    /**
     * Optimizes a multi-stop route starting from technician's current GPS position
     * using SLA-weighted Nearest Neighbor heuristic followed by 2-Opt iterative improvement.
     */
    public OptimizedRoute optimizeRoute(Technician technician, List<WorkOrder> workOrders, LocalDateTime departureTime) {
        if (departureTime == null) {
            departureTime = LocalDateTime.now();
        }

        OptimizedRoute result = new OptimizedRoute();
        result.setTechnicianId(technician.getId());
        result.setTechnicianName(technician.getName());
        result.setTechnicianStatus(technician.getStatus().name());
        result.setOriginAddress(technician.getAddress() != null ? technician.getAddress() : technician.getCity());
        result.setOriginLat(technician.getLatitude());
        result.setOriginLon(technician.getLongitude());
        result.setStartTime(departureTime);

        if (workOrders == null || workOrders.isEmpty()) {
            result.setExplanation("No assigned or pending work orders found for technician.");
            result.setProjectedCompletionTime(departureTime);
            result.setOptimizationScore(100);
            return result;
        }

        // Default coordinates fallback if technician has no GPS
        double originLat = technician.getLatitude() != null ? technician.getLatitude() : 28.6139; // Delhi default
        double originLon = technician.getLongitude() != null ? technician.getLongitude() : 77.2090;

        // 1. Evaluate Baseline Route (current natural order by priority & deadline)
        List<WorkOrder> baselineOrder = new ArrayList<>(workOrders);
        baselineOrder.sort(Comparator.comparing(WorkOrder::getPriority, Comparator.reverseOrder())
                .thenComparing(wo -> wo.getSlaDeadline() != null ? wo.getSlaDeadline() : LocalDateTime.MAX));

        RouteMetrics baselineMetrics = calculateSequenceMetrics(originLat, originLon, baselineOrder, departureTime);

        // 2. Step 1: SLA-Urgency-Weighted Nearest Neighbor Constructive Heuristic
        List<WorkOrder> optimizedOrder = solveSlaWeightedNearestNeighbor(originLat, originLon, workOrders, departureTime);

        // 3. Step 2: 2-Opt Iterative Edge-Swap Improvement
        if (optimizedOrder.size() > 2) {
            optimizedOrder = applyTwoOptImprovement(originLat, originLon, optimizedOrder, departureTime);
        }

        // 4. Calculate Final Detailed Metrics and Stops
        RouteMetrics finalMetrics = calculateSequenceMetrics(originLat, originLon, optimizedOrder, departureTime);

        // Populate stops
        List<RouteStop> routeStops = new ArrayList<>();
        double currentLat = originLat;
        double currentLon = originLon;
        LocalDateTime simulatedTime = departureTime;

        for (int i = 0; i < optimizedOrder.size(); i++) {
            WorkOrder wo = optimizedOrder.get(i);
            double targetLat = wo.getLatitude() != null ? wo.getLatitude() : originLat;
            double targetLon = wo.getLongitude() != null ? wo.getLongitude() : originLon;

            double legDist = GeoUtils.calculateHaversineDistanceKm(currentLat, currentLon, targetLat, targetLon);
            int transitMinutes = GeoUtils.estimateTravelMinutes(legDist);

            LocalDateTime arrival = simulatedTime.plusMinutes(transitMinutes);
            int serviceMinutes = (int) Math.round((wo.getEstimatedDurationHours() != null ? wo.getEstimatedDurationHours() : 1.5) * 60);
            LocalDateTime departure = arrival.plusMinutes(serviceMinutes);

            long slaRemainingAtArrival = wo.getSlaDeadline() != null
                    ? Duration.between(arrival, wo.getSlaDeadline()).toMinutes()
                    : 180;

            String slaStatus;
            if (slaRemainingAtArrival < 0) {
                slaStatus = "VIOLATION";
            } else if (slaRemainingAtArrival <= 45) {
                slaStatus = "AT_RISK";
            } else {
                slaStatus = "ON_TRACK";
            }

            RouteStop stop = new RouteStop();
            stop.setWorkOrderId(wo.getId());
            stop.setWorkOrderNumber(wo.getWorkOrderNumber());
            stop.setTitle(wo.getTitle());
            stop.setPriority(wo.getPriority().name());
            stop.setStatus(wo.getStatus().name());
            stop.setCustomerName(wo.getCustomer() != null ? wo.getCustomer().getName() : "Enterprise Customer");
            stop.setLocationName(wo.getLocationName());
            stop.setAddress(wo.getAddress() != null ? wo.getAddress() : wo.getCity());
            stop.setLatitude(targetLat);
            stop.setLongitude(targetLon);
            stop.setSlaDeadline(wo.getSlaDeadline());
            stop.setSlaRemainingMinutes(slaRemainingAtArrival);
            stop.setServiceDurationMinutes(serviceMinutes);
            stop.setSequenceNumber(i + 1);
            stop.setEstimatedArrival(arrival);
            stop.setEstimatedDeparture(departure);
            stop.setTravelTimeFromPreviousMinutes(transitMinutes);
            stop.setDistanceFromPreviousKm(legDist);
            stop.setSlaStatus(slaStatus);

            routeStops.add(stop);

            // Advance simulation
            currentLat = targetLat;
            currentLon = targetLon;
            simulatedTime = departure;
        }

        // Populate route legs
        List<RouteLeg> legs = new ArrayList<>();
        double legOriginLat = originLat;
        double legOriginLon = originLon;
        String legOriginName = technician.getName() + " (Start)";

        for (int i = 0; i < routeStops.size(); i++) {
            RouteStop stop = routeStops.get(i);
            legs.add(new RouteLeg(
                    i + 1,
                    legOriginName,
                    stop.getWorkOrderNumber() + " - " + stop.getTitle(),
                    legOriginLat,
                    legOriginLon,
                    stop.getLatitude(),
                    stop.getLongitude(),
                    stop.getDistanceFromPreviousKm(),
                    stop.getTravelTimeFromPreviousMinutes()
            ));
            legOriginLat = stop.getLatitude();
            legOriginLon = stop.getLongitude();
            legOriginName = stop.getWorkOrderNumber();
        }

        // Savings and Scoring
        double distanceSaved = Math.max(0.0, Math.round((baselineMetrics.totalDistanceKm - finalMetrics.totalDistanceKm) * 10.0) / 10.0);
        int timeSaved = Math.max(0, baselineMetrics.totalTravelTimeMinutes - finalMetrics.totalTravelTimeMinutes);

        int slaViolations = (int) routeStops.stream().filter(s -> "VIOLATION".equals(s.getSlaStatus())).count();
        int slaAtRisk = (int) routeStops.stream().filter(s -> "AT_RISK".equals(s.getSlaStatus())).count();
        int slaOnTrack = (int) routeStops.stream().filter(s -> "ON_TRACK".equals(s.getSlaStatus())).count();

        // Optimization score (100 base, penalizing distance and SLA risk)
        int score = 100 - (slaViolations * 35) - (slaAtRisk * 12);
        if (distanceSaved > 0) score = Math.min(100, score + 5);
        score = Math.max(20, Math.min(100, score));

        result.setStops(routeStops);
        result.setLegs(legs);
        result.setTotalStops(routeStops.size());
        result.setTotalDistanceKm(finalMetrics.totalDistanceKm);
        result.setTotalTravelTimeMinutes(finalMetrics.totalTravelTimeMinutes);
        result.setTotalServiceTimeMinutes(finalMetrics.totalServiceTimeMinutes);
        result.setTotalDurationMinutes(finalMetrics.totalTravelTimeMinutes + finalMetrics.totalServiceTimeMinutes);
        result.setProjectedCompletionTime(simulatedTime);

        result.setSlaCompliantCount(slaOnTrack);
        result.setSlaAtRiskCount(slaAtRisk);
        result.setSlaViolationCount(slaViolations);

        result.setOptimizationScore(score);
        result.setBaselineDistanceKm(baselineMetrics.totalDistanceKm);
        result.setBaselineTravelTimeMinutes(baselineMetrics.totalTravelTimeMinutes);
        result.setDistanceSavedKm(distanceSaved);
        result.setTimeSavedMinutes(timeSaved);

        // Explanation builder
        String explanation = buildExplanation(technician.getName(), routeStops.size(),
                finalMetrics.totalDistanceKm, distanceSaved, timeSaved, slaViolations, slaAtRisk, slaOnTrack);
        result.setExplanation(explanation);

        return result;
    }

    /**
     * SLA-Weighted Nearest Neighbor heuristic.
     */
    private List<WorkOrder> solveSlaWeightedNearestNeighbor(double originLat, double originLon,
                                                           List<WorkOrder> workOrders, LocalDateTime startTime) {
        List<WorkOrder> remaining = new ArrayList<>(workOrders);
        List<WorkOrder> sequence = new ArrayList<>();

        double currLat = originLat;
        double currLon = originLon;
        LocalDateTime simulatedTime = startTime;

        while (!remaining.isEmpty()) {
            WorkOrder bestNext = null;
            double bestScore = Double.MAX_VALUE;

            for (WorkOrder candidate : remaining) {
                double targetLat = candidate.getLatitude() != null ? candidate.getLatitude() : originLat;
                double targetLon = candidate.getLongitude() != null ? candidate.getLongitude() : originLon;

                double distance = GeoUtils.calculateHaversineDistanceKm(currLat, currLon, targetLat, targetLon);
                int travelMinutes = GeoUtils.estimateTravelMinutes(distance);
                LocalDateTime arrival = simulatedTime.plusMinutes(travelMinutes);

                // Priority weights: CRITICAL > HIGH > MEDIUM > LOW
                double priorityBonus = getPriorityWeight(candidate.getPriority());

                // SLA Urgency penalty: sooner deadline gets higher priority
                double slaUrgencyBonus = 0.0;
                if (candidate.getSlaDeadline() != null) {
                    long minutesUntilSla = Duration.between(arrival, candidate.getSlaDeadline()).toMinutes();
                    if (minutesUntilSla < 0) {
                        slaUrgencyBonus = 50.0; // Severe breach danger
                    } else if (minutesUntilSla < 90) {
                        slaUrgencyBonus = 35.0;
                    } else if (minutesUntilSla < 180) {
                        slaUrgencyBonus = 20.0;
                    } else if (minutesUntilSla < 360) {
                        slaUrgencyBonus = 10.0;
                    }
                }

                // Deterministic Cost = (Distance * 1.2) + (TravelTime * 0.4) - PriorityBonus - SlaUrgencyBonus
                double candidateCost = (distance * 1.2) + (travelMinutes * 0.4) - (priorityBonus * 1.5) - (slaUrgencyBonus * 1.2);

                if (candidateCost < bestScore) {
                    bestScore = candidateCost;
                    bestNext = candidate;
                }
            }

            if (bestNext == null) {
                bestNext = remaining.get(0);
            }

            sequence.add(bestNext);
            remaining.remove(bestNext);

            // Advance current location and time
            currLat = bestNext.getLatitude() != null ? bestNext.getLatitude() : originLat;
            currLon = bestNext.getLongitude() != null ? bestNext.getLongitude() : originLon;
            double dist = GeoUtils.calculateHaversineDistanceKm(currLat, currLon, currLat, currLon);
            int transitMin = GeoUtils.estimateTravelMinutes(dist);
            int servMin = (int) Math.round((bestNext.getEstimatedDurationHours() != null ? bestNext.getEstimatedDurationHours() : 1.5) * 60);
            simulatedTime = simulatedTime.plusMinutes(transitMin + servMin);
        }

        return sequence;
    }

    /**
     * 2-Opt local search improvement. Swaps 2 non-adjacent edges if the resulting
     * composite tour cost is lower without inducing fresh SLA breaches.
     */
    private List<WorkOrder> applyTwoOptImprovement(double originLat, double originLon,
                                                  List<WorkOrder> tour, LocalDateTime startTime) {
        List<WorkOrder> bestTour = new ArrayList<>(tour);
        double bestCost = computeCompositeTourCost(originLat, originLon, bestTour, startTime);
        boolean improved = true;
        int maxIterations = 50;
        int iteration = 0;

        while (improved && iteration < maxIterations) {
            improved = false;
            iteration++;

            for (int i = 0; i < bestTour.size() - 1; i++) {
                for (int k = i + 1; k < bestTour.size(); k++) {
                    List<WorkOrder> newTour = twoOptSwap(bestTour, i, k);
                    double newCost = computeCompositeTourCost(originLat, originLon, newTour, startTime);

                    if (newCost < bestCost - 0.01) {
                        bestCost = newCost;
                        bestTour = newTour;
                        improved = true;
                        break;
                    }
                }
                if (improved) break;
            }
        }

        return bestTour;
    }

    private List<WorkOrder> twoOptSwap(List<WorkOrder> tour, int i, int k) {
        List<WorkOrder> newTour = new ArrayList<>();
        // 1. Take tour[0] to tour[i-1]
        for (int c = 0; c < i; c++) {
            newTour.add(tour.get(c));
        }
        // 2. Reverse tour[i] to tour[k]
        for (int c = k; c >= i; c--) {
            newTour.add(tour.get(c));
        }
        // 3. Take tour[k+1] to end
        for (int c = k + 1; c < tour.size(); c++) {
            newTour.add(tour.get(c));
        }
        return newTour;
    }

    /**
     * Composite tour cost combining travel distance and SLA penalty.
     */
    private double computeCompositeTourCost(double originLat, double originLon, List<WorkOrder> tour, LocalDateTime startTime) {
        double totalDistance = 0.0;
        double currentLat = originLat;
        double currentLon = originLon;
        LocalDateTime simulatedTime = startTime;
        double slaPenalty = 0.0;

        for (WorkOrder wo : tour) {
            double targetLat = wo.getLatitude() != null ? wo.getLatitude() : originLat;
            double targetLon = wo.getLongitude() != null ? wo.getLongitude() : originLon;

            double dist = GeoUtils.calculateHaversineDistanceKm(currentLat, currentLon, targetLat, targetLon);
            int transitMinutes = GeoUtils.estimateTravelMinutes(dist);
            LocalDateTime arrival = simulatedTime.plusMinutes(transitMinutes);

            if (wo.getSlaDeadline() != null) {
                long minutesUntilSla = Duration.between(arrival, wo.getSlaDeadline()).toMinutes();
                if (minutesUntilSla < 0) {
                    // Critical penalty for causing breach
                    slaPenalty += 100.0 + Math.abs(minutesUntilSla) * 0.5;
                } else if (minutesUntilSla < 30) {
                    slaPenalty += 30.0;
                }
            }

            totalDistance += dist;
            currentLat = targetLat;
            currentLon = targetLon;
            int servMin = (int) Math.round((wo.getEstimatedDurationHours() != null ? wo.getEstimatedDurationHours() : 1.5) * 60);
            simulatedTime = arrival.plusMinutes(servMin);
        }

        return totalDistance + (slaPenalty * 0.8);
    }

    private RouteMetrics calculateSequenceMetrics(double originLat, double originLon, List<WorkOrder> orders, LocalDateTime startTime) {
        double distKm = 0.0;
        int travelMin = 0;
        int serviceMin = 0;
        double currentLat = originLat;
        double currentLon = originLon;

        for (WorkOrder wo : orders) {
            double targetLat = wo.getLatitude() != null ? wo.getLatitude() : originLat;
            double targetLon = wo.getLongitude() != null ? wo.getLongitude() : originLon;

            double legDist = GeoUtils.calculateHaversineDistanceKm(currentLat, currentLon, targetLat, targetLon);
            int transit = GeoUtils.estimateTravelMinutes(legDist);
            int serv = (int) Math.round((wo.getEstimatedDurationHours() != null ? wo.getEstimatedDurationHours() : 1.5) * 60);

            distKm += legDist;
            travelMin += transit;
            serviceMin += serv;

            currentLat = targetLat;
            currentLon = targetLon;
        }

        RouteMetrics m = new RouteMetrics();
        m.totalDistanceKm = Math.round(distKm * 10.0) / 10.0;
        m.totalTravelTimeMinutes = travelMin;
        m.totalServiceTimeMinutes = serviceMin;
        return m;
    }

    private double getPriorityWeight(WorkOrderPriority priority) {
        if (priority == null) return 10.0;
        switch (priority) {
            case CRITICAL: return 40.0;
            case HIGH: return 25.0;
            case MEDIUM: return 12.0;
            case LOW: return 4.0;
            default: return 10.0;
        }
    }

    private String buildExplanation(String techName, int stopsCount, double distanceKm,
                                     double distanceSaved, int timeSaved,
                                     int violations, int atRisk, int onTrack) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Deterministic multi-stop itinerary engineered for %s across %d sequential jobs. ",
                techName, stopsCount));

        if (violations > 0) {
            sb.append(String.format("Prioritizes imminent SLA deadlines: %d jobs on track, %d at risk, with %d projected breach warnings. ",
                    onTrack, atRisk, violations));
        } else if (atRisk > 0) {
            sb.append(String.format("All jobs reachable with 0 breaches (%d safe, %d close to threshold). ", onTrack, atRisk));
        } else {
            sb.append(String.format("All %d jobs projected comfortably within SLA deadlines. ", onTrack));
        }

        if (distanceSaved > 0.0 || timeSaved > 0) {
            sb.append(String.format("2-Opt heuristic optimization yielded ~%.1f km reduction and %d min transit savings compared to naive priority sequence.",
                    distanceSaved, timeSaved));
        } else {
            sb.append("Direct proximity-first path aligned with critical contractual deadlines.");
        }

        return sb.toString();
    }

    private static class RouteMetrics {
        double totalDistanceKm;
        int totalTravelTimeMinutes;
        int totalServiceTimeMinutes;
    }
}

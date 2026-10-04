package com.fieldops.routing;

import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class RouteIntelligenceService {

    private final WorkOrderRepository workOrderRepository;
    private final TechnicianRepository technicianRepository;

    public RouteIntelligenceService(WorkOrderRepository workOrderRepository,
                                    TechnicianRepository technicianRepository) {
        this.workOrderRepository = workOrderRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional(readOnly = true)
    public RouteEstimate getRouteEstimate(Long workOrderId, Long specificTechnicianId) {
        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + workOrderId));

        Technician tech = null;
        if (specificTechnicianId != null) {
            tech = technicianRepository.findById(specificTechnicianId).orElse(null);
        } else if (wo.getTechnician() != null) {
            tech = wo.getTechnician();
        }

        return calculateEstimate(wo, tech);
    }

    public RouteEstimate calculateEstimate(WorkOrder wo, Technician tech) {
        Double techLat = tech != null ? tech.getLatitude() : null;
        Double techLon = tech != null ? tech.getLongitude() : null;
        Double destLat = wo.getLatitude();
        Double destLon = wo.getLongitude();

        double distance = GeoUtils.calculateHaversineDistanceKm(techLat, techLon, destLat, destLon);
        int etaMinutes = GeoUtils.estimateTravelMinutes(distance);

        long slaRemainingMinutes = 0;
        if (wo.getSlaDeadline() != null) {
            Duration dur = Duration.between(LocalDateTime.now(), wo.getSlaDeadline());
            slaRemainingMinutes = Math.max(0, dur.toMinutes());
        }

        String feasibility = "FEASIBLE";
        if (slaRemainingMinutes <= 0) {
            feasibility = "BREACHED";
        } else if (etaMinutes >= slaRemainingMinutes) {
            feasibility = "UNFEASIBLE";
        } else if ((slaRemainingMinutes - etaMinutes) < 20) {
            feasibility = "AT_RISK";
        }

        return new RouteEstimate(
                wo.getId(),
                tech != null ? tech.getId() : null,
                tech != null ? tech.getName() : "Unassigned",
                distance,
                etaMinutes,
                slaRemainingMinutes,
                feasibility,
                techLat,
                techLon,
                destLat,
                destLon,
                wo.getAddress() != null ? wo.getAddress() : wo.getLocationName()
        );
    }
}

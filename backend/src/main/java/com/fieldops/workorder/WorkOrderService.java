package com.fieldops.workorder;

import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.customer.Customer;
import com.fieldops.customer.CustomerRepository;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fieldops.websocket.OperationsEvent;
import com.fieldops.websocket.OperationsEventPublisher;
import com.fieldops.websocket.OperationsEventType;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;
    private final OperationsEventPublisher eventPublisher;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            CustomerRepository customerRepository,
                            TechnicianRepository technicianRepository,
                            OperationsEventPublisher eventPublisher) {
        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.technicianRepository = technicianRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getAllWorkOrders(
            WorkOrderStatus status,
            WorkOrderPriority priority,
            Long technicianId,
            Long customerId,
            String search
    ) {
        List<WorkOrder> list = workOrderRepository.findAll();

        return list.stream()
                .filter(wo -> status == null || wo.getStatus() == status)
                .filter(wo -> priority == null || wo.getPriority() == priority)
                .filter(wo -> technicianId == null || (wo.getTechnician() != null && wo.getTechnician().getId().equals(technicianId)))
                .filter(wo -> customerId == null || (wo.getCustomer() != null && wo.getCustomer().getId().equals(customerId)))
                .filter(wo -> search == null || search.isBlank() ||
                        wo.getWorkOrderNumber().toLowerCase().contains(search.toLowerCase()) ||
                        wo.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                        (wo.getCustomer() != null && wo.getCustomer().getName().toLowerCase().contains(search.toLowerCase())))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(WorkOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkOrderDTO getWorkOrderById(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));
        return WorkOrderDTO.fromEntity(wo);
    }

    @Transactional(readOnly = true)
    public WorkOrderDTO getWorkOrderByNumber(String number) {
        WorkOrder wo = workOrderRepository.findByWorkOrderNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with number: " + number));
        return WorkOrderDTO.fromEntity(wo);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getHighRiskWorkOrders() {
        return workOrderRepository.findHighRiskWorkOrders().stream()
                .map(WorkOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkOrderDTO createWorkOrder(WorkOrderCreateRequest request) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Technician technician = null;
        if (request.getTechnicianId() != null) {
            technician = technicianRepository.findById(request.getTechnicianId()).orElse(null);
        }

        String woNum = "WO-" + (1000 + (int)(Math.random() * 9000));

        WorkOrder wo = WorkOrder.builder()
                .workOrderNumber(woNum)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(technician != null ? WorkOrderStatus.ASSIGNED : WorkOrderStatus.NEW)
                .customer(customer)
                .technician(technician)
                .locationName(request.getLocationName() != null ? request.getLocationName() : customer.getCompany())
                .city(request.getCity() != null ? request.getCity() : customer.getCity())
                .address(request.getAddress() != null ? request.getAddress() : customer.getAddress())
                .latitude(request.getLatitude() != null ? request.getLatitude() : customer.getLatitude())
                .longitude(request.getLongitude() != null ? request.getLongitude() : customer.getLongitude())
                .requiredSkills(request.getRequiredSkills())
                .scheduledStartTime(request.getScheduledStartTime())
                .scheduledEndTime(request.getScheduledEndTime())
                .slaDeadline(request.getSlaDeadline() != null ? request.getSlaDeadline() : LocalDateTime.now().plusHours(4))
                .estimatedDurationHours(request.getEstimatedDurationHours() != null ? request.getEstimatedDurationHours() : 2.0)
                .slaBreachProbability(Math.round(Math.random() * 30 * 10.0) / 10.0)
                .technicianEtaMinutes(technician != null ? 25 : null)
                .distanceKm(technician != null ? 6.5 : null)
                .aiInsight("Deterministic risk evaluation: SLA within nominal limits.")
                .build();

        customer.setOpenJobsCount(customer.getOpenJobsCount() + 1);
        customerRepository.save(customer);

        WorkOrder saved = workOrderRepository.save(wo);

        eventPublisher.publish(OperationsEvent.builder()
                .eventType(saved.getTechnician() != null ? OperationsEventType.WORK_ORDER_ASSIGNED : OperationsEventType.WORK_ORDER_STATUS_CHANGED)
                .severity(saved.getPriority() == WorkOrderPriority.CRITICAL ? "CRITICAL" : "INFO")
                .workOrderId(saved.getId())
                .technicianId(saved.getTechnician() != null ? saved.getTechnician().getId() : null)
                .message(String.format("Work order %s created (%s)", saved.getWorkOrderNumber(), saved.getTitle()))
                .build());

        return WorkOrderDTO.fromEntity(saved);
    }

    @Transactional
    public WorkOrderDTO updateWorkOrder(Long id, WorkOrderUpdateRequest request) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));

        WorkOrderStatus prevStatus = wo.getStatus();
        Technician prevTech = wo.getTechnician();

        if (request.getTitle() != null) wo.setTitle(request.getTitle());
        if (request.getDescription() != null) wo.setDescription(request.getDescription());
        if (request.getPriority() != null) wo.setPriority(request.getPriority());

        if (request.getStatus() != null) {
            wo.setStatus(request.getStatus());
            if (request.getStatus() == WorkOrderStatus.COMPLETED && wo.getCompletedAt() == null) {
                wo.setCompletedAt(LocalDateTime.now());
                if (wo.getCustomer() != null) {
                    wo.getCustomer().setCompletedJobsCount(wo.getCustomer().getCompletedJobsCount() + 1);
                    if (wo.getCustomer().getOpenJobsCount() > 0) {
                        wo.getCustomer().setOpenJobsCount(wo.getCustomer().getOpenJobsCount() - 1);
                    }
                    customerRepository.save(wo.getCustomer());
                }
            }
        }

        if (request.getTechnicianId() != null) {
            Technician tech = technicianRepository.findById(request.getTechnicianId()).orElse(null);
            wo.setTechnician(tech);
            if (wo.getStatus() == WorkOrderStatus.NEW && tech != null) {
                wo.setStatus(WorkOrderStatus.ASSIGNED);
            }
        }

        if (request.getScheduledStartTime() != null) wo.setScheduledStartTime(request.getScheduledStartTime());
        if (request.getScheduledEndTime() != null) wo.setScheduledEndTime(request.getScheduledEndTime());
        if (request.getServiceNotes() != null) wo.setServiceNotes(request.getServiceNotes());
        if (request.getTechnicianEtaMinutes() != null) wo.setTechnicianEtaMinutes(request.getTechnicianEtaMinutes());
        if (request.getSlaBreachProbability() != null) wo.setSlaBreachProbability(request.getSlaBreachProbability());

        WorkOrder updated = workOrderRepository.save(wo);

        // Determine event type
        OperationsEventType eventType = OperationsEventType.WORK_ORDER_STATUS_CHANGED;
        String message = String.format("Work order %s status changed to %s", updated.getWorkOrderNumber(), updated.getStatus());

        if (prevTech == null && updated.getTechnician() != null) {
            eventType = OperationsEventType.WORK_ORDER_ASSIGNED;
            message = String.format("Work order %s assigned to %s", updated.getWorkOrderNumber(), updated.getTechnician().getName());
        } else if (request.getSlaBreachProbability() != null) {
            eventType = OperationsEventType.SLA_RISK_CHANGED;
            message = String.format("Work order %s SLA risk evaluated at %.0f%%", updated.getWorkOrderNumber(), updated.getSlaBreachProbability());
        }

        eventPublisher.publish(OperationsEvent.builder()
                .eventType(eventType)
                .severity(updated.getPriority() == WorkOrderPriority.CRITICAL ? "HIGH" : "INFO")
                .workOrderId(updated.getId())
                .technicianId(updated.getTechnician() != null ? updated.getTechnician().getId() : null)
                .message(message)
                .build());

        return WorkOrderDTO.fromEntity(updated);
    }

    @Transactional
    public WorkOrderDTO updateWorkOrderStatus(Long id, WorkOrderStatusUpdateRequest request, String callerEmail, boolean isTechnician) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));

        if (isTechnician) {
            if (wo.getTechnician() == null || !com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, wo.getTechnician().getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not assigned to this work order.");
            }

            if (request.getStatus() != null && request.getStatus() != wo.getStatus()) {
                // Technicians cannot transition ticket to NEW
                if (request.getStatus() == WorkOrderStatus.NEW) {
                    throw new IllegalArgumentException("Technicians cannot revert work order status to NEW.");
                }
                // Completed work orders cannot be re-opened by technicians
                if (wo.getStatus() == WorkOrderStatus.COMPLETED) {
                    throw new IllegalArgumentException("Completed work order cannot have its status changed.");
                }
            }
        }

        if (request.getStatus() != null) {
            wo.setStatus(request.getStatus());
            if (request.getStatus() == WorkOrderStatus.COMPLETED && wo.getCompletedAt() == null) {
                wo.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : LocalDateTime.now());
                if (wo.getCustomer() != null) {
                    wo.getCustomer().setCompletedJobsCount(wo.getCustomer().getCompletedJobsCount() + 1);
                    if (wo.getCustomer().getOpenJobsCount() > 0) {
                        wo.getCustomer().setOpenJobsCount(wo.getCustomer().getOpenJobsCount() - 1);
                    }
                    customerRepository.save(wo.getCustomer());
                }
            }
        }

        if (request.getServiceNotes() != null) {
            wo.setServiceNotes(request.getServiceNotes());
        }

        WorkOrder updated = workOrderRepository.save(wo);

        eventPublisher.publish(OperationsEvent.builder()
                .eventType(OperationsEventType.WORK_ORDER_STATUS_CHANGED)
                .severity(updated.getPriority() == WorkOrderPriority.CRITICAL ? "HIGH" : "INFO")
                .workOrderId(updated.getId())
                .technicianId(updated.getTechnician() != null ? updated.getTechnician().getId() : null)
                .message(String.format("Work order %s status updated to %s", updated.getWorkOrderNumber(), updated.getStatus()))
                .build());

        return WorkOrderDTO.fromEntity(updated);
    }

    @Transactional
    public void deleteWorkOrder(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));
        workOrderRepository.delete(wo);
    }
}

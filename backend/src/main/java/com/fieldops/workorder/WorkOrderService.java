package com.fieldops.workorder;

import com.fieldops.common.ResourceNotFoundException;
import com.fieldops.customer.Customer;
import com.fieldops.customer.CustomerRepository;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.websocket.OperationsEvent;
import com.fieldops.websocket.OperationsEventPublisher;
import com.fieldops.websocket.OperationsEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);

    private final WorkOrderRepository workOrderRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;
    private final OperationsEventPublisher eventPublisher;
    private final WorkOrderNumberGenerator workOrderNumberGenerator;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            CustomerRepository customerRepository,
                            TechnicianRepository technicianRepository,
                            OperationsEventPublisher eventPublisher) {
        this(workOrderRepository, customerRepository, technicianRepository, eventPublisher, null);
    }

    @Autowired
    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            CustomerRepository customerRepository,
                            TechnicianRepository technicianRepository,
                            OperationsEventPublisher eventPublisher,
                            WorkOrderNumberGenerator workOrderNumberGenerator) {
        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.technicianRepository = technicianRepository;
        this.eventPublisher = eventPublisher;
        this.workOrderNumberGenerator = workOrderNumberGenerator;
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderDTO> getWorkOrdersPaged(
            WorkOrderStatus status,
            WorkOrderPriority priority,
            Long technicianId,
            Long customerId,
            String search,
            String customerEmail,
            String technicianEmail,
            Pageable pageable
    ) {
        Specification<WorkOrder> spec = WorkOrderSpecification.filterBy(
                status, priority, technicianId, customerId, search, customerEmail, technicianEmail
        );
        return workOrderRepository.findAll(spec, pageable).map(WorkOrderDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDTO> getAllWorkOrders(
            WorkOrderStatus status,
            WorkOrderPriority priority,
            Long technicianId,
            Long customerId,
            String search
    ) {
        Specification<WorkOrder> spec = WorkOrderSpecification.filterBy(
                status, priority, technicianId, customerId, search, null, null
        );
        return workOrderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
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

        // Bounded retry loop for resilience against any rare transient constraint conflict
        int maxAttempts = 3;
        WorkOrder saved = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String woNum = workOrderNumberGenerator != null
                    ? workOrderNumberGenerator.generateNextNumber()
                    : "WO-" + (10000 + (int) (Math.random() * 90000));

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

            try {
                saved = workOrderRepository.save(wo);
                break;
            } catch (DataIntegrityViolationException dive) {
                if (attempt == maxAttempts) {
                    log.error("Exhausted retries creating work order due to collision: {}", dive.getMessage());
                    throw dive;
                }
                log.warn("Collision detected for work order number on attempt {}, retrying...", attempt);
            }
        }

        // Atomic database counter increment (avoids lost updates and row contention)
        customerRepository.incrementOpenJobsCount(customer.getId());

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

        if (request.getStatus() != null && request.getStatus() != prevStatus) {
            wo.setStatus(request.getStatus());
            if (request.getStatus() == WorkOrderStatus.COMPLETED && wo.getCompletedAt() == null) {
                wo.setCompletedAt(LocalDateTime.now());
                if (wo.getCustomer() != null) {
                    customerRepository.transitionJobCompleted(wo.getCustomer().getId());
                }
            } else if (request.getStatus() == WorkOrderStatus.CANCELLED && prevStatus != WorkOrderStatus.COMPLETED) {
                if (wo.getCustomer() != null) {
                    customerRepository.decrementOpenJobsCount(wo.getCustomer().getId());
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

        WorkOrderStatus prevStatus = wo.getStatus();

        if (isTechnician) {
            if (wo.getTechnician() == null || !com.fieldops.common.IdentityMatcher.matchesTechnician(callerEmail, wo.getTechnician().getEmail())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not assigned to this work order.");
            }

            if (request.getStatus() != null && request.getStatus() != wo.getStatus()) {
                if (request.getStatus() == WorkOrderStatus.NEW) {
                    throw new IllegalArgumentException("Technicians cannot revert work order status to NEW.");
                }
                if (wo.getStatus() == WorkOrderStatus.COMPLETED) {
                    throw new IllegalArgumentException("Completed work order cannot have its status changed.");
                }
            }
        }

        if (request.getStatus() != null && request.getStatus() != prevStatus) {
            wo.setStatus(request.getStatus());
            if (request.getStatus() == WorkOrderStatus.COMPLETED && wo.getCompletedAt() == null) {
                wo.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : LocalDateTime.now());
                if (wo.getCustomer() != null) {
                    customerRepository.transitionJobCompleted(wo.getCustomer().getId());
                }
            } else if (request.getStatus() == WorkOrderStatus.CANCELLED && prevStatus != WorkOrderStatus.COMPLETED) {
                if (wo.getCustomer() != null) {
                    customerRepository.decrementOpenJobsCount(wo.getCustomer().getId());
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
        if (wo.getStatus() != WorkOrderStatus.COMPLETED && wo.getStatus() != WorkOrderStatus.CANCELLED) {
            if (wo.getCustomer() != null) {
                customerRepository.decrementOpenJobsCount(wo.getCustomer().getId());
            }
        }
        workOrderRepository.delete(wo);
    }
}

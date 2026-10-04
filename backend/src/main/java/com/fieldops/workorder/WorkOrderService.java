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

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            CustomerRepository customerRepository,
                            TechnicianRepository technicianRepository) {
        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.technicianRepository = technicianRepository;
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
        return WorkOrderDTO.fromEntity(saved);
    }

    @Transactional
    public WorkOrderDTO updateWorkOrder(Long id, WorkOrderUpdateRequest request) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));

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
        return WorkOrderDTO.fromEntity(updated);
    }

    @Transactional
    public void deleteWorkOrder(Long id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));
        workOrderRepository.delete(wo);
    }
}

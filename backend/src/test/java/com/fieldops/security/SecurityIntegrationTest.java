package com.fieldops.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.customer.Customer;
import com.fieldops.customer.CustomerRepository;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.workorder.WorkOrder;
import com.fieldops.workorder.WorkOrderCreateRequest;
import com.fieldops.workorder.WorkOrderPriority;
import com.fieldops.workorder.WorkOrderRepository;
import com.fieldops.workorder.WorkOrderStatus;
import com.fieldops.workorder.WorkOrderStatusUpdateRequest;
import com.fieldops.workorder.WorkOrderUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint must return 401 or 403")
    void unauthenticatedRequest_ShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operations Intelligence: Dispatcher-only endpoints reject Customer role with 403")
    @WithMockUser(username = "customer@fieldops.demo", roles = {"CUSTOMER"})
    void operationsIntelligence_CustomerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/intelligence/overview"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/intelligence/anomalies"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/work-orders/1/accept-recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"technicianId\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operations Intelligence: Dispatcher-only endpoints reject Technician role with 403")
    @WithMockUser(username = "rahul.s@fieldops.demo", roles = {"TECHNICIAN"})
    void operationsIntelligence_TechnicianRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/intelligence/overview"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/intelligence/anomalies"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/work-orders/1/accept-recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"technicianId\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operations Intelligence: Dispatcher role can access intelligence overview")
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    void operationsIntelligence_DispatcherRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/intelligence/overview"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Technician Route Ownership: Technician A cannot view Technician B's route (returns 403)")
    @WithMockUser(username = "rahul.s@fieldops.demo", roles = {"TECHNICIAN"})
    void routeOptimization_TechnicianCannotViewOtherRoute() throws Exception {
        List<Technician> techs = technicianRepository.findAll();
        Technician techRahul = techs.stream().filter(t -> "rahul.s@fieldops.demo".equalsIgnoreCase(t.getEmail())).findFirst().orElseThrow();
        Technician techOther = techs.stream().filter(t -> !t.getId().equals(techRahul.getId())).findFirst().orElseThrow();

        // Accessing other technician's route must be forbidden
        mockMvc.perform(get("/api/technicians/" + techOther.getId() + "/optimized-route"))
                .andExpect(status().isForbidden());

        // Accessing other technician's summary must be forbidden
        mockMvc.perform(get("/api/technicians/" + techOther.getId() + "/route-summary"))
                .andExpect(status().isForbidden());

        // Accessing self route must be allowed
        mockMvc.perform(get("/api/technicians/" + techRahul.getId() + "/optimized-route"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Technician Route Ownership: Dispatcher can view any technician's route")
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    void routeOptimization_DispatcherCanViewAnyRoute() throws Exception {
        List<Technician> techs = technicianRepository.findAll();
        Technician tech = techs.get(0);

        mockMvc.perform(get("/api/technicians/" + tech.getId() + "/optimized-route"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Work Order Privilege Escalation: Technician cannot modify technicianId or priority via PUT (returns 403)")
    @WithMockUser(username = "rahul.s@fieldops.demo", roles = {"TECHNICIAN"})
    void workOrder_TechnicianCannotModifyRestrictedFields() throws Exception {
        // Find a work order assigned to Rahul
        List<WorkOrder> list = workOrderRepository.findAll();
        WorkOrder assignedWo = list.stream()
                .filter(wo -> wo.getTechnician() != null && "rahul.s@fieldops.demo".equalsIgnoreCase(wo.getTechnician().getEmail()))
                .findFirst().orElseThrow();

        // 1. Attempt to change priority
        WorkOrderUpdateRequest badReq1 = new WorkOrderUpdateRequest();
        badReq1.setPriority(WorkOrderPriority.LOW);

        mockMvc.perform(put("/api/work-orders/" + assignedWo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq1)))
                .andExpect(status().isForbidden());

        // 2. Attempt to reassign technician
        WorkOrderUpdateRequest badReq2 = new WorkOrderUpdateRequest();
        badReq2.setTechnicianId(999L);

        mockMvc.perform(put("/api/work-orders/" + assignedWo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq2)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Work Order Status Update: Assigned technician can update status and notes via PATCH")
    @WithMockUser(username = "rahul.s@fieldops.demo", roles = {"TECHNICIAN"})
    void workOrder_AssignedTechnicianCanUpdateStatus() throws Exception {
        List<WorkOrder> list = workOrderRepository.findAll();
        WorkOrder assignedWo = list.stream()
                .filter(wo -> wo.getTechnician() != null && "rahul.s@fieldops.demo".equalsIgnoreCase(wo.getTechnician().getEmail()))
                .filter(wo -> wo.getStatus() != WorkOrderStatus.COMPLETED)
                .findFirst().orElseThrow();

        WorkOrderStatusUpdateRequest statusReq = new WorkOrderStatusUpdateRequest(
                WorkOrderStatus.IN_PROGRESS,
                "On-site diagnostic initiated with calibrated multimeters",
                null
        );

        mockMvc.perform(patch("/api/work-orders/" + assignedWo.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Work Order Status Update: Unassigned technician cannot update status (returns 403)")
    @WithMockUser(username = "priya.v@fieldops.demo", roles = {"TECHNICIAN"})
    void workOrder_UnassignedTechnicianCannotUpdateStatus() throws Exception {
        // Find a work order assigned to Rahul, not Priya
        List<WorkOrder> list = workOrderRepository.findAll();
        WorkOrder rahulWo = list.stream()
                .filter(wo -> wo.getTechnician() != null && "rahul.s@fieldops.demo".equalsIgnoreCase(wo.getTechnician().getEmail()))
                .findFirst().orElseThrow();

        WorkOrderStatusUpdateRequest statusReq = new WorkOrderStatusUpdateRequest(
                WorkOrderStatus.IN_PROGRESS,
                "Unauthorized status update attempt",
                null
        );

        mockMvc.perform(patch("/api/work-orders/" + rahulWo.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer Profile Isolation: Customer cannot view other customer profiles (returns 403)")
    @WithMockUser(username = "dispatch@vertexlogistics.in", roles = {"CUSTOMER"})
    void customerProfile_CustomerCannotViewOtherCustomer() throws Exception {
        List<Customer> customers = customerRepository.findAll();
        Customer custA = customers.stream().filter(c -> "dispatch@vertexlogistics.in".equalsIgnoreCase(c.getEmail())).findFirst().orElseThrow();
        Customer custB = customers.stream().filter(c -> !c.getId().equals(custA.getId())).findFirst().orElseThrow();

        // Customer A accessing Customer B must return 403
        mockMvc.perform(get("/api/customers/" + custB.getId()))
                .andExpect(status().isForbidden());

        // Customer A accessing Customer A must return 200
        mockMvc.perform(get("/api/customers/" + custA.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cross-Role Isolation: Customer cannot access technician profiles directly (returns 403)")
    @WithMockUser(username = "customer@fieldops.demo", roles = {"CUSTOMER"})
    void crossRole_CustomerCannotViewTechnicianProfile() throws Exception {
        List<Technician> techs = technicianRepository.findAll();
        Technician tech = techs.get(0);

        mockMvc.perform(get("/api/technicians/" + tech.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Predictive SLA Ownership: Technician cannot view predictive SLA for unassigned ticket (returns 403)")
    @WithMockUser(username = "priya.v@fieldops.demo", roles = {"TECHNICIAN"})
    void predictiveSla_TechnicianCannotViewUnassignedTicket() throws Exception {
        // Find work order assigned to Rahul, not Priya
        List<WorkOrder> list = workOrderRepository.findAll();
        WorkOrder rahulWo = list.stream()
                .filter(wo -> wo.getTechnician() != null && "rahul.s@fieldops.demo".equalsIgnoreCase(wo.getTechnician().getEmail()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/sla/predictive/" + rahulWo.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-6302: Technician self-access succeeds with technician@fieldops.demo demo account")
    @WithMockUser(username = "technician@fieldops.demo", roles = {"TECHNICIAN"})
    void technician_SelfAccess_WithDemoEmail_ShouldSucceed() throws Exception {
        Technician rahul = technicianRepository.findAll().stream()
                .filter(t -> "rahul.s@fieldops.demo".equalsIgnoreCase(t.getEmail()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/technicians/" + rahul.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("rahul.s@fieldops.demo"));
    }

    @Test
    @DisplayName("SEC-6302: Technician self-access succeeds with rahul.s@fieldops.demo primary account")
    @WithMockUser(username = "rahul.s@fieldops.demo", roles = {"TECHNICIAN"})
    void technician_SelfAccess_WithPrimaryEmail_ShouldSucceed() throws Exception {
        Technician rahul = technicianRepository.findAll().stream()
                .filter(t -> "rahul.s@fieldops.demo".equalsIgnoreCase(t.getEmail()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/technicians/" + rahul.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("rahul.s@fieldops.demo"));
    }

    @Test
    @DisplayName("SEC-6302: Technician assigned work order status update succeeds with technician@fieldops.demo")
    @WithMockUser(username = "technician@fieldops.demo", roles = {"TECHNICIAN"})
    void workOrder_StatusUpdate_WithDemoEmail_ShouldSucceed() throws Exception {
        WorkOrder rahulWo = workOrderRepository.findAll().stream()
                .filter(wo -> wo.getTechnician() != null && "rahul.s@fieldops.demo".equalsIgnoreCase(wo.getTechnician().getEmail()))
                .findFirst().orElseThrow();

        WorkOrderStatusUpdateRequest req = new WorkOrderStatusUpdateRequest(
                WorkOrderStatus.IN_PROGRESS,
                "En route to facility for priority diagnosis",
                null
        );

        mockMvc.perform(patch("/api/work-orders/" + rahulWo.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("SEC-6302: Customer self-access succeeds with customer@fieldops.demo demo account")
    @WithMockUser(username = "customer@fieldops.demo", roles = {"CUSTOMER"})
    void customer_SelfAccess_WithDemoEmail_ShouldSucceed() throws Exception {
        Customer vertex = customerRepository.findAll().stream()
                .filter(c -> "dispatch@vertexlogistics.in".equalsIgnoreCase(c.getEmail()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/customers/" + vertex.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("P1 Defect: Malformed request payload returns 400 Bad Request with sanitized message")
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    void malformedPayload_ShouldReturn400BadRequest() throws Exception {
        // Send invalid JSON body
        mockMvc.perform(post("/api/simulator/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenarioType\": \"INVALID_NONEXISTENT_TYPE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Malformed request payload or invalid field format."));
    }

    @Test
    @DisplayName("P1 Defect: Unmapped resource URL returns 404 Not Found")
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    void unmappedEndpoint_ShouldReturn404NotFound() throws Exception {
        mockMvc.perform(get("/api/nonexistent/unmapped/path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Gap 1 (P1): Customer can create work order for self without technician self-assignment")
    @WithMockUser(username = "customer@fieldops.demo", roles = {"CUSTOMER"})
    void customer_CanCreateWorkOrder_ForSelf_ShouldSucceed() throws Exception {
        Customer vertex = customerRepository.findAll().stream()
                .filter(c -> "dispatch@vertexlogistics.in".equalsIgnoreCase(c.getEmail()))
                .findFirst().orElseThrow();

        WorkOrderCreateRequest req = new WorkOrderCreateRequest();
        req.setTitle("Customer Self-Service Emergency Cooling Repair");
        req.setDescription("Compressor unit 3 at cold storage bay 4 is tripping safety breakers.");
        req.setPriority(WorkOrderPriority.HIGH);
        req.setCustomerId(vertex.getId());
        req.setTechnicianId(999L); // Malicious/invalid self-assignment attempt

        mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Customer Self-Service Emergency Cooling Repair"))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andExpect(jsonPath("$.data.technician").doesNotExist());
    }

    @Test
    @DisplayName("Gap 1 (P1): Customer creating work order for another customer is rejected with 403")
    @WithMockUser(username = "customer@fieldops.demo", roles = {"CUSTOMER"})
    void customer_CreateWorkOrder_ForOtherCustomer_ShouldBeForbidden() throws Exception {
        Customer otherCustomer = customerRepository.findAll().stream()
                .filter(c -> !"dispatch@vertexlogistics.in".equalsIgnoreCase(c.getEmail()))
                .findFirst().orElseThrow();

        WorkOrderCreateRequest badReq = new WorkOrderCreateRequest();
        badReq.setTitle("Unauthorized Cross-Customer Job Creation");
        badReq.setPriority(WorkOrderPriority.MEDIUM);
        badReq.setCustomerId(otherCustomer.getId());

        mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Gap 2 (P2): Technician performance scorecard succeeds with demo alias technician@fieldops.demo")
    @WithMockUser(username = "technician@fieldops.demo", roles = {"TECHNICIAN"})
    void technician_PerformanceScorecard_WithDemoEmail_ShouldSucceed() throws Exception {
        Technician rahul = technicianRepository.findAll().stream()
                .filter(t -> "rahul.s@fieldops.demo".equalsIgnoreCase(t.getEmail()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/technicians/" + rahul.getId() + "/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianId").value(rahul.getId()))
                .andExpect(jsonPath("$.email").value("rahul.s@fieldops.demo"));
    }

    @Test
    @DisplayName("Gap 2 (P2): Technician accessing another technician's performance scorecard returns 403")
    @WithMockUser(username = "technician@fieldops.demo", roles = {"TECHNICIAN"})
    void technician_PerformanceScorecard_OtherTechnician_ShouldBeForbidden() throws Exception {
        Technician otherTech = technicianRepository.findAll().stream()
                .filter(t -> !"rahul.s@fieldops.demo".equalsIgnoreCase(t.getEmail()))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/technicians/" + otherTech.getId() + "/performance"))
                .andExpect(status().isForbidden());
    }
}

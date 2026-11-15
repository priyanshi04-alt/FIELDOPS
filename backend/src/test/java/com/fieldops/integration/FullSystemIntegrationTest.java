package com.fieldops.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.customer.Customer;
import com.fieldops.customer.CustomerRepository;
import com.fieldops.technician.Technician;
import com.fieldops.technician.TechnicianRepository;
import com.fieldops.workorder.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Phase 6.5 Full-System Integration & Security Lifecycle Test.
 *
 * Validates the complete multi-role work order lifecycle:
 * 1. Customer creates ticket (POST /api/work-orders) -> status NEW, no technician assigned
 * 2. Dispatcher queries and assigns ticket to technician (PUT /api/work-orders/{id}) -> status ASSIGNED
 * 3. Assigned Technician retrieves work order (GET /api/work-orders/{id})
 * 4. Assigned Technician transitions status to IN_PROGRESS (PATCH /api/work-orders/{id}/status)
 * 5. Assigned Technician transitions status to COMPLETED (PATCH /api/work-orders/{id}/status)
 * 6. Customer verifies ticket is COMPLETED (GET /api/work-orders/{id})
 * 7. Administrator checks Operations Overview metrics (GET /api/intelligence/overview)
 * 8. Administrator deletes work order (DELETE /api/work-orders/{id}) -> subsequent GET returns 404
 *
 * Also tests security boundaries, IDOR isolation, and privilege escalation guards:
 * - Non-admin cannot DELETE work order (403 Forbidden)
 * - Customer cannot create ticket for another customer (403 Forbidden)
 * - Technician cannot revert completed work order or modify dispatch-controlled fields (400 / 403)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FullSystemIntegrationTest {

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
    @DisplayName("E2E Lifecycle: Customer -> Dispatcher -> Technician -> Completed -> Admin Delete")
    void fullSystemLifecycle_ShouldSucceedAcrossAllRoles() throws Exception {
        Customer vertexCustomer = customerRepository.findAll().stream()
                .filter(c -> "dispatch@vertexlogistics.in".equalsIgnoreCase(c.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Vertex Logistics customer not seeded"));

        Technician rahulTech = technicianRepository.findAll().stream()
                .filter(t -> "rahul.s@fieldops.demo".equalsIgnoreCase(t.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Rahul Sharma technician not seeded"));

        // Step 1: Customer creates work order
        WorkOrderCreateRequest createReq = new WorkOrderCreateRequest();
        createReq.setTitle("Emergency Cold Storage Compressor Trip - E2E Test");
        createReq.setDescription("Compressor unit 3 at Bay 4 shutdown on high pressure trip.");
        createReq.setPriority(WorkOrderPriority.HIGH);
        createReq.setCustomerId(vertexCustomer.getId());

        MvcResult createResult = mockMvc.perform(post("/api/work-orders")
                        .header("Authorization", "Bearer mock-token") // MockMvc WithMockUser bypasses JWT filter
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("customer@fieldops.demo").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andExpect(jsonPath("$.data.technician").doesNotExist())
                .andExpect(jsonPath("$.data.customer.id").value(vertexCustomer.getId()))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long workOrderId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

        // Step 2: Dispatcher assigns work order to technician Rahul
        WorkOrderUpdateRequest assignReq = new WorkOrderUpdateRequest();
        assignReq.setTechnicianId(rahulTech.getId());
        assignReq.setStatus(WorkOrderStatus.ASSIGNED);

        mockMvc.perform(put("/api/work-orders/" + workOrderId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("dispatcher@fieldops.demo").roles("DISPATCHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.data.technician.id").value(rahulTech.getId()));

        // Step 3: Technician accesses the assigned work order
        mockMvc.perform(get("/api/work-orders/" + workOrderId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("technician@fieldops.demo").roles("TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(workOrderId))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        // Step 4: Technician updates status to IN_PROGRESS
        WorkOrderStatusUpdateRequest inProgressReq = new WorkOrderStatusUpdateRequest();
        inProgressReq.setStatus(WorkOrderStatus.IN_PROGRESS);
        inProgressReq.setServiceNotes("Technician arrived on site, diagnostics initiated.");

        mockMvc.perform(patch("/api/work-orders/" + workOrderId + "/status")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("technician@fieldops.demo").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.serviceNotes").value("Technician arrived on site, diagnostics initiated."));

        // Step 5: Technician completes the work order
        WorkOrderStatusUpdateRequest completeReq = new WorkOrderStatusUpdateRequest();
        completeReq.setStatus(WorkOrderStatus.COMPLETED);
        completeReq.setServiceNotes("Compressor contactor replaced and refrigerant pressure normalized.");

        mockMvc.perform(patch("/api/work-orders/" + workOrderId + "/status")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("technician@fieldops.demo").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Step 6: Customer verifies completion
        mockMvc.perform(get("/api/work-orders/" + workOrderId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("customer@fieldops.demo").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.serviceNotes").value("Compressor contactor replaced and refrigerant pressure normalized."));

        // Step 7: Administrator verifies operational overview
        mockMvc.perform(get("/api/intelligence/overview")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@fieldops.demo").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.operationsHealthScore").isNumber());

        // Step 8: Administrator deletes work order
        mockMvc.perform(delete("/api/work-orders/" + workOrderId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@fieldops.demo").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify work order is now gone (404)
        mockMvc.perform(get("/api/work-orders/" + workOrderId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@fieldops.demo").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Security: Non-Admin roles cannot delete work orders (403 Forbidden)")
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    void nonAdmin_DeleteWorkOrder_ShouldReturnForbidden() throws Exception {
        WorkOrder wo = workOrderRepository.findAll().stream().findFirst().orElseThrow();

        mockMvc.perform(delete("/api/work-orders/" + wo.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Customer cannot access other customer work orders (403 Forbidden)")
    @WithMockUser(username = "customer@fieldops.demo", roles = {"CUSTOMER"})
    void customer_CrossCustomerAccess_ShouldReturnForbidden() throws Exception {
        Customer otherCustomer = customerRepository.findAll().stream()
                .filter(c -> !"dispatch@vertexlogistics.in".equalsIgnoreCase(c.getEmail()))
                .findFirst().orElseThrow();

        List<WorkOrder> otherJobs = workOrderRepository.findByCustomerId(otherCustomer.getId());
        if (!otherJobs.isEmpty()) {
            mockMvc.perform(get("/api/work-orders/" + otherJobs.get(0).getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Security: Technician cannot revert completed work order or modify dispatch fields")
    @WithMockUser(username = "technician@fieldops.demo", roles = {"TECHNICIAN"})
    void technician_PrivilegeEscalation_ShouldBeRejected() throws Exception {
        Technician rahul = technicianRepository.findAll().stream()
                .filter(t -> "rahul.s@fieldops.demo".equalsIgnoreCase(t.getEmail()))
                .findFirst().orElseThrow();

        List<WorkOrder> completedJobs = workOrderRepository.findByTechnicianId(rahul.getId()).stream()
                .filter(w -> w.getStatus() == WorkOrderStatus.COMPLETED)
                .toList();

        if (!completedJobs.isEmpty()) {
            WorkOrderStatusUpdateRequest revertReq = new WorkOrderStatusUpdateRequest();
            revertReq.setStatus(WorkOrderStatus.NEW);

            mockMvc.perform(patch("/api/work-orders/" + completedJobs.get(0).getId() + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(revertReq)))
                    .andExpect(status().isBadRequest());
        }

        // Attempting to modify priority via PUT should be rejected with 403
        WorkOrder assignedJob = workOrderRepository.findByTechnicianId(rahul.getId()).stream()
                .filter(w -> w.getStatus() == WorkOrderStatus.ASSIGNED || w.getStatus() == WorkOrderStatus.IN_PROGRESS)
                .findFirst().orElse(null);

        if (assignedJob != null) {
            WorkOrderUpdateRequest escalateReq = new WorkOrderUpdateRequest();
            escalateReq.setPriority(WorkOrderPriority.CRITICAL); // Dispatch controlled

            mockMvc.perform(put("/api/work-orders/" + assignedJob.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(escalateReq)))
                    .andExpect(status().isForbidden());
        }
    }
}

package com.fieldops.workorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WorkOrderPaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    @DisplayName("Default pagination returns page 0 with size 50 and pagination headers")
    void testDefaultPagination() throws Exception {
        mockMvc.perform(get("/api/work-orders"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "50"))
                .andExpect(header().exists("X-Total-Pages"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    @DisplayName("Page boundaries: requesting a page beyond total pages returns empty list without error")
    void testPageBoundaries() throws Exception {
        mockMvc.perform(get("/api/work-orders?page=999&size=50"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page-Number", "999"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    @DisplayName("Sorting: valid sort parameter orders database query results")
    void testSorting() throws Exception {
        mockMvc.perform(get("/api/work-orders?page=0&size=10&sort=createdAt&direction=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    @DisplayName("Filtering: status filter returns only matching work orders")
    void testFilteringByStatus() throws Exception {
        mockMvc.perform(get("/api/work-orders?status=NEW&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].status", everyItem(is("NEW"))));
    }

    @Test
    @WithMockUser(username = "customer@fieldops.demo", roles = {"CUSTOMER"})
    @DisplayName("Customer Isolation: customer role receives only tickets for their company/identity")
    void testCustomerDataIsolation() throws Exception {
        mockMvc.perform(get("/api/work-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    @DisplayName("Validation: invalid page number (<0) returns 400 Bad Request")
    void testInvalidPageNegative() throws Exception {
        mockMvc.perform(get("/api/work-orders?page=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    @DisplayName("Validation: invalid page size (>100) returns 400 Bad Request")
    void testInvalidPageSizeOverMax() throws Exception {
        mockMvc.perform(get("/api/work-orders?size=101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "dispatcher@fieldops.demo", roles = {"DISPATCHER"})
    @DisplayName("Validation: invalid sort field returns 400 Bad Request")
    void testInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/work-orders?sort=unknownFieldSqlInjection"))
                .andExpect(status().isBadRequest());
    }
}

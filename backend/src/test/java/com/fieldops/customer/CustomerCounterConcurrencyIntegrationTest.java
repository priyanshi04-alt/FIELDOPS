package com.fieldops.customer;

import com.fieldops.workorder.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CustomerCounterConcurrencyIntegrationTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Concurrent work order creation and completion maintains strict counter consistency")
    void testConcurrentCustomerCounterConsistency() throws InterruptedException {
        Customer customer = customerRepository.findAll().stream().findFirst().orElseThrow();
        Long customerId = customer.getId();
        int initialOpen = customer.getOpenJobsCount() != null ? customer.getOpenJobsCount() : 0;
        int initialCompleted = customer.getCompletedJobsCount() != null ? customer.getCompletedJobsCount() : 0;

        int totalJobs = 20;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch createLatch = new CountDownLatch(totalJobs);

        List<WorkOrderDTO> createdOrders = new ArrayList<>();
        Object lock = new Object();

        for (int i = 0; i < totalJobs; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    WorkOrderCreateRequest req = new WorkOrderCreateRequest();
                    req.setTitle("Counter Job #" + index);
                    req.setPriority(WorkOrderPriority.LOW);
                    req.setCustomerId(customerId);

                    WorkOrderDTO wo = workOrderService.createWorkOrder(req);
                    synchronized (lock) {
                        createdOrders.add(wo);
                    }
                } finally {
                    createLatch.countDown();
                }
            });
        }

        assertTrue(createLatch.await(20, TimeUnit.SECONDS));

        Customer afterCreation = customerRepository.findById(customerId).orElseThrow();
        assertEquals(initialOpen + totalJobs, afterCreation.getOpenJobsCount(),
                "Customer openJobsCount must equal initial + totalJobs");

        // Concurrently complete half of the jobs
        int jobsToComplete = totalJobs / 2;
        CountDownLatch completeLatch = new CountDownLatch(jobsToComplete);

        for (int i = 0; i < jobsToComplete; i++) {
            final Long woId = createdOrders.get(i).getId();
            executor.submit(() -> {
                try {
                    WorkOrderStatusUpdateRequest req = new WorkOrderStatusUpdateRequest();
                    req.setStatus(WorkOrderStatus.COMPLETED);
                    workOrderService.updateWorkOrderStatus(woId, req, "dispatcher@fieldops.demo", false);
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        assertTrue(completeLatch.await(20, TimeUnit.SECONDS));
        executor.shutdown();

        Customer afterCompletion = customerRepository.findById(customerId).orElseThrow();
        assertEquals(initialOpen + (totalJobs - jobsToComplete), afterCompletion.getOpenJobsCount(),
                "openJobsCount must correctly decrement after completion");
        assertEquals(initialCompleted + jobsToComplete, afterCompletion.getCompletedJobsCount(),
                "completedJobsCount must correctly increment after completion");
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    @DisplayName("Counter floor check: decrementing openJobsCount at 0 does not produce negative values")
    void testNonNegativeFloor() {
        Customer customer = customerRepository.findAll().stream().findFirst().orElseThrow();
        Long customerId = customer.getId();

        // Repeated decrements
        for (int i = 0; i < 50; i++) {
            customerRepository.decrementOpenJobsCount(customerId);
        }

        Customer updated = customerRepository.findById(customerId).orElseThrow();
        assertTrue(updated.getOpenJobsCount() >= 0, "openJobsCount must never be negative");
    }
}

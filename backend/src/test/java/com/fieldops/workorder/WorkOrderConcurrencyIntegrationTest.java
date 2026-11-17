package com.fieldops.workorder;

import com.fieldops.customer.Customer;
import com.fieldops.customer.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WorkOrderConcurrencyIntegrationTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Test
    @DisplayName("Concurrent creation of 40 work orders across 10 threads produces unique numbers without collision")
    void testConcurrentWorkOrderCreationUniqueness() throws InterruptedException {
        Customer customer = customerRepository.findAll().stream().findFirst().orElseThrow();
        Long customerId = customer.getId();
        int initialOpenJobs = customer.getOpenJobsCount() != null ? customer.getOpenJobsCount() : 0;

        int threadCount = 10;
        int ordersPerThread = 4;
        int totalOrders = threadCount * ordersPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(totalOrders);

        Set<String> generatedNumbers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < totalOrders; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    WorkOrderCreateRequest req = new WorkOrderCreateRequest();
                    req.setTitle("Concurrent Order #" + index);
                    req.setDescription("Resilience concurrency test");
                    req.setPriority(WorkOrderPriority.MEDIUM);
                    req.setCustomerId(customerId);

                    WorkOrderDTO created = workOrderService.createWorkOrder(req);
                    assertNotNull(created);
                    assertNotNull(created.getWorkOrderNumber());
                    assertTrue(created.getWorkOrderNumber().startsWith("WO-"));

                    generatedNumbers.add(created.getWorkOrderNumber());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();
        boolean completed = finishLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Concurrency test timed out");
        assertEquals(0, errorCount.get(), "No errors expected during concurrent work order creation");
        assertEquals(totalOrders, successCount.get(), "All work orders must succeed");
        assertEquals(totalOrders, generatedNumbers.size(), "All generated work order numbers must be strictly unique");

        // Verify customer open jobs counter increased by exactly totalOrders
        Customer updatedCustomer = customerRepository.findById(customerId).orElseThrow();
        assertEquals(initialOpenJobs + totalOrders, updatedCustomer.getOpenJobsCount(),
                "Customer openJobsCount must match exactly without lost updates");
    }
}

package com.fieldops.workorder;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class WorkOrderNumberGenerator {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderNumberGenerator.class);

    private final JdbcTemplate jdbcTemplate;
    private final AtomicLong fallbackSequence = new AtomicLong(10000);
    private boolean sequenceInitialized = false;

    public WorkOrderNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            // PostgreSQL and H2 compatible DDL
            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS work_order_number_seq START WITH 10000 INCREMENT BY 1");
            sequenceInitialized = true;
            log.info("Database sequence 'work_order_number_seq' initialized successfully.");
        } catch (Exception e) {
            log.warn("Database sequence creation deferred or unsupported (using in-memory atomic fallback): {}", e.getMessage());
        }
    }

    public String generateNextNumber() {
        if (sequenceInitialized) {
            try {
                Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('work_order_number_seq')", Long.class);
                if (nextVal != null) {
                    return "WO-" + nextVal;
                }
            } catch (Exception e) {
                log.warn("Failed to retrieve nextval from sequence, using atomic fallback: {}", e.getMessage());
            }
        }
        return "WO-" + fallbackSequence.incrementAndGet();
    }
}

package com.fieldops.integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("pgtest")
@EnabledIfSystemProperty(named = "fieldops.pg.it.enabled", matches = "true")
public class FlywayMigrationPostgreSqlIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verify Flyway migration baseline executes and validates against PostgreSQL")
    void testFlywayMigrationExecution() {
        assertNotNull(flyway, "Flyway bean must be configured");

        MigrationInfo[] appliedMigrations = flyway.info().applied();
        assertTrue(appliedMigrations.length >= 1, "At least baseline migration must be recorded");

        // Verify flyway schema history table exists
        Integer historyCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = true", Integer.class);
        assertNotNull(historyCount);
        assertTrue(historyCount >= 1, "Flyway schema history must record successful migrations");

        // Verify sequence exists
        Long nextval = jdbcTemplate.queryForObject("SELECT nextval('work_order_number_seq')", Long.class);
        assertNotNull(nextval);
        assertTrue(nextval >= 10000);
    }
}

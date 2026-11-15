# FIELDOPS Platform — Changelog

All notable changes to the FIELDOPS platform across phases will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0-RC1] - 2026-11-15

### Phase 6.6: Release Candidate Qualification, Performance & Production Hardening

#### Added
- **Browser E2E Automation:** Integrated Playwright framework (`@playwright/test`) with complete test coverage across Admin, Dispatcher, Technician, and Customer user journeys.
- **Dedicated Performance Test Database:** Automated isolation environment (`fieldops_perf_test_db`) preserving operational data in `fieldops_db` during load testing.
- **Hardened Performance Test Suite:** Added 10-warmup request cycle, inclusive quantile percentile calculation, and semantic JSON response assertions.
- **Safety Gate Controls:** Implemented automatic abortion thresholds (error rate $> 5\%$, p95 latency $> 5,000\text{ ms}$, HikariCP pending $> 5$).
- **Theme Persistence Verification:** Validated White Mode & Dark Mode toggling and `localStorage` state persistence via automated browser tests.

#### Fixed
- **PostgreSQL 17 Integration:** Validated live PostgreSQL 17 server on port 5432 with zero side-effect alert generation inside read-only transactions.
- **MockMaker Subclass in JDK 25:** Resolved ByteBuddy dynamic agent attachment hang on Windows named pipes via `mock-maker-subclass` and `<forkCount>0</forkCount>`.
- **Technician Identity Matching:** Applied `IdentityMatcher.matchesTechnician` across `TechnicianPerformanceController` and routing controllers, resolving canonical alias mismatches.

#### Changed
- Standardized Java compilation and Docker runtime targets on **Java 17 LTS** (Spring Boot 3.3.4 compatibility).
- Documented known high-concurrency collision constraint in `WorkOrderService` (`Math.random() * 9000`) with mitigation roadmap for 50+ VU scale.

---

## [0.9.5] - 2026-11-14

### Phase 6.5: Full-System Integration, Security Validation & E2E Testing
- Added `FullSystemIntegrationTest.java` exercising complete 8-step operational lifecycle.
- Hardened customer work order creation (`POST /api/work-orders`) with strict server-side customer ID resolution and technician ID stripping.
- Blocked IDOR cross-customer ticket submission with HTTP 403 Forbidden.
- Multi-tenant single-provider boundary documented in architecture runbooks.

---

## [0.9.4] - 2026-11-13

### Phase 6.4: Containerization, Staging Deployment & Observability
- Authored multi-stage `backend/Dockerfile` and `frontend/Dockerfile` using non-root users.
- Created `docker-compose.staging.yml` integrating PostgreSQL 17, Spring Boot, React/Vite, and Prometheus.
- Published `DEPLOYMENT_RUNBOOK.md`, `OBSERVABILITY_RUNBOOK.md`, and `STAGING_SETUP.md`.

---

## [0.9.3] - 2026-11-12

### Phase 6.3.1 - 6.3.2: Reliability Remediation & Safe Baseline Recovery
- Remediated SQLState 25006 read-only transaction alert persistence bug (`SEC-6301`).
- Remediated technician email identity mismatch in `DataInitializer` (`SEC-6302`).
- Hardened Jackson `HttpMessageNotReadableException` handling with HTTP 400 Bad Request.

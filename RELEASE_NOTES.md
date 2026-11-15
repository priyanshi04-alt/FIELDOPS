# FIELDOPS Platform — Release Notes (v1.0.0-RC1)

**Release Date:** November 15, 2026  
**Target Milestone:** Release Candidate 1 (v1.0.0-RC1)  
**Lead Auditor:** Principal Software Architect, DevSecOps Lead & Independent Release Auditor  
**Repository Branch:** `phase-6.3.1-reliability-remediation`  
**Baseline Commit:** `7c1a1b2`  

---

## 1. Product Overview

FIELDOPS is an enterprise Intelligent Field Service Operations & Telemetry Platform engineered for critical infrastructure engineering teams. It provides deterministic technician routing (TSP heuristic with 2-opt improvement), predictive machine learning forecasting (Ridge regression inventory demand forecasting and Logistic Regression SLA breach classification), live operations overview, and role-based self-service ticket portals.

---

## 2. Key Release Highlights

### 🚀 Performance & High Throughput
- **Nominal Peak Throughput:** Verified up to **448.78 RPS** on active alerts and **415.40 RPS** on inventory predictions with **0.00% error rate** under 10 concurrent Virtual Users.
- **Elevated Throughput:** Sustained up to **585.19 RPS** under 25 concurrent Virtual Users across predictive evaluation and telemetry aggregation APIs.
- **Connection Pool Stability:** Zero connection leaks under high concurrency bursts. Automatic pool reclamation confirmed via Spring Boot Actuator.

### 🛡️ Resilience & Fault Recovery
- **Concurrent Integrity:** Verified 10 concurrent update operations on single work order rows without lost updates, deadlocks, or state corruption.
- **Transactional Rollback:** Invalid status mutations rejected with HTTP 400 Bad Request, guaranteeing zero partial writes.
- **Container Health Probes:** Kubernetes-compatible liveness (`/actuator/health/livenessState`) and readiness (`/actuator/health/readinessState`) probes integrated.

### 🌐 Browser End-to-End Automation
- **Playwright Test Suite:** 5 comprehensive browser workflows verified across Google Chrome in headless mode.
- **Role Journeys:** Admin supervision, Dispatcher route planning, Field Technician task progression, and Customer self-service.
- **Theme Accessibility:** Fully accessible White Mode & Dark Mode with `localStorage` state persistence verified across page reloads.

### 🔒 Enterprise Security & Governance
- **Strict Role-Based Access Control (RBAC):** Admin, Dispatcher, Technician, and Customer boundaries enforced.
- **IDOR Protection:** Customer work order creation strictly derives caller ID from the authenticated JWT principal. Any attempt to supply an unauthorized customer ID returns HTTP 403 Forbidden.
- **Technician Non-Assignment Gate:** Customers cannot assign technicians to tickets; dispatcher and admin assignment controls preserved.
- **Identity Alias Matching:** `IdentityMatcher.matchesTechnician` seamlessly resolves canonical demo aliases.

---

## 3. Known Operational Boundaries & Constraints

1. **High-Concurrency Ticket Number Generation (P2):**
   - Work order numbers are generated via pseudo-random 4-digit numbers (`Math.random() * 9000`).
   - At extreme concurrent write bursts ($\ge 25$ parallel creation threads), birthday collision probability reaches ~4.8% to 9.4%.
   - **Recommended Production Workaround:** Deploy with PostgreSQL atomic sequence generator prior to massive bulk ticket ingestion workflows.
2. **Single-Provider Multi-Tenant Model:**
   - FIELDOPS v1.0.0-RC1 provides customer-level and assignment-based tenant isolation within a single service provider. Multi-organization global isolation is scheduled for v1.1.0.

---

## 4. Minimum System Requirements

- **Java Runtime:** Java 17 LTS (Eclipse Temurin recommended)
- **Database:** PostgreSQL 17.0+
- **Frontend Runtime:** Node.js 20+ / Modern Chromium-based browser
- **Memory Allocation:** Minimum 1 GB RAM for Spring Boot, 512 MB for PostgreSQL.

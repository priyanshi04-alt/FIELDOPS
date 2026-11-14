# FIELDOPS — Staging Environment Setup Guide

**Target Audience**: Developers, QA Engineers, Release Managers  
**Purpose**: Provision a fully isolated, production-parity staging environment using Docker Compose.

---

## 1. Staging Environment Architecture

The FIELDOPS staging environment runs identical container images to production, but operates with:
- Staging resource limits (`docker-compose.staging.yml`)
- Staging Spring Boot profile (`SPRING_PROFILES_ACTIVE=staging`)
- Isolated bridge network (`fieldops-network`)
- Dedicated data volume (`fieldops_pgdata`)

---

## 2. Quickstart Instructions

### Step 1: Clone and Configure
```bash
git clone https://github.com/priyanshi04-alt/FIELDOPS.git
cd FIELDOPS
git checkout phase-6.3.1-reliability-remediation

# Generate local staging configuration
cp .env.example .env
```

### Step 2: Launch Staging Cluster
```bash
# Validate configurations
docker-compose -f docker-compose.yml -f docker-compose.staging.yml config

# Start all staging containers in background
docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d --build
```

### Step 3: Verify All Containers Are Healthy
```bash
docker-compose ps
```
All three containers (`fieldops-postgres`, `fieldops-backend`, `fieldops-frontend`) must report status `Up (healthy)`.

---

## 3. End-to-End Smoke Test Suite

Once the staging cluster is running, execute this smoke test sequence:

### 1. Authentication Smoke Tests

Test authentication endpoints with seeded demo personas:

```bash
# A. Admin / Dispatcher Login
curl -s -X POST http://localhost:5173/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dispatcher@fieldops.demo","password":"FieldOps@2026!"}' | jq .
# Expected: {"success":true,"token":"...","role":"DISPATCHER"}

# B. Technician Login
curl -s -X POST http://localhost:5173/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"technician@fieldops.demo","password":"FieldOps@2026!"}' | jq .
# Expected: {"success":true,"token":"...","role":"TECHNICIAN"}

# C. Customer Login
curl -s -X POST http://localhost:5173/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@fieldops.demo","password":"FieldOps@2026!"}' | jq .
# Expected: {"success":true,"token":"...","role":"CUSTOMER"}

# D. Invalid Credentials Rejection
curl -s -X POST http://localhost:5173/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dispatcher@fieldops.demo","password":"WrongPassword"}' | jq .
# Expected: HTTP 401 Unauthorized
```

### 2. Operational Workflows Smoke Tests

Using the dispatcher token `$TOKEN`:

```bash
# A. Operations Intelligence Overview (SEC-6301 Read-Only Safety)
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:5173/api/intelligence/overview | jq .

# B. Predictive SLA Breach Evaluation
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:5173/api/sla/predictive | jq .

# C. Multi-Stop Route Optimization Summary
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:5173/api/technicians/1/route-summary | jq .

# D. Active Operational Alerts
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:5173/api/alerts | jq .
```

---

## 4. Failure & Recovery Verification

Test resilience under failure scenarios:

### Scenario 1: Backend Container Restart
```bash
docker restart fieldops-backend
# Wait 15 seconds
curl -s http://localhost:8082/actuator/health | jq .status
# Expected: "UP"
```

### Scenario 2: Database Outage & Recovery
```bash
# Pause PostgreSQL
docker pause fieldops-postgres
# Backend liveness/readiness will report DOWN
curl -s http://localhost:8082/actuator/health/readiness | jq .status
# Expected: "OUT_OF_SERVICE" or "DOWN"

# Resume PostgreSQL
docker unpause fieldops-postgres
# Wait 10 seconds for HikariCP to reconnect
curl -s http://localhost:8082/actuator/health/readiness | jq .status
# Expected: "UP" (Automatic reconnection without restart)
```

### Scenario 3: Database Data Persistence Verification
```bash
# Stop containers
docker-compose down

# Restart containers
docker-compose up -d

# Verify data intact (user count remains > 0)
docker exec -it fieldops-postgres psql -U postgres -d fieldops_db -c "SELECT count(*) FROM users;"
# Expected: Count matches seeded users
```

---

## 5. Teardown
```bash
# Stop staging cluster while preserving volume
docker-compose -f docker-compose.yml -f docker-compose.staging.yml down
```

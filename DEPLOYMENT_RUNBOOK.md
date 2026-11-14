# FIELDOPS — Deployment Runbook

**Audience**: DevOps Engineers, Site Reliability Engineers, System Administrators  
**System**: FIELDOPS Field Service & Operations Intelligence Platform  
**Target Environment**: Docker Compose / Linux / Staging / Production Container Runtime

---

## 1. System Architecture Overview

FIELDOPS runs as a three-tier micro-service container architecture:

```
[ Browser / Client ]
        │
        ▼ (Port 5173 / HTTP)
┌──────────────────────────────────────────────┐
│  fieldops-frontend (Nginx Alpine)            │
│  - SPA static assets (/usr/share/nginx/html) │
│  - REST API reverse proxy (/api/ -> backend) │
│  - STOMP WebSocket proxy (/ws -> backend)    │
└──────────────────────┬───────────────────────┘
                       │ (internal network: fieldops-network)
                       ▼ (Port 8082 / HTTP & WS)
┌──────────────────────────────────────────────┐
│  fieldops-backend (Spring Boot 3.3.4 / JRE)  │
│  - Non-root user: fieldops (UID 10001)       │
│  - Observability: Actuator (/actuator/health)│
│  - Embedded ML Inference Engine              │
└──────────────────────┬───────────────────────┘
                       │ (internal network: fieldops-network)
                       ▼ (Port 5432 / TCP)
┌──────────────────────────────────────────────┐
│  fieldops-postgres (PostgreSQL 17 Alpine)    │
│  - Persistent data volume: fieldops_pgdata   │
│  - Database: fieldops_db                     │
└──────────────────────────────────────────────┘
```

---

## 2. Prerequisites & Environment Setup

### Prerequisites
- Docker Engine >= 20.10.0
- Docker Compose >= 2.20.0
- Ports available: `5173` (Frontend HTTP), `8082` (Backend API), `5432` (PostgreSQL)

### Initial Setup
1. Clone the repository and navigate to the project root:
   ```bash
   cd /path/to/FIELDOPS
   ```
2. Create environment file from template:
   ```bash
   cp .env.example .env
   ```
3. Edit `.env` to supply production/staging secrets:
   ```bash
   # Minimum required overrides:
   POSTGRES_PASSWORD=YourStrongDatabasePasswordHere
   JWT_SECRET=Your64CharacterHexHmacSha256SecretHere
   ```

---

## 3. Deployment Commands

### Standard Deployment (Single Command)
```bash
# Validate Compose configuration syntax
docker-compose config

# Build and start all services in detached mode
docker-compose up -d --build
```

### Staging Deployment (With Staging Resource Limits & Profile)
```bash
docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d --build
```

### Checking Service Status
```bash
docker-compose ps
```
Expected output:
```
NAME                IMAGE                COMMAND                  SERVICE    CREATED         STATUS                   PORTS
fieldops-backend    fieldops-backend     "sh -c 'exec java $J…"   backend    1 minute ago    Up 1 minute (healthy)    0.0.0.0:8082->8082/tcp
fieldops-frontend   fieldops-frontend    "/docker-entrypoint.…"   frontend   1 minute ago    Up 1 minute (healthy)    0.0.0.0:5173->80/tcp
fieldops-postgres   postgres:17-alpine   "docker-entrypoint.s…"   postgres   1 minute ago    Up 1 minute (healthy)    0.0.0.0:5432->5432/tcp
```

---

## 4. Health & Readiness Verification

Run the following curl commands to verify deployment health:

```bash
# 1. Verify Frontend Nginx Health
curl -I http://localhost:5173/healthz
# Expected: HTTP/1.1 200 OK

# 2. Verify Backend Actuator Overall Health
curl -s http://localhost:8082/actuator/health | jq .
# Expected: {"status":"UP", "components": {...}}

# 3. Verify Container Liveness & Readiness Probes
curl -s http://localhost:8082/actuator/health/liveness | jq .
curl -s http://localhost:8082/actuator/health/readiness | jq .
# Expected: {"status":"UP"}

# 4. Verify Model Artifacts Health
curl -s http://localhost:8082/actuator/health | jq .components.modelArtifacts
# Expected: {"status":"UP","details":{"inventoryForecastModel":"LOADED","slaBreachModel":"LOADED"}}

# 5. Verify PostgreSQL Container Readiness
docker exec fieldops-postgres pg_isready -U postgres -d fieldops_db
# Expected: /var/run/postgresql:5432 - accepting connections
```

---

## 5. Log Inspection & Troubleshooting

```bash
# Follow logs across all containers
docker-compose logs -f

# Follow backend logs specifically
docker-compose logs -f backend

# Search for errors or warnings with request correlation IDs
docker-compose logs backend | grep "ERROR"
```

---

## 6. Zero-Downtime Graceful Shutdown & Maintenance

To stop services safely while allowing in-flight requests to complete:
```bash
# Graceful stop (Spring Boot drains connections within 20s-30s)
docker-compose stop

# Complete teardown (preserves database volume)
docker-compose down

# CAUTION: Never pass '-v' unless completely purging data!
# docker-compose down -v  <-- WILL DELETE POSTGRES DATA VOLUME
```

---

## 7. Rollback Procedure

If a deployed image fails health checks:
```bash
# 1. Roll back to previous Git commit
git checkout <PREVIOUS_STABLE_COMMIT>

# 2. Rebuild and restart containers
docker-compose up -d --build --force-recreate
```

# FIELDOPS — Observability & Monitoring Runbook

**Audience**: DevOps Engineers, SREs, Security Operations Center (SOC)  
**System**: FIELDOPS Spring Boot 3.3.4 Actuator & Structured Logging

---

## 1. Observability Architecture

FIELDOPS provides comprehensive operational visibility through Spring Boot Actuator, Micrometer runtime metrics, custom health indicators, and MDC request-correlated logging.

```
┌─────────────────────────────────────────────────────────────┐
│                    OBSERVABILITY STACK                      │
├──────────────────────────────┬──────────────────────────────┤
│ Metrics & Health (Actuator)  │ Structured Logging (MDC)     │
│ - /actuator/health           │ - %X{requestId} correlation  │
│ - /actuator/health/liveness  │ - ISO-8601 Timestamps        │
│ - /actuator/health/readiness │ - Masked PII & Credentials   │
│ - /actuator/metrics          │ - Auth failure audits        │
│ - Model Artifacts Status     │ - SQL exception alerts       │
└──────────────────────────────┴──────────────────────────────┘
```

---

## 2. Health Endpoints & Probes

| Endpoint | Access | Purpose | Normal Response |
| :--- | :--- | :--- | :--- |
| `/actuator/health` | Public / Container | Aggregate system health status | `{"status":"UP", ...}` |
| `/actuator/health/liveness` | Public / Container | Container liveness check | `{"status":"UP"}` |
| `/actuator/health/readiness` | Public / Container | Service traffic readiness check | `{"status":"UP"}` |
| `/actuator/info` | Public | Application metadata & Git info | `{"app":{"name":"fieldops-backend"}}` |
| `/actuator/metrics` | Authenticated (JWT) | Metric index of all tracked counters | List of metric names |
| `/healthz` | Public (Nginx) | Frontend reverse proxy web server check | `200 OK` (`text/plain`) |

### Custom Component: Model Artifacts Indicator
Inspect the status of the machine learning inference models:
```bash
curl -s http://localhost:8082/actuator/health | jq .components.modelArtifacts
```
Output:
```json
{
  "status": "UP",
  "details": {
    "inventoryForecastModel": "LOADED",
    "slaBreachModel": "LOADED"
  }
}
```

---

## 3. Key Metrics to Monitor

Metrics are queryable via `/actuator/metrics/{metric.name}` with Authorization header:

### A. JVM & Memory Metrics
- **Heap Memory Used**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/jvm.memory.used?tag=area:heap
  ```
- **GC Pause Time**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/jvm.gc.pause
  ```
- **Thread Count**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/jvm.threads.live
  ```

### B. Database Connection Pool Metrics (HikariCP)
- **Active Connections**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/hikaricp.connections.active
  ```
- **Pending/Awaiting Connections**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/hikaricp.connections.pending
  ```
- **Idle Connections**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/hikaricp.connections.idle
  ```

### C. HTTP Request Throughput & Latency
- **Request Count & Latency**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/http.server.requests
  ```
- **Error Count (5xx)**:
  ```bash
  curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/actuator/metrics/http.server.requests?tag=status:500
  ```

---

## 4. Structured Logging & Correlation IDs

### Log Pattern
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{requestId}] %-5level %logger{36} - %msg%n
```

### Traceability Example
Each incoming HTTP request generates a unique UUID `requestId` via `MdcCorrelationFilter`:
```
2026-11-14 22:15:30.123 [http-nio-8082-exec-1] [f8e4a902-3c21-4f1a-b28e-5c4d6e9f1a23] INFO  c.f.a.JwtAuthenticationFilter - Authenticated user rahul.s@fieldops.demo with roles [ROLE_TECHNICIAN]
2026-11-14 22:15:30.145 [http-nio-8082-exec-1] [f8e4a902-3c21-4f1a-b28e-5c4d6e9f1a23] INFO  c.f.t.TechnicianController - Retrieved self route summary for technicianId=1
```
The client also receives `X-Request-ID: f8e4a902-3c21-4f1a-b28e-5c4d6e9f1a23` in HTTP response headers.

### Security & Sanitization Rules
- **No Password Exposure**: Password hashes and raw passwords are never printed.
- **JWT Sanitization**: Malformed or expired JWTs log claims errors without printing signatures.
- **SQL Sanitization**: Hibernate `show-sql` is disabled in production/staging to avoid printing sensitive query parameters.

---

## 5. Incident Response & Triage Guide

### Issue 1: High Connection Pool Latency (`hikaricp.connections.pending > 0`)
1. Inspect running database transactions:
   ```bash
   docker exec -it fieldops-postgres psql -U postgres -d fieldops_db -c "SELECT pid, now() - query_start AS duration, query FROM pg_stat_activity WHERE state != 'idle';"
   ```
2. Check for long-running unindexed queries or stuck transactions.

### Issue 2: Liveness Probe Failure (`/actuator/health/liveness != UP`)
1. Check backend JVM memory usage for OutOfMemoryError.
2. Review container restart exit code: `docker inspect fieldops-backend --format '{{.State.ExitCode}}'`.

### Issue 3: STOMP WebSocket Connection Drops
1. Check Nginx proxy timeouts (`proxy_read_timeout 86400s;`).
2. Verify token expiration time (`app.jwt.expiration-ms`).

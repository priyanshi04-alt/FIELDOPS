# FIELDOPS — PHASE 3 MASTER ARCHITECTURE & IMPLEMENTATION SPECIFICATION
========================================================================

**Platform:** FieldOps — Real-Time Multi-Job Field Operations Optimization Platform  
**Baseline Status:** Phase 1 & Phase 2 baselines preserved, verified, and active.  
**Phase 3 Completion Date:** October 2026  
**Operating Environment:** Spring Boot 3.3.4 (Java 25) + React 18 / TypeScript / Vite + PostgreSQL 17  

---

## 1. Executive Summary & Transformation Overview

FieldOps has evolved from **"Single-Work-Order Decision Support"** into a **"Real-Time Multi-Job Field Operations Optimization Platform"**.

### Phase 3 Major Capabilities Delivered:
1. **Multi-Stop Route Optimization Engine (`com.fieldops.routing.multistop`)**:
   - Computes sequential multi-stop travel itineraries from technician current origin across assigned and pending work orders ($Technician \to Stop_1 \to Stop_2 \to Stop_3 \to \dots$).
   - Deterministic algorithm combining **Nearest Neighbor Heuristic**, **Dynamic SLA Penalty Weighting**, and **2-Opt Edge-Exchange Local Search**.
   - Explainable scoring formula exposing exact trade-offs (distance cost, travel time, SLA risk penalties, and priority weighting).
   - Human-in-the-loop governance: Dispatchers must review and explicitly click **"Apply Route"** before execution sequences are persisted.
   - Comprehensive audit logging in `route_decision_audits` capturing dispatcher identity, timestamp, before/after routes, metric deltas, and rationale.

2. **Spring WebSocket & STOMP Operational Event Pipeline (`com.fieldops.websocket`)**:
   - Real-time STOMP message broker configured on `/ws` with subscriptions to `/topic/operations` and `/topic/alerts`.
   - Domain-level decoupled publishing via `OperationsEventPublisher` triggered directly within service transactions (Work Orders, Technicians, Assignments, Alerts, and Multi-Stop Routes).
   - Frontend zero-dependency native WebSocket/STOMP manager (`operationsSocket.ts`) and `useOperationsSocket()` hook with automatic exponential reconnection, visual connection state (`● LIVE`, `○ RECONNECTING`, `○ OFFLINE`), and smart React Query cache invalidation without page refreshes.

3. **Deterministic Operational Alert Engine (`com.fieldops.alert`)**:
   - Structured alerts for 8 distinct operational conditions: `SLA_CRITICAL`, `SLA_AT_RISK`, `TECHNICIAN_OVERLOAD`, `UNASSIGNED_CRITICAL_JOB`, `ROUTE_DELAY`, `INVENTORY_SHORTAGE`, `TERRITORY_SPIKE`, and `TECHNICIAN_OFFLINE`.
   - Complete alert lifecycle: `OPEN` $\to$ `ACKNOWLEDGED` $\to$ `RESOLVED` with user tracking and resolution notes.
   - Dedicated interactive **Alert Center** (`/app/alerts`) with filtering, bulk operations, and rapid triage modals.

---

## 2. Multi-Stop Route Optimization Architecture

### 2.1 Heuristic Formulation
The optimization engine solves the Traveling Salesperson Problem with Time Windows (TSPTW) variant using a two-stage deterministic pipeline:

$$\text{Route Cost} = w_{\text{dist}} \cdot \text{Distance}_{\text{km}} + w_{\text{time}} \cdot \text{TravelTime}_{\text{min}} + \sum_{\text{stops}} \text{Penalty}_{\text{SLA}} + \sum_{\text{stops}} \text{Penalty}_{\text{Priority}}$$

- **Stage 1 (SLA-Weighted Nearest Neighbor)**:
  From current coordinate $P_0$, iteratively select the unvisited stop $P_i$ that minimizes:
  $$C(P_{\text{curr}}, P_i) = \text{HaversineDistance}(P_{\text{curr}}, P_i) + \lambda_{\text{SLA}} \cdot \max(0, \text{ETA}(P_i) - \text{Deadline}(P_i)) + \mu_{\text{prio}} \cdot (4 - \text{PriorityLevel}(P_i))$$
- **Stage 2 (2-Opt Edge-Exchange Improvement)**:
  For every pair of non-adjacent edges $(i, i+1)$ and $(j, j+1)$ where $j > i+1$, reverse sequence segment $i+1 \dots j$. If the candidate swap lowers total route cost without introducing new SLA violations, adopt the swap.
- **Stage 3 (Metric Aggregation & Explainability)**:
  Computes total distance, travel minutes, service duration minutes, baseline comparison (distance saved and minutes saved), and generates a plain-English explainability narrative.

### 2.2 Domain Entities & DTOs
- `RouteStop`: Order ID, title, priority, status, lat/lon, SLA deadline, remaining minutes, service duration, sequence number, ETA, departure time, leg travel time, and SLA status (`ON_TRACK`, `AT_RISK`, `VIOLATION`).
- `RouteLeg`: Source/destination coordinates, leg number, distance (km), and travel minutes.
- `OptimizedRoute`: Aggregated route metrics, stop array, leg array, baseline comparison, score, and explanation.
- `RouteDecisionAudit` (`route_decision_audits`): Database entity recording every route calculation and dispatch approval action.

### 2.3 REST Endpoints
| HTTP Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/technicians/{id}/optimized-route` | Calculates and returns the optimized multi-stop route. |
| `POST` | `/api/technicians/{id}/optimize-route` | Triggers a fresh re-calculation with custom constraints. |
| `POST` | `/api/technicians/{id}/apply-route` | Dispatcher explicitly approves and applies the itinerary. |
| `GET` | `/api/technicians/{id}/route-summary` | Returns a compact summary of stops, time saved, and SLA status. |
| `GET` | `/api/technicians/{id}/route-audit` | Retrieves auditable decision log history for the technician. |

---

## 3. Real-Time Operations WebSocket Architecture

### 3.1 Broker Configuration (`WebSocketConfig.java`)
- Endpoint: `/ws` (with fallback SockJS / raw WebSocket at `/ws/websocket`).
- Permitted in Spring Security: `/ws/**` explicitly permitted in `SecurityConfig.java`.
- Broker Destination Prefix: `/topic` (broadcasts to `/topic/operations` and `/topic/alerts`).
- Application Destination Prefix: `/app`.

### 3.2 Supported Operations Events (`OperationsEventType`)
- `WORK_ORDER_ASSIGNED`: Work order assigned to technician.
- `WORK_ORDER_STATUS_CHANGED`: Status transitioned (e.g. `IN_PROGRESS`, `COMPLETED`).
- `TECHNICIAN_STATUS_CHANGED`: Shift status updated (`AVAILABLE`, `ON_ROUTE`, `BUSY`, `OFFLINE`).
- `TECHNICIAN_LOCATION_UPDATED`: GPS coordinate ping received.
- `SLA_RISK_CHANGED`: SLA breach probability crossed threshold.
- `NEW_ALERT`: Operational alert raised by deterministic rules.
- `ALERT_ACKNOWLEDGED`: Alert acknowledged by dispatcher.
- `ALERT_RESOLVED`: Alert resolved with audit notes.
- `ROUTE_OPTIMIZED`: Multi-stop itinerary calculated.
- `ROUTE_APPLIED`: Multi-stop itinerary approved by dispatcher.
- `INVENTORY_ALERT`: Stock depletion below reorder point.
- `ANOMALY_DETECTED`: Fleet telemetry anomaly flagged.

### 3.3 Event Delivery Flow
$$\text{Service State Change} \longrightarrow \text{OperationsEventPublisher} \longrightarrow \text{STOMP Broker } (/topic) \longrightarrow \text{useOperationsSocket} \longrightarrow \text{React Query Invalidation}$$

---

## 4. Operational Alert Engine Architecture

### 4.1 Alert Structure (`OperationalAlert.java` & `alert_records`)
- Attributes: `id`, `alertType`, `severity`, `status`, `title`, `message`, `explanation`, `relatedEntityType`, `relatedEntityId`, `workOrderId`, `technicianId`, `acknowledgedBy`, `acknowledgedAt`, `resolvedBy`, `resolvedAt`, `resolutionNotes`, `createdAt`.
- Severity Tiers: `INFO`, `WARNING`, `HIGH`, `CRITICAL`.
- Status Lifecycle: `OPEN` $\to$ `ACKNOWLEDGED` $\to$ `RESOLVED`.

### 4.2 Deterministic Trigger Matrix
1. **`SLA_CRITICAL`**: Work order remaining SLA $\le 30$ minutes or overdue ($< 0$ minutes).
2. **`SLA_AT_RISK`**: Work order SLA breach probability $\ge 60\%$.
3. **`TECHNICIAN_OVERLOAD`**: Technician assigned $\ge 4$ active unfinished work orders.
4. **`UNASSIGNED_CRITICAL_JOB`**: Work order with `CRITICAL` priority in `NEW` status without an assigned technician.
5. **`ROUTE_DELAY`**: Multi-stop itinerary projected completion time exceeds SLA deadline.
6. **`INVENTORY_SHORTAGE`**: Part available quantity $\le$ safety reorder threshold.
7. **`TERRITORY_SPIKE`**: Regional cluster density anomaly identified by anomaly engine.
8. **`TECHNICIAN_OFFLINE`**: Technician marked `OFFLINE` while having open assigned work orders.

### 4.3 REST Endpoints
| HTTP Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/api/alerts` | Paginated and filtered alert listing (severity, status, type). |
| `GET` | `/api/alerts/active` | All open or acknowledged alerts. |
| `GET` | `/api/alerts/{id}` | Alert details with explanation and entity links. |
| `POST` | `/api/alerts/{id}/acknowledge` | Mark alert acknowledged with dispatcher email. |
| `POST` | `/api/alerts/{id}/resolve` | Resolve alert with resolution notes. |
| `POST` | `/api/alerts/scan` | Trigger immediate deterministic rule evaluation scan. |

---

## 5. Frontend Visual Hierarchy & Component Integration

1. **Top Bar Header (`AppLayout.tsx`)**:
   - NOC Stream live status indicator pill: `● LIVE` (pulsing green), `○ RECONNECTING` (pulsing amber), `○ OFFLINE` (slate).
   - Dynamic `Alerts` navigation link with real-time active alert count badge.
2. **Operations Overview (`OverviewPage.tsx`)**:
   - `RouteOptimizationPanel` embedded above the map: Technician selector, stops preview, 2-opt comparison metrics, "Apply Route" button, and audit drawer.
   - `LiveOperationsMap`: Upgraded Leaflet map rendering multi-stop route vectors, numbered stop badges ($1, 2, 3 \dots$), technician origin pin, and popups displaying SLA countdowns and sequence numbers.
3. **Operations Intelligence Center (`IntelligenceCenter.tsx`)**:
   - 5-Tier operational tabs: **Active Alerts**, **SLA Risks**, **Smart Dispatch**, **Multi-Stop Routes**, and **Anomalies**.
   - Grid Health score indicator ($96\%$).
4. **Dedicated Alert Center (`AlertsPage.tsx`)**:
   - Full-page alert management with filter pills, severity styling, telemetry scan button, and triage modal for acknowledging/resolving.

---

## 6. Verification & Test Evidence

- **Backend Tests (`./mvnw test`)**:
  - `AlertServiceUnitTest`: 3 tests passed.
  - `OperationsIntelligenceUnitTest`: 4 tests passed.
  - `RouteOptimizationServiceTest`: 6 tests passed.
  - **Total:** 13/13 passing tests (0 failures, 0 errors).
- **Frontend Production Build (`npm run build`)**:
  - TypeScript compiler (`tsc --noEmit`): 0 errors.
  - Vite v5.4.21 bundle: 2,487 modules transformed, `dist/` built successfully.
- **REST Endpoints Verified via Live Server**:
  - Authentication `/api/auth/login`: `200 OK` (token issued).
  - Multi-Stop Route `/api/technicians/34/optimized-route`: `200 OK` (3 stops, 300.7 km, explanation generated).
  - Human-in-the-Loop Apply Route `/api/technicians/34/apply-route`: `200 OK` (`isApplied: true`, audit record #1 logged).
  - Decision Audit `/api/technicians/34/route-audit`: `200 OK` (recorded with before/after sequence).
  - Operational Alerts `/api/alerts/active`: `200 OK` (30 active alerts generated).
  - Alert Lifecycle `/api/alerts/30/acknowledge` & `/resolve`: `200 OK` (`ACKNOWLEDGED` $\to$ `RESOLVED`).

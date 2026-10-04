# FIELDOPS — Intelligent Field Service Operations Platform

[![SaaS Architecture](https://img.shields.io/badge/Architecture-Enterprise%20SaaS-indigo)](https://github.com)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.3-green)](https://spring.io)
[![React](https://img.shields.io/badge/Frontend-React%2018%20%2B%20TypeScript-blue)](https://react.dev)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%2017-blue)](https://www.postgresql.org)
[![TailwindCSS](https://img.shields.io/badge/UI-Tailwind%20%2B%20Leaflet-cyan)](https://tailwindcss.com)

> *"Run every field operation with clarity."*

**FIELDOPS** is a production-grade, portfolio-caliber SaaS platform engineered for field service operations teams. Inspired by the high-density visual craft of Linear and the operational telemetry of Stripe Dashboard, FieldOps coordinates technicians, live work order states, contractual SLA breach guardrails, GPS tracking, and parts replenishment across municipal territories.

---

## 🌟 Key Highlights & Architectural Features

### 1. Operations Command Center (Dashboard)
- **Intelligence Center**: Integrated real-time engine featuring:
  - **SLA Risks**: Real dynamic breach probability calculations (0–100) with contributing factor breakdown.
  - **Smart Dispatch**: 100-point candidate match scores with 1-click review and assignment.
  - **Operational Anomalies**: Real-time detection of technician overloading (+100%), territory volume spikes (>38%), and stockouts.
- **Live Operations Map (Leaflet & OpenStreetMap)**: Interactive telemetry rendering real-time engineer GPS beacons, critical tickets, and active route vectors (dashed vectors connecting dispatched technician GPS coordinates to customer facilities with transit ETA telemetry).
- **5-Stage Work Order Pipeline**: Visual Kanban tracking `NEW` → `ASSIGNED` → `IN_PROGRESS` → `AWAITING_VERIFICATION` → `COMPLETED`.
- **Technician Utilization Chart**: Recharts-powered visualization of field workforce load and active job distribution.
- **Real-Time Activity Feed**: Audit trail of dispatches, acceptances, and inventory adjustments.

### 2. Full Operations Workflow Suite
- **Work Orders**: Enterprise grid with multi-filter faceted search (status, severity, territorial node), sorting, pagination, and a modal for instant ticket creation.
- **Work Order Detail**: Upgraded with Phase 2 Intelligent Decision Suite:
  - **Smart Dispatch Panel**: Candidate rankings with explainable 100-point score cards (Skills 35%, Distance 25%, Availability 15%, Workload 10%, SLA Feasibility 10%, Performance 5%). Supports 1-click Accept and Dispatcher Rejection with reason tracking.
  - **SLA Risk Intelligence Card**: Dynamic breach probability with multi-factor impact badges.
  - **Route & Transit Telemetry Card**: Great-Circle Haversine calculations, urban transit ETA, and SLA feasibility status.
  - **Decision & Audit History**: Chronological timeline of system recommendations and dispatcher actions.
- **Technician Management**: Roster view with rating metrics, skill taxonomy tags, on-duty status toggle, and technician performance drawer.
- **Field Scheduling (Day/Week)**: Time-block timeline (08:00 to 18:00) showing work order allocations per engineer.
- **Customer CRM**: Accounts table with service location coordinates, active ticket counts, and account details.
- **Spare Parts & Inventory**: Central depot warehouse telemetry with minimum reorder thresholds, low stock alerts, and unit adjustment dialogue.
- **Executive Analytics**: Daily volume throughput curves, SLA compliance rates, regional capacity distribution, and engineer leaderboards.
- **Platform Configuration (Settings)**: Fine-tune SLA breach warning horizons, auto-dispatch proximity radii, overtime dispatch policies, and API keys.

---

## 🔑 Demo Personas & Instant Credentials

The platform is pre-seeded with 4 realistic personas and a **1-click Quick Persona Switcher** on the login screen:

| Role | Demo Email | Password | Responsibilities |
|---|---|---|---|
| **Global Admin** | `admin@fieldops.demo` | `FieldOps@2026!` | Full platform administration & settings |
| **Dispatcher** | `dispatcher@fieldops.demo` | `FieldOps@2026!` | Live ops telemetry, routing, & ticket dispatch |
| **Field Technician** | `technician@fieldops.demo` | `FieldOps@2026!` | On-site work execution & service notes |
| **Enterprise Customer** | `customer@fieldops.demo` | `FieldOps@2026!` | Ticket requests & status tracking |

---

## 🚀 Quickstart & Local Development

### Prerequisites
- **Java 17+**
- **Node.js 18+ & npm**
- **PostgreSQL 14+** (or Docker)

### 1. Database Setup
Create database `fieldops_db` in PostgreSQL:
```sql
CREATE DATABASE fieldops_db;
```

### 2. Run Backend (Spring Boot 3.3.4)
```powershell
cd backend
# Using Maven Wrapper (Java 17 / 21 / 25 compatible)
./mvnw clean spring-boot:run
```
*Backend runs on `http://localhost:8082` with complete demo data auto-seeded upon boot (`DataInitializer.java`).*

### 3. Run Frontend (Vite + React + Tailwind)
```powershell
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173` with automatic reverse proxy for `/api` pointing to `http://localhost:8082`.*

---

## 🐳 Docker Compose (Full-Stack Deployment)

To deploy PostgreSQL, the Spring Boot API, and the Nginx-served React application with one command:

```powershell
docker-compose up --build -d
```

- **Frontend App**: `http://localhost:5173`
- **Backend REST API**: `http://localhost:8082/api`
- **PostgreSQL**: `localhost:5432`

---

## 📐 Project Directory Structure

```
fieldops/
├── docker-compose.yml             # Container orchestration
├── README.md                      # Documentation & quickstart
│
├── backend/                       # Spring Boot 3.3.4 (Java 17)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/fieldops/
│       ├── auth/                  # JWT auth, Login, Register, Filters
│       ├── user/                  # User entity & repositories
│       ├── technician/            # Field technician entity & service
│       ├── customer/              # Customer entity & management
│       ├── workorder/             # 5-stage lifecycle work orders & notes
│       ├── inventory/             # Spare parts stock & threshold tracking
│       ├── analytics/             # Telemetry aggregation & reporting
│       ├── notification/          # Real-time alert notifications
│       ├── dashboard/             # Aggregated operational KPIs
│       └── common/                # DataInitializer (50+ seed records)
│
└── frontend/                      # React 18 + Vite + TypeScript
    ├── Dockerfile
    ├── nginx.conf
    ├── index.html
    ├── package.json
    ├── tailwind.config.js
    └── src/
        ├── components/
        │   ├── common/            # Logo, NotificationPopover
        │   └── map/               # LiveOperationsMap (Leaflet)
        ├── features/auth/         # AuthContext & ProtectedRoute
        ├── layouts/               # AppLayout (Sidebar, Topbar)
        ├── pages/                 # Landing, Overview, Work Orders, Technicians,
        │                          # Schedule, Customers, Inventory, Analytics,
        │                          # Settings, Login, SignUp
        ├── services/              # Typed REST client with bearer interceptor
        └── types/                 # Domain interfaces & DTO schemas
```

---

## 🛡️ API Endpoints Summary

- `POST /api/auth/login` — Authenticate and retrieve JWT token
- `POST /api/auth/register` — Create new organization user
- `GET  /api/auth/me` — Retrieve active session profile
- `GET  /api/dashboard` — High-level dashboard counters, map telemetry & pipeline
- `GET  /api/work-orders` — Filtered work orders list
- `GET  /api/work-orders/{id}` — Work order detailed operational record
- `POST /api/work-orders` — Dispatch new work order
- `PUT  /api/work-orders/{id}` — Update status or lifecycle
- `GET  /api/technicians` — Technician roster and GPS coordinates
- `PATCH /api/technicians/{id}/status` — Toggle engineer availability
- `GET  /api/customers` — Enterprise client directory
- `GET  /api/inventory` — Depot inventory catalog
- `PUT  /api/inventory/{id}` — Adjust stock counts
- `GET  /api/analytics` — Historical throughput & CSAT metrics
- `GET  /api/notifications` — Notification alerts
- `POST /api/notifications/mark-all-read` — Mark all alerts as acknowledged

---
*Crafted for production-grade field operations.*

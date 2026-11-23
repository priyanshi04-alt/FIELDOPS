# FIELDOPS — Live Cloud Deployment & Resume Presentation Guide

This guide details the live deployment architecture and turnkey 1-click cloud hosting setup for **FIELDOPS**.

---

## 1. Resume Presentation Links

For your resume, CV, and portfolio, use the following standardized links:

- **Source Code (GitHub):** [https://github.com/priyanshi04-alt/FIELDOPS](https://github.com/priyanshi04-alt/FIELDOPS)
- **Live Demo Application (Web):** `https://fieldops-frontend.onrender.com` (or `https://fieldops.vercel.app`)
- **Backend API & Swagger / Health:** `https://fieldops-backend.onrender.com/actuator/health`

### Pre-Seeded Instant Demo Credentials
The platform includes built-in test personas pre-seeded in the database:

| Role | Email | Password | Responsibilities |
|---|---|---|---|
| **Global Admin** | `admin@fieldops.demo` | `FieldOps@2026!` | Platform administration, full system control |
| **Dispatcher** | `dispatcher@fieldops.demo` | `FieldOps@2026!` | Operations intelligence, routing, dispatch |
| **Technician** | `arun@fieldops.demo` | `FieldOps@2026!` | Mobile job orders, route execution |
| **Customer** | `contact@acme.test` | `FieldOps@2026!` | Customer ticket submission & tracking |

---

## 2. Option A: 1-Click Turnkey Deploy via Render Blueprint (Recommended)

Render provides free hosting for PostgreSQL databases, Spring Boot Docker containers, and React static sites. A complete Infrastructure-as-Code Blueprint ([`render.yaml`](file:///d:/FIELDOPS/render.yaml)) is included in this repository.

### Quick Setup Steps (Takes ~3 minutes):
1. Push your latest branch / code to GitHub:
   ```bash
   git push origin <your-branch>
   ```
2. Log in to [dashboard.render.com](https://dashboard.render.com/) (Sign in with your GitHub account).
3. Click **New +** → **Blueprint**.
4. Connect your repository: `priyanshi04-alt/FIELDOPS`.
5. Render will detect [`render.yaml`](file:///d:/FIELDOPS/render.yaml) and automatically create:
   - **`fieldops-db`**: Managed PostgreSQL database.
   - **`fieldops-backend`**: Spring Boot 3.3.4 container (`https://fieldops-backend.onrender.com`).
   - **`fieldops-frontend`**: React 18 + Vite frontend with automatic `/api/*` proxy rewrite (`https://fieldops-frontend.onrender.com`).
6. Click **Apply**.
7. Once deployed, open `https://fieldops-frontend.onrender.com` to see the live app!

---

## 3. Option B: Vercel Frontend + Render Backend

If you prefer using Vercel for instant frontend CDN hosting:
1. Deploy Backend + Database on Render (using steps above).
2. Go to [vercel.com/new](https://vercel.com/new) and import `priyanshi04-alt/FIELDOPS`.
3. Set **Root Directory** to `frontend`.
4. Vercel will automatically read [`frontend/vercel.json`](file:///d:/FIELDOPS/frontend/vercel.json) to route API calls directly to your live backend.
5. Click **Deploy**. Your frontend is live at `https://fieldops.vercel.app`!

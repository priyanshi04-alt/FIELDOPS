import {
  ApiResponse,
  User,
  DashboardOverview,
  WorkOrder,
  Technician,
  Customer,
  InventoryItem,
  AnalyticsOverview,
  NotificationItem,
  OperationsIntelligenceOverview,
  OperationalAnomaly,
  AssignmentRecommendation,
  SlaRiskResult,
  RouteEstimate,
  DecisionAuditLog,
  OptimizedRoute,
  RouteDecisionAudit,
  OperationalAlert,
  InventoryForecastDTO,
  PredictiveSlaResultDTO,
  TechnicianPerformanceDTO,
  ScenarioInput,
  ScenarioResult,
  ScenarioDecisionAudit,
  Role,
} from '@/types';
import {
  MOCK_USERS,
  MOCK_CUSTOMERS,
  MOCK_TECHNICIANS,
  MOCK_WORK_ORDERS,
  MOCK_INVENTORY,
  MOCK_ALERTS,
  MOCK_NOTIFICATIONS,
  MOCK_DASHBOARD_OVERVIEW,
  MOCK_ANALYTICS,
  MOCK_INTELLIGENCE,
} from './mockData';

const API_BASE = '/api';

export function getAuthToken(): string | null {
  return localStorage.getItem('fieldops_token');
}

export function setAuthToken(token: string | null) {
  if (token) {
    localStorage.setItem('fieldops_token', token);
  } else {
    localStorage.removeItem('fieldops_token');
  }
}

function getMockFallback<T>(endpoint: string, options: RequestInit = {}): T {
  const method = (options.method || 'GET').toUpperCase();

  if (endpoint.startsWith('/dashboard')) {
    return MOCK_DASHBOARD_OVERVIEW as unknown as T;
  }
  if (endpoint.startsWith('/work-orders')) {
    if (endpoint.includes('/sla-at-risk')) {
      return [MOCK_WORK_ORDERS[0]] as unknown as T;
    }
    const idMatch = endpoint.match(/\/work-orders\/(\d+)/);
    if (idMatch) {
      const found = MOCK_WORK_ORDERS.find((w) => w.id === Number(idMatch[1]));
      return (found || MOCK_WORK_ORDERS[0]) as unknown as T;
    }
    if (method === 'POST') {
      const newWO: WorkOrder = {
        ...MOCK_WORK_ORDERS[0],
        id: Math.floor(Math.random() * 9000) + 1000,
        workOrderNumber: `WO-2026-${Math.floor(Math.random() * 900) + 100}`,
        status: 'NEW',
        createdAt: new Date().toISOString(),
      };
      return newWO as unknown as T;
    }
    return MOCK_WORK_ORDERS as unknown as T;
  }
  if (endpoint.startsWith('/technicians')) {
    const idMatch = endpoint.match(/\/technicians\/(\d+)/);
    if (idMatch) {
      const found = MOCK_TECHNICIANS.find((t) => t.id === Number(idMatch[1]));
      return (found || MOCK_TECHNICIANS[0]) as unknown as T;
    }
    return MOCK_TECHNICIANS as unknown as T;
  }
  if (endpoint.startsWith('/customers')) {
    const idMatch = endpoint.match(/\/customers\/(\d+)/);
    if (idMatch) {
      const found = MOCK_CUSTOMERS.find((c) => c.id === Number(idMatch[1]));
      return (found || MOCK_CUSTOMERS[0]) as unknown as T;
    }
    return MOCK_CUSTOMERS as unknown as T;
  }
  if (endpoint.startsWith('/inventory')) {
    const idMatch = endpoint.match(/\/inventory\/(\d+)/);
    if (idMatch) {
      const found = MOCK_INVENTORY.find((i) => i.id === Number(idMatch[1]));
      return (found || MOCK_INVENTORY[0]) as unknown as T;
    }
    return MOCK_INVENTORY as unknown as T;
  }
  if (endpoint.startsWith('/analytics')) {
    return MOCK_ANALYTICS as unknown as T;
  }
  if (endpoint.startsWith('/alerts')) {
    if (endpoint.includes('/active')) {
      return MOCK_ALERTS as unknown as T;
    }
    return MOCK_ALERTS as unknown as T;
  }
  if (endpoint.startsWith('/notifications')) {
    if (endpoint.includes('/unread-count')) {
      return { unreadCount: 2 } as unknown as T;
    }
    return MOCK_NOTIFICATIONS as unknown as T;
  }
  if (endpoint.startsWith('/intelligence')) {
    return MOCK_INTELLIGENCE as unknown as T;
  }

  return {} as unknown as T;
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getAuthToken();
  const headers = new Headers(options.headers || {});
  headers.set('Content-Type', 'application/json');

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      return getMockFallback<T>(endpoint, options);
    }

    const resJson: ApiResponse<T> = await response.json();
    return resJson.data !== undefined ? resJson.data : (resJson as unknown as T);
  } catch (err) {
    return getMockFallback<T>(endpoint, options);
  }
}

export const authApi = {
  login: async (email: string, password: string): Promise<User> => {
    try {
      const res = await request<User>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      if (res && res.email) {
        if (res.token) setAuthToken(res.token);
        return res;
      }
    } catch {
      // fallback
    }

    const normalizedEmail = (email || '').trim().toLowerCase();
    const matchedUser = MOCK_USERS[normalizedEmail] || {
      id: 1,
      email: normalizedEmail || 'admin@fieldops.demo',
      fullName: 'FieldOps Admin',
      role: 'ADMIN' as Role,
      organization: 'FieldOps Global Operations',
      token: 'mock-demo-jwt-token',
    };

    setAuthToken(matchedUser.token || 'mock-demo-jwt-token');
    return matchedUser;
  },
  register: async (data: { email: string; password: string; fullName: string; role?: string; phone?: string; organization?: string }): Promise<User> => {
    try {
      const res = await request<User>('/auth/register', {
        method: 'POST',
        body: JSON.stringify(data),
      });
      if (res && res.email) {
        if (res.token) setAuthToken(res.token);
        return res;
      }
    } catch {
      // fallback
    }

    const newUser: User = {
      id: Math.floor(Math.random() * 1000) + 10,
      email: data.email,
      fullName: data.fullName,
      role: (data.role as Role) || 'ADMIN',
      organization: data.organization || 'FieldOps Enterprise',
      token: 'mock-demo-jwt-token',
    };
    setAuthToken(newUser.token!);
    return newUser;
  },
  getCurrentUser: async (): Promise<User> => {
    try {
      const user = await request<User>('/auth/me');
      if (user && user.email) return user;
    } catch {
      // fallback
    }
    const saved = localStorage.getItem('fieldops_user');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return MOCK_USERS['admin@fieldops.demo'];
  },
  logout: () => {
    setAuthToken(null);
    localStorage.removeItem('fieldops_user');
  },
};

export const dashboardApi = {
  getOverview: (): Promise<DashboardOverview> => request<DashboardOverview>('/dashboard'),
};

export const workOrdersApi = {
  getAll: (params?: { status?: string; priority?: string; technicianId?: number; customerId?: number; search?: string }): Promise<WorkOrder[]> => {
    const searchParams = new URLSearchParams();
    if (params?.status) searchParams.set('status', params.status);
    if (params?.priority) searchParams.set('priority', params.priority);
    if (params?.technicianId) searchParams.set('technicianId', params.technicianId.toString());
    if (params?.customerId) searchParams.set('customerId', params.customerId.toString());
    if (params?.search) searchParams.set('search', params.search);
    const qs = searchParams.toString();
    return request<WorkOrder[]>(`/work-orders${qs ? `?${qs}` : ''}`);
  },
  getById: (id: number): Promise<WorkOrder> => request<WorkOrder>(`/work-orders/${id}`),
  getByNumber: (number: string): Promise<WorkOrder> => request<WorkOrder>(`/work-orders/by-number/${number}`),
  getHighRisk: (): Promise<WorkOrder[]> => request<WorkOrder[]>('/work-orders/sla-at-risk'),
  create: (data: Partial<WorkOrder> & { customerId: number }): Promise<WorkOrder> =>
    request<WorkOrder>('/work-orders', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: Partial<WorkOrder>): Promise<WorkOrder> =>
    request<WorkOrder>(`/work-orders/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
  delete: (id: number): Promise<void> =>
    request<void>(`/work-orders/${id}`, {
      method: 'DELETE',
    }),
};

export const techniciansApi = {
  getAll: (params?: { status?: string; city?: string }): Promise<Technician[]> => {
    const searchParams = new URLSearchParams();
    if (params?.status) searchParams.set('status', params.status);
    if (params?.city) searchParams.set('city', params.city);
    const qs = searchParams.toString();
    return request<Technician[]>(`/technicians${qs ? `?${qs}` : ''}`);
  },
  getById: (id: number): Promise<Technician> => request<Technician>(`/technicians/${id}`),
  updateStatus: (id: number, status: string): Promise<Technician> =>
    request<Technician>(`/technicians/${id}/status?status=${status}`, {
      method: 'PATCH',
    }),
};

export const customersApi = {
  getAll: (city?: string): Promise<Customer[]> => {
    const qs = city ? `?city=${encodeURIComponent(city)}` : '';
    return request<Customer[]>(`/customers${qs}`);
  },
  getById: (id: number): Promise<Customer> => request<Customer>(`/customers/${id}`),
};

export const inventoryApi = {
  getAll: (params?: { category?: string; status?: string }): Promise<InventoryItem[]> => {
    const searchParams = new URLSearchParams();
    if (params?.category) searchParams.set('category', params.category);
    if (params?.status) searchParams.set('status', params.status);
    const qs = searchParams.toString();
    return request<InventoryItem[]>(`/inventory${qs ? `?${qs}` : ''}`);
  },
  getById: (id: number): Promise<InventoryItem> => request<InventoryItem>(`/inventory/${id}`),
  adjustStock: (id: number, amount: number, reason?: string): Promise<InventoryItem> =>
    request<InventoryItem>(`/inventory/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ amount, reason }),
    }),
};

export const analyticsApi = {
  getOverview: (): Promise<AnalyticsOverview> => request<AnalyticsOverview>('/analytics'),
};

export const notificationsApi = {
  getAll: (unreadOnly?: boolean): Promise<NotificationItem[]> => {
    const qs = unreadOnly ? '?unreadOnly=true' : '';
    return request<NotificationItem[]>(`/notifications${qs}`);
  },
  getUnreadCount: (): Promise<{ unreadCount: number }> => request<{ unreadCount: number }>('/notifications/unread-count'),
  markRead: (id: number): Promise<NotificationItem> =>
    request<NotificationItem>(`/notifications/${id}/read`, {
      method: 'PATCH',
    }),
  markAllRead: (): Promise<void> =>
    request<void>('/notifications/mark-all-read', {
      method: 'POST',
    }),
};

export const intelligenceApi = {
  getOverview: (): Promise<OperationsIntelligenceOverview> =>
    request<OperationsIntelligenceOverview>('/intelligence/overview'),
  getAnomalies: (): Promise<OperationalAnomaly[]> =>
    request<OperationalAnomaly[]>('/intelligence/anomalies'),
  getAssignmentRecommendation: (workOrderId: number): Promise<AssignmentRecommendation> =>
    request<AssignmentRecommendation>(`/work-orders/${workOrderId}/assignment-recommendation`),
  getSlaRisk: (workOrderId: number): Promise<SlaRiskResult> =>
    request<SlaRiskResult>(`/work-orders/${workOrderId}/sla-risk`),
  getRouteEstimate: (workOrderId: number, technicianId?: number): Promise<RouteEstimate> => {
    const qs = technicianId ? `?technicianId=${technicianId}` : '';
    return request<RouteEstimate>(`/work-orders/${workOrderId}/route-estimate${qs}`);
  },
  acceptRecommendation: (workOrderId: number, technicianId?: number): Promise<WorkOrder> =>
    request<WorkOrder>(`/work-orders/${workOrderId}/accept-recommendation`, {
      method: 'POST',
      body: JSON.stringify({ technicianId }),
    }),
  rejectRecommendation: (workOrderId: number, selectedTechnicianId?: number, reason?: string): Promise<WorkOrder> =>
    request<WorkOrder>(`/work-orders/${workOrderId}/reject-recommendation`, {
      method: 'POST',
      body: JSON.stringify({ selectedTechnicianId, reason }),
    }),
  getAuditTrail: (workOrderId: number): Promise<DecisionAuditLog[]> =>
    request<DecisionAuditLog[]>(`/work-orders/${workOrderId}/audit-trail`),
};

export const routesApi = {
  getOptimizedRoute: (technicianId: number): Promise<OptimizedRoute> =>
    request<OptimizedRoute>(`/technicians/${technicianId}/optimized-route`),
  optimizeRoute: (technicianId: number): Promise<OptimizedRoute> =>
    request<OptimizedRoute>(`/technicians/${technicianId}/optimize-route`, {
      method: 'POST',
    }),
  applyRoute: (technicianId: number): Promise<OptimizedRoute> =>
    request<OptimizedRoute>(`/technicians/${technicianId}/apply-route`, {
      method: 'POST',
    }),
  getRouteSummary: (technicianId: number): Promise<Record<string, any>> =>
    request<Record<string, any>>(`/technicians/${technicianId}/route-summary`),
  getRouteAudit: (technicianId: number): Promise<RouteDecisionAudit[]> =>
    request<RouteDecisionAudit[]>(`/technicians/${technicianId}/route-audit`),
};

export const alertsApi = {
  getAll: (params?: { severity?: string; type?: string; status?: string }): Promise<OperationalAlert[]> => {
    const searchParams = new URLSearchParams();
    if (params?.severity) searchParams.set('severity', params.severity);
    if (params?.type) searchParams.set('type', params.type);
    if (params?.status) searchParams.set('status', params.status);
    const qs = searchParams.toString();
    return request<OperationalAlert[]>(`/alerts${qs ? `?${qs}` : ''}`);
  },
  getActive: (): Promise<OperationalAlert[]> => request<OperationalAlert[]>('/alerts/active'),
  getById: (id: number): Promise<OperationalAlert> => request<OperationalAlert>(`/alerts/${id}`),
  acknowledge: (id: number): Promise<OperationalAlert> =>
    request<OperationalAlert>(`/alerts/${id}/acknowledge`, {
      method: 'POST',
    }),
  resolve: (id: number, notes?: string): Promise<OperationalAlert> =>
    request<OperationalAlert>(`/alerts/${id}/resolve`, {
      method: 'POST',
      body: JSON.stringify({ notes }),
    }),
  scan: (): Promise<{ newAlertsCount: number; totalActiveAlerts: number }> =>
    request<{ newAlertsCount: number; totalActiveAlerts: number }>('/alerts/scan', {
      method: 'POST',
    }),
};

export const predictiveApi = {
  getInventoryPredictions: (): Promise<InventoryForecastDTO[]> =>
    request<InventoryForecastDTO[]>('/inventory/predictions'),
  getInventoryForecast: (id: number): Promise<InventoryForecastDTO> =>
    request<InventoryForecastDTO>(`/inventory/${id}/forecast`),
  getReplenishmentRecommendations: (): Promise<InventoryForecastDTO[]> =>
    request<InventoryForecastDTO[]>('/inventory/replenishment-recommendations'),

  getPredictiveSla: (workOrderId: number): Promise<PredictiveSlaResultDTO> =>
    request<PredictiveSlaResultDTO>(`/sla/predictive/${workOrderId}`),
  getBatchPredictiveSla: (): Promise<PredictiveSlaResultDTO[]> =>
    request<PredictiveSlaResultDTO[]>('/sla/predictive/batch'),
};

export const performanceApi = {
  getTechnicianPerformance: (id: number): Promise<TechnicianPerformanceDTO> =>
    request<TechnicianPerformanceDTO>(`/technicians/${id}/performance`),
  getLeaderboard: (): Promise<TechnicianPerformanceDTO[]> =>
    request<TechnicianPerformanceDTO[]>('/technicians/performance/leaderboard'),
};

export const simulatorApi = {
  simulate: (input: ScenarioInput): Promise<ScenarioResult> =>
    request<ScenarioResult>('/simulator/simulate', {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  applyScenario: (simulationId: string): Promise<ScenarioResult> =>
    request<ScenarioResult>(`/simulator/apply/${simulationId}`, {
      method: 'POST',
    }),
  getAuditHistory: (): Promise<ScenarioDecisionAudit[]> =>
    request<ScenarioDecisionAudit[]>('/simulator/history'),
};


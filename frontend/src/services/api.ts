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
} from '@/types';

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

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getAuthToken();
  const headers = new Headers(options.headers || {});
  headers.set('Content-Type', 'application/json');

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    const message = errorBody.message || `API error (${response.status}): ${response.statusText}`;
    throw new Error(message);
  }

  const resJson: ApiResponse<T> = await response.json();
  return resJson.data;
}

export const authApi = {
  login: async (email: string, password: string): Promise<User> => {
    const res = await request<User>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (res.token) {
      setAuthToken(res.token);
    }
    return res;
  },
  register: async (data: { email: string; password: string; fullName: string; role?: string; phone?: string; organization?: string }): Promise<User> => {
    const res = await request<User>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    if (res.token) {
      setAuthToken(res.token);
    }
    return res;
  },
  getCurrentUser: (): Promise<User> => request<User>('/auth/me'),
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


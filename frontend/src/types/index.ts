export type Role = 'ADMIN' | 'DISPATCHER' | 'TECHNICIAN' | 'CUSTOMER';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  organization?: string;
  avatarUrl?: string;
  token?: string;
}

export type TechnicianStatus = 'AVAILABLE' | 'ON_ROUTE' | 'BUSY' | 'OFFLINE';

export interface Technician {
  id: number;
  name: string;
  email: string;
  phone: string;
  avatarUrl?: string;
  status: TechnicianStatus;
  city: string;
  address?: string;
  latitude: number;
  longitude: number;
  rating: number;
  completedJobs: number;
  slaSuccessRate: number;
  activeJobsCount: number;
  utilizationRate: number;
  skills: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Customer {
  id: number;
  name: string;
  company: string;
  email: string;
  phone: string;
  city: string;
  address?: string;
  latitude: number;
  longitude: number;
  openJobsCount: number;
  completedJobsCount: number;
  satisfactionScore: number;
  lastServiceDate?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type WorkOrderStatus = 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'AWAITING_VERIFICATION' | 'COMPLETED' | 'CANCELLED';
export type WorkOrderPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface WorkOrder {
  id: number;
  workOrderNumber: string;
  title: string;
  description: string;
  priority: WorkOrderPriority;
  status: WorkOrderStatus;
  customer: Customer;
  technician?: Technician;
  locationName: string;
  city: string;
  address: string;
  latitude: number;
  longitude: number;
  requiredSkills: string[];
  scheduledStartTime?: string;
  scheduledEndTime?: string;
  slaDeadline: string;
  completedAt?: string;
  estimatedDurationHours: number;
  slaBreachProbability: number;
  technicianEtaMinutes?: number;
  distanceKm?: number;
  serviceNotes?: string;
  aiInsight?: string;
  createdAt: string;
  updatedAt: string;
}

export type InventoryItemStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';

export interface InventoryItem {
  id: number;
  name: string;
  sku: string;
  category: string;
  availableQuantity: number;
  reservedQuantity: number;
  minThreshold: number;
  unitPrice: number;
  unitOfMeasure: string;
  status: InventoryItemStatus;
  warehouseLocation?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type NotificationSeverity = 'CRITICAL' | 'WARNING' | 'INFO' | 'SUCCESS';

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  severity: NotificationSeverity;
  readStatus: boolean;
  relatedEntityType?: string;
  relatedEntityId?: number;
  createdAt: string;
}

export interface RecentActivity {
  id: number;
  title: string;
  description: string;
  type: string;
  timeAgo: string;
  timestamp: string;
  actorName?: string;
  actorAvatar?: string;
}

export interface DashboardOverview {
  activeJobs: number;
  activeJobsTrend: string;
  slaAtRisk: number;
  slaAtRiskTrend: string;
  availableTechnicians: number;
  totalTechnicians: number;
  technicianAvailabilityTrend: string;
  completedToday: number;
  completedTodayTrend: string;
  pipelineCounts: Record<WorkOrderStatus, number>;
  highRiskJobs: WorkOrder[];
  technicians: Technician[];
  activeWorkOrders: WorkOrder[];
  recentActivities: RecentActivity[];
  technicianUtilization: Array<{
    name: string;
    fullName: string;
    utilization: number;
    jobs: number;
  }>;
}

export interface AnalyticsOverview {
  overallSlaRate: number;
  avgResolutionHours: number;
  totalJobsCompleted: number;
  averageRating: number;
  jobsCompletedOverTime: Array<{ date: string; completed: number; created: number; target: number }>;
  slaRateTrend: Array<{ date: string; slaRate: number; benchmark: number }>;
  jobsByPriority: Array<{ priority: string; count: number; color: string }>;
  jobsByRegion: Array<{ region: string; jobs: number; slaRate: number; technicians: number }>;
  technicianPerformance: Array<{ name: string; jobs: number; slaRate: number; rating: number }>;
  inventoryConsumption: Array<{ category: string; consumed: number; cost: number }>;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

// ================= PHASE 2 INTELLIGENCE ENGINE TYPES =================

export interface AssignmentFactor {
  factorName: string;
  score: number;
  maxScore: number;
  explanation: string;
}

export interface CandidateTechnicianScore {
  technicianId: number;
  name: string;
  avatarUrl?: string;
  status: TechnicianStatus;
  rating: number;
  distanceKm: number;
  estimatedEtaMinutes: number;
  totalScore: number;
  skillScore: number;
  distanceScore: number;
  availabilityScore: number;
  workloadScore: number;
  slaFeasibilityScore: number;
  performanceScore: number;
  matchedSkills: string[];
  missingSkills: string[];
  factors: AssignmentFactor[];
}

export interface AssignmentRecommendation {
  workOrderId: number;
  workOrderNumber: string;
  priority: string;
  topCandidate?: CandidateTechnicianScore;
  candidates: CandidateTechnicianScore[];
  generatedAt: string;
}

export interface SlaRiskFactor {
  factorName: string;
  impactScore: number;
  explanation: string;
}

export interface SlaRiskResult {
  workOrderId: number;
  workOrderNumber: string;
  riskScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  remainingSlaMinutes: number;
  estimatedArrivalMinutes: number;
  technicianDistanceKm: number;
  technicianName: string;
  summaryRecommendation: string;
  evaluationTimestamp: string;
  contributingFactors: SlaRiskFactor[];
}

export interface RouteEstimate {
  workOrderId: number;
  technicianId?: number;
  technicianName: string;
  distanceKm: number;
  estimatedTravelMinutes: number;
  slaRemainingMinutes: number;
  travelFeasibility: 'FEASIBLE' | 'AT_RISK' | 'UNFEASIBLE' | 'BREACHED';
  technicianLatitude?: number;
  technicianLongitude?: number;
  destinationLatitude?: number;
  destinationLongitude?: number;
  destinationAddress?: string;
}

export interface OperationalAnomaly {
  id: string;
  title: string;
  description: string;
  severity: 'INFO' | 'WARNING' | 'HIGH' | 'CRITICAL';
  category: 'WORKLOAD' | 'SLA' | 'INVENTORY' | 'TERRITORY' | 'TIMING';
  entityType: string;
  entityId: string;
  baselineMetric: number;
  currentMetric: number;
  metricUnit: string;
  recommendation: string;
  detectedAt: string;
}

export interface OperationsIntelligenceOverview {
  activeAnomaliesCount: number;
  criticalSlaCount: number;
  highSlaRisks: SlaRiskResult[];
  anomalies: OperationalAnomaly[];
  pendingSmartDispatches: AssignmentRecommendation[];
  generatedAt: string;
}

export interface DecisionAuditLog {
  id: number;
  workOrderId: number;
  workOrderNumber: string;
  decisionType: string;
  recommendedTechnicianId?: number;
  recommendedTechnicianName?: string;
  selectedTechnicianId?: number;
  selectedTechnicianName?: string;
  score?: number;
  explanation?: string;
  dispatcherEmail?: string;
  timestamp: string;
}


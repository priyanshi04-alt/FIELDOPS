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

export interface RouteStop {
  workOrderId: number;
  workOrderNumber: string;
  title: string;
  priority: string;
  status: string;
  customerName: string;
  locationName: string;
  address: string;
  latitude: number;
  longitude: number;
  slaDeadline: string;
  slaRemainingMinutes: number;
  serviceDurationMinutes: number;
  sequenceNumber: number;
  estimatedArrival: string;
  estimatedDeparture: string;
  travelTimeFromPreviousMinutes: number;
  distanceFromPreviousKm: number;
  slaStatus: 'ON_TRACK' | 'AT_RISK' | 'VIOLATION';
}

export interface RouteLeg {
  legNumber: number;
  fromName: string;
  toName: string;
  fromLat: number;
  fromLon: number;
  toLat: number;
  toLon: number;
  distanceKm: number;
  travelTimeMinutes: number;
}

export interface OptimizedRoute {
  technicianId: number;
  technicianName: string;
  technicianStatus: string;
  originAddress: string;
  originLat: number;
  originLon: number;
  stops: RouteStop[];
  legs: RouteLeg[];
  totalStops: number;
  totalDistanceKm: number;
  totalTravelTimeMinutes: number;
  totalServiceTimeMinutes: number;
  totalDurationMinutes: number;
  startTime: string;
  projectedCompletionTime: string;
  slaCompliantCount: number;
  slaAtRiskCount: number;
  slaViolationCount: number;
  optimizationScore: number;
  baselineDistanceKm: number;
  baselineTravelTimeMinutes: number;
  distanceSavedKm: number;
  timeSavedMinutes: number;
  explanation: string;
  isApplied: boolean;
  createdAt: string;
}

export interface RouteDecisionAudit {
  id: number;
  technicianId: number;
  technicianName: string;
  dispatcherEmail: string;
  jobCount: number;
  previousOrder: string;
  optimizedOrder: string;
  algorithm: string;
  optimizationScore: number;
  totalDistanceKm: number;
  totalTravelTimeMinutes: number;
  distanceSavedKm: number;
  timeSavedMinutes: number;
  slaAtRiskCount: number;
  slaViolationCount: number;
  action: string;
  explanation: string;
  timestamp: string;
}

export type AlertSeverity = 'INFO' | 'WARNING' | 'HIGH' | 'CRITICAL';
export type AlertType =
  | 'SLA_CRITICAL'
  | 'SLA_AT_RISK'
  | 'TECHNICIAN_OVERLOAD'
  | 'UNASSIGNED_CRITICAL_JOB'
  | 'ROUTE_DELAY'
  | 'INVENTORY_SHORTAGE'
  | 'TERRITORY_SPIKE'
  | 'TECHNICIAN_OFFLINE'
  | 'PREDICTED_SLA_BREACH'
  | 'PREDICTED_STOCKOUT'
  | 'PERFORMANCE_DEGRADATION'
  | 'PREDICTED_ROUTE_DELAY';
export type AlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED';

export interface OperationalAlert {
  id: number;
  alertType: AlertType;
  severity: AlertSeverity;
  status: AlertStatus;
  title: string;
  message: string;
  explanation?: string;
  fingerprint?: string;
  isPredictive?: boolean;
  relatedEntityType?: string;
  relatedEntityId?: number;
  workOrderId?: number;
  workOrderNumber?: string;
  technicianId?: number;
  technicianName?: string;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
  resolvedBy?: string;
  resolvedAt?: string;
  resolutionNotes?: string;
  createdAt: string;
}

export type OperationsEventType =
  | 'WORK_ORDER_ASSIGNED'
  | 'WORK_ORDER_STATUS_CHANGED'
  | 'TECHNICIAN_STATUS_CHANGED'
  | 'TECHNICIAN_LOCATION_UPDATED'
  | 'SLA_RISK_CHANGED'
  | 'NEW_ALERT'
  | 'ALERT_ACKNOWLEDGED'
  | 'ALERT_RESOLVED'
  | 'ROUTE_OPTIMIZED'
  | 'ROUTE_APPLIED'
  | 'INVENTORY_ALERT'
  | 'ANOMALY_DETECTED'
  | 'PREDICTIVE_ALERT_CREATED'
  | 'PREDICTIVE_RISK_CHANGED'
  | 'STOCKOUT_PREDICTED'
  | 'SLA_BREACH_PREDICTED'
  | 'SCENARIO_APPLIED';

export interface OperationsEvent {
  eventType: OperationsEventType;
  severity: string;
  message: string;
  workOrderId?: number;
  technicianId?: number;
  alertId?: number;
  timestamp: string;
  payload?: Record<string, any>;
}

export interface ModelRegistryEntry {
  id: number;
  modelKey: string;
  modelName: string;
  modelVersion: string;
  algorithm: string;
  evaluationDataset?: string;
  primaryMetricValue?: number;
  primaryMetricName?: string;
  secondaryMetricValue?: number;
  secondaryMetricName?: string;
  syntheticDisclaimer?: string;
  trainedAt?: string;
  loadedAt?: string;
}

export interface InventoryForecastDTO {
  inventoryItemId: number;
  partName: string;
  category: string;
  currentStock: number;
  safetyStock: number;
  supplierLeadDays: number;
  predicted7DayDemand: number;
  dailyDemandRate: number;
  estimatedDaysUntilStockout: number;
  recommendation: 'REORDER' | 'MONITOR' | 'OPTIMAL';
  recommendedReorderQuantity: number;
  confidenceScore: number;
  explanation: string;
  modelVersion: string;
  algorithm: string;
  syntheticDisclaimer?: string;
  generatedAt: string;
}

export interface PredictiveSlaResultDTO {
  workOrderId: number;
  workOrderNumber: string;
  title: string;
  priority: string;
  deterministicRiskScore: number;
  deterministicRiskLevel: string;
  mlBreachProbability: number;
  mlRiskAssessment: 'ON_TRACK' | 'AT_RISK' | 'LIKELY_BREACH';
  confidenceScore: number;
  fusedRiskScore: number;
  finalAssessment: 'ON_TRACK' | 'AT_RISK' | 'LIKELY_BREACH';
  remainingSlaMinutes: number;
  estimatedArrivalMinutes?: number;
  estimatedServiceDurationMinutes?: number;
  technicianDistanceKm?: number;
  technicianName?: string;
  technicianBacklog?: number;
  topContributingFactors: string[];
  recommendedAction: string;
  explanation: string;
  modelVersion: string;
  algorithm: string;
  syntheticDisclaimer?: string;
  generatedAt: string;
}

export interface TechnicianPerformanceDTO {
  technicianId: number;
  name: string;
  email: string;
  city: string;
  status: string;
  userRating: number;
  performanceScore: number;
  performanceTier: 'ELITE' | 'HIGH_PERFORMER' | 'STANDARD' | 'REQUIRES_SUPPORT';
  performanceTrend: 'IMPROVING' | 'STABLE' | 'DECLINING';
  slaAdherencePercent: number;
  firstTimeFixPercent: number;
  avgCompletionMinutes: number;
  routeEfficiencyScore: number;
  workloadBalanceScore: number;
  reliabilityScore: number;
  completedJobsCount: number;
  activeJobsCount: number;
  currentUtilizationRate: number;
  operationalStrengths: string[];
  improvementAreas: string[];
  plainEnglishSummary: string;
  evaluatedAt: string;
}

export type ScenarioType =
  | 'TECHNICIAN_UNAVAILABLE'
  | 'TECHNICIAN_DELAYED'
  | 'URGENT_JOB_INJECTED'
  | 'INVENTORY_STOCKOUT_INJECTED'
  | 'TECHNICIAN_REASSIGNED'
  | 'BULK_RESEQUENCE';

export interface ScenarioInput {
  scenarioType: ScenarioType;
  scenarioName?: string;
  targetTechnicianId?: number;
  targetWorkOrderId?: number;
  targetInventoryItemId?: number;
  delayMinutes?: number;
  fallbackTechnicianId?: number;
  parameters?: Record<string, any>;
}

export interface ScenarioResult {
  simulationId: string;
  scenarioType: ScenarioType;
  scenarioName: string;
  baselineActiveTechnicians: number;
  baselineAtRiskJobs: number;
  baselineProjectedBreaches: number;
  baselineTotalDistanceKm: number;
  baselineTotalTravelMinutes: number;
  baselineActiveAlerts: number;
  simulatedActiveTechnicians: number;
  simulatedAtRiskJobs: number;
  simulatedProjectedBreaches: number;
  simulatedTotalDistanceKm: number;
  simulatedTotalTravelMinutes: number;
  simulatedActiveAlerts: number;
  deltaAtRiskJobs: number;
  deltaProjectedBreaches: number;
  deltaDistanceKm: number;
  deltaTravelMinutes: number;
  deltaNewAlerts: number;
  reassignedJobsCount: number;
  operationalRiskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  recommendedAction: string;
  simulatedStateChanges: string[];
  projectedWarnings: string[];
  isApplied: boolean;
  generatedAt: string;
}

export interface ScenarioDecisionAudit {
  id: number;
  simulationId: string;
  scenarioType: ScenarioType;
  scenarioName: string;
  dispatcherEmail: string;
  reassignedJobsCount: number;
  deltaDistanceKm: number;
  deltaTravelMinutes: number;
  deltaProjectedBreaches: number;
  stateChangesSummary: string;
  rationale: string;
  appliedAt: string;
}

export interface OperationsIntelligenceOverview {
  operationsHealthScore?: number;
  activeAnomaliesCount: number;
  criticalSlaCount: number;
  activeAlertsCount?: number;
  predictedBreachesCount?: number;
  predictedStockoutsCount?: number;
  modelRegistry?: Record<string, ModelRegistryEntry>;
  topStockoutRisks?: InventoryForecastDTO[];
  topPredictedBreaches?: PredictiveSlaResultDTO[];
  highSlaRisks: SlaRiskResult[];
  anomalies: OperationalAnomaly[];
  pendingSmartDispatches: AssignmentRecommendation[];
  activeAlerts?: OperationalAlert[];
  sampleOptimizedRoute?: OptimizedRoute;
  recentRouteAudits?: RouteDecisionAudit[];
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



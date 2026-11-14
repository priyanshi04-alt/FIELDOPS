import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  SlidersHorizontal,
  Play,
  CheckCircle,
  AlertTriangle,
  ArrowRight,
  TrendingDown,
  TrendingUp,
  RotateCcw,
  History,
  ShieldAlert,
  MapPin,
  Clock,
  UserX,
  PackageX,
  PlusCircle,
  Sparkles,
} from 'lucide-react';
import { simulatorApi, techniciansApi, workOrdersApi } from '@/services/api';
import { ScenarioInput, ScenarioResult, ScenarioDecisionAudit, ScenarioType } from '@/types';

export const SimulatorPage: React.FC = () => {
  const queryClient = useQueryClient();

  // Form State
  const [scenarioType, setScenarioType] = useState<ScenarioType>('TECHNICIAN_UNAVAILABLE');
  const [scenarioName, setScenarioName] = useState<string>('Sudden Workforce Outage Test');
  const [targetTechnicianId, setTargetTechnicianId] = useState<number | undefined>(undefined);
  const [fallbackTechnicianId, setFallbackTechnicianId] = useState<number | undefined>(undefined);
  const [delayMinutes, setDelayMinutes] = useState<number>(45);
  const [targetWorkOrderId, setTargetWorkOrderId] = useState<number | undefined>(undefined);
  const [targetInventoryItemId, setTargetInventoryItemId] = useState<number | undefined>(1);

  // Simulation run state
  const [simulationResult, setSimulationResult] = useState<ScenarioResult | null>(null);
  const [applyModalOpen, setApplyModalOpen] = useState(false);
  const [decisionNotes, setDecisionNotes] = useState('');

  // Fetch Technicians & Work Orders for input selects
  const { data: technicians = [] } = useQuery({
    queryKey: ['technicians'],
    queryFn: () => techniciansApi.getAll(),
  });

  const { data: workOrders = [] } = useQuery({
    queryKey: ['work-orders'],
    queryFn: () => workOrdersApi.getAll(),
  });

  // Fetch past scenario audits
  const { data: history = [], refetch: refetchHistory } = useQuery({
    queryKey: ['scenario-history'],
    queryFn: () => simulatorApi.getAuditHistory(),
  });

  // Set default targets once loaded
  React.useEffect(() => {
    if (technicians.length > 0 && !targetTechnicianId) {
      setTargetTechnicianId(technicians[0].id);
      if (technicians.length > 1) {
        setFallbackTechnicianId(technicians[1].id);
      }
    }
  }, [technicians, targetTechnicianId]);

  React.useEffect(() => {
    if (workOrders.length > 0 && !targetWorkOrderId) {
      setTargetWorkOrderId(workOrders[0].id);
    }
  }, [workOrders, targetWorkOrderId]);

  // Simulate mutation
  const simulateMutation = useMutation({
    mutationFn: (input: ScenarioInput) => simulatorApi.simulate(input),
    onSuccess: (res) => {
      setSimulationResult(res);
    },
  });

  // Apply scenario mutation (Human-in-the-loop commit)
  const applyMutation = useMutation({
    mutationFn: (simulationId: string) => simulatorApi.applyScenario(simulationId),
    onSuccess: () => {
      setApplyModalOpen(false);
      refetchHistory();
      queryClient.invalidateQueries({ queryKey: ['work-orders'] });
      queryClient.invalidateQueries({ queryKey: ['technicians'] });
      queryClient.invalidateQueries({ queryKey: ['active-alerts'] });
      queryClient.invalidateQueries({ queryKey: ['intelligence-overview'] });
      alert('Scenario successfully committed into live operations!');
    },
  });

  const handleRunSimulation = (e: React.FormEvent) => {
    e.preventDefault();
    const payload: ScenarioInput = {
      scenarioType,
      scenarioName: scenarioName || `${scenarioType} Simulation`,
      targetTechnicianId: Number(targetTechnicianId),
      targetWorkOrderId: targetWorkOrderId ? Number(targetWorkOrderId) : undefined,
      targetInventoryItemId: targetInventoryItemId ? Number(targetInventoryItemId) : undefined,
      delayMinutes,
      fallbackTechnicianId: fallbackTechnicianId ? Number(fallbackTechnicianId) : undefined,
    };
    simulateMutation.mutate(payload);
  };

  const deltaSign = (val: number) => (val > 0 ? `+${val}` : `${val}`);

  return (
    <div className="p-4 lg:p-8 space-y-6 max-w-[1700px] mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl lg:text-2xl font-bold tracking-tight text-white font-sans">
              What-If Operations Simulator
            </h1>
            <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-500/15 text-indigo-400 border border-indigo-500/30 font-mono">
              In-Memory Isolated Sandbox
            </span>
          </div>
          <p className="text-xs text-slate-400">
            Simulate operational disruptions, technician outages, sudden urgent jobs, and supply shortages with zero production risk.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Form: Scenario Builder */}
        <div className="lg:col-span-5 bg-slate-900/90 border border-slate-800 rounded-xl p-5 shadow-xl space-y-5">
          <div className="flex items-center gap-2 pb-3 border-b border-slate-800">
            <SlidersHorizontal className="w-4 h-4 text-indigo-400" />
            <h2 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
              Scenario Configuration
            </h2>
          </div>

          <form onSubmit={handleRunSimulation} className="space-y-4 text-xs">
            {/* Scenario Name */}
            <div>
              <label className="block text-slate-400 font-medium mb-1.5">Scenario Name</label>
              <input
                type="text"
                value={scenarioName}
                onChange={(e) => setScenarioName(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-indigo-500"
                placeholder="Morning Peak Incident Test..."
              />
            </div>

            {/* Scenario Type */}
            <div>
              <label className="block text-slate-400 font-medium mb-1.5">Simulation Type</label>
              <select
                value={scenarioType}
                onChange={(e) => setScenarioType(e.target.value as ScenarioType)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-indigo-500"
              >
                <option value="TECHNICIAN_UNAVAILABLE">Technician Sudden Outage / Sickness</option>
                <option value="TECHNICIAN_DELAYED">Technician Route Delay (Traffic / Hold)</option>
                <option value="URGENT_JOB_INJECTED">Inject Urgent Emergency Breakdown Job</option>
                <option value="INVENTORY_STOCKOUT_INJECTED">Critical Inventory Stockout Shortage</option>
                <option value="TECHNICIAN_REASSIGNED">Technician Reassignment Scenario</option>
                <option value="BULK_RESEQUENCE">Bulk Route Resequencing</option>
              </select>
            </div>

            {/* Fields depending on scenario */}
            {(scenarioType === 'TECHNICIAN_UNAVAILABLE' ||
              scenarioType === 'TECHNICIAN_DELAYED' ||
              scenarioType === 'TECHNICIAN_REASSIGNED') && (
              <div>
                <label className="block text-slate-400 font-medium mb-1.5">Target Field Technician</label>
                <select
                  value={targetTechnicianId}
                  onChange={(e) => setTargetTechnicianId(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-indigo-500"
                >
                  {technicians.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.name} ({t.city}) - {t.status}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {scenarioType === 'TECHNICIAN_REASSIGNED' && (
              <div>
                <label className="block text-slate-400 font-medium mb-1.5">Fallback / Target Technician</label>
                <select
                  value={fallbackTechnicianId}
                  onChange={(e) => setFallbackTechnicianId(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-indigo-500"
                >
                  {technicians.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.name} ({t.city})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {scenarioType === 'TECHNICIAN_DELAYED' && (
              <div>
                <label className="block text-slate-400 font-medium mb-1.5">Delay Duration (Minutes)</label>
                <input
                  type="number"
                  min="5"
                  max="360"
                  value={delayMinutes}
                  onChange={(e) => setDelayMinutes(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 font-mono focus:outline-none focus:border-indigo-500"
                />
              </div>
            )}

            {scenarioType === 'URGENT_JOB_INJECTED' && (
              <div>
                <label className="block text-slate-400 font-medium mb-1.5">Target Work Order Template</label>
                <select
                  value={targetWorkOrderId}
                  onChange={(e) => setTargetWorkOrderId(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-indigo-500"
                >
                  {workOrders.map((wo) => (
                    <option key={wo.id} value={wo.id}>
                      {wo.workOrderNumber} - {wo.title} ({wo.priority})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {scenarioType === 'INVENTORY_STOCKOUT_INJECTED' && (
              <div>
                <label className="block text-slate-400 font-medium mb-1.5">Target Inventory Item ID</label>
                <input
                  type="number"
                  value={targetInventoryItemId}
                  onChange={(e) => setTargetInventoryItemId(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 font-mono"
                />
              </div>
            )}

            <button
              type="submit"
              disabled={simulateMutation.isPending}
              className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold rounded-lg shadow-lg flex items-center justify-center gap-2 transition"
            >
              <Play className="w-4 h-4 fill-current" />
              <span>{simulateMutation.isPending ? 'Simulating In Memory...' : 'Run What-If Simulation'}</span>
            </button>
          </form>

          {/* Sandbox Guarantees */}
          <div className="p-3 rounded-lg bg-slate-950/50 border border-slate-800 text-[11px] text-slate-400 space-y-1">
            <span className="font-semibold text-slate-300 block">Strict Isolation Contract:</span>
            <p>
              • In-memory deep cloning guarantees zero writes to PostgreSQL tables during simulation.
            </p>
            <p>
              • Only authorized dispatchers can apply an inspected scenario via Human-in-the-Loop approval.
            </p>
          </div>
        </div>

        {/* Right Panel: Impact Analysis & Before/After */}
        <div className="lg:col-span-7 space-y-5">
          {simulationResult ? (
            <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 shadow-xl space-y-5">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-slate-800">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                      ID: {simulationResult.simulationId.substring(0, 8)}
                    </span>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                      simulationResult.operationalRiskLevel === 'CRITICAL'
                        ? 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                        : simulationResult.operationalRiskLevel === 'HIGH'
                        ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                        : 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                    }`}>
                      Risk: {simulationResult.operationalRiskLevel}
                    </span>
                    <h3 className="text-base font-bold text-white font-mono">
                      {simulationResult.scenarioName}
                    </h3>
                  </div>
                  <p className="text-xs text-slate-400 mt-0.5">{simulationResult.recommendedAction}</p>
                </div>

                <button
                  onClick={() => setApplyModalOpen(true)}
                  className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-md transition self-start sm:self-auto"
                >
                  <CheckCircle className="w-3.5 h-3.5" />
                  <span>Apply Scenario</span>
                </button>
              </div>

              {/* Before vs After KPI Delta Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs font-mono">
                <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
                  <span className="text-[10px] text-slate-500 block">AT-RISK JOBS</span>
                  <div className="flex items-baseline gap-2 mt-1">
                    <span className="text-sm text-slate-400">{simulationResult.baselineAtRiskJobs}</span>
                    <ArrowRight className="w-3 h-3 text-slate-600" />
                    <span
                      className={`text-base font-bold ${
                        simulationResult.simulatedAtRiskJobs > simulationResult.baselineAtRiskJobs
                          ? 'text-rose-400'
                          : 'text-emerald-400'
                      }`}
                    >
                      {simulationResult.simulatedAtRiskJobs}
                    </span>
                  </div>
                  <span className="text-[10px] text-slate-500">
                    Delta: {deltaSign(simulationResult.deltaAtRiskJobs)}
                  </span>
                </div>

                <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
                  <span className="text-[10px] text-slate-500 block">TOTAL TRANSIT DISTANCE</span>
                  <div className="flex items-baseline gap-2 mt-1">
                    <span className="text-sm text-slate-400">{simulationResult.baselineTotalDistanceKm.toFixed(1)} km</span>
                    <ArrowRight className="w-3 h-3 text-slate-600" />
                    <span className="text-base font-bold text-white">
                      {simulationResult.simulatedTotalDistanceKm.toFixed(1)} km
                    </span>
                  </div>
                  <span className="text-[10px] text-slate-500">
                    Delta: {deltaSign(Number(simulationResult.deltaDistanceKm.toFixed(1)))} km
                  </span>
                </div>

                <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
                  <span className="text-[10px] text-slate-500 block">TOTAL TRAVEL TIME</span>
                  <div className="flex items-baseline gap-2 mt-1">
                    <span className="text-sm text-slate-400">{simulationResult.baselineTotalTravelMinutes}m</span>
                    <ArrowRight className="w-3 h-3 text-slate-600" />
                    <span className="text-base font-bold text-white">
                      {simulationResult.simulatedTotalTravelMinutes}m
                    </span>
                  </div>
                  <span className="text-[10px] text-slate-500">
                    Delta: {deltaSign(simulationResult.deltaTravelMinutes)}m
                  </span>
                </div>

                <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
                  <span className="text-[10px] text-slate-500 block">ALERT VOLUME</span>
                  <div className="flex items-baseline gap-2 mt-1">
                    <span className="text-sm text-slate-400">{simulationResult.baselineActiveAlerts}</span>
                    <ArrowRight className="w-3 h-3 text-slate-600" />
                    <span className="text-base font-bold text-amber-400">
                      {simulationResult.simulatedActiveAlerts}
                    </span>
                  </div>
                  <span className="text-[10px] text-slate-500">
                    Delta: {deltaSign(simulationResult.deltaNewAlerts)}
                  </span>
                </div>
              </div>

              {/* State Changes Log */}
              <div className="space-y-2">
                <span className="font-bold text-slate-300 uppercase font-mono text-xs">
                  Simulated State Changes ({simulationResult.simulatedStateChanges.length})
                </span>
                <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800 text-xs space-y-1.5 font-mono">
                  {simulationResult.simulatedStateChanges.map((change: string, idx: number) => (
                    <div key={idx} className="text-indigo-300 flex items-start gap-2">
                      <span className="text-indigo-500">›</span>
                      <span>{change}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Projected Warnings */}
              {simulationResult.projectedWarnings.length > 0 && (
                <div className="p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 text-xs text-amber-300 space-y-1">
                  <span className="font-bold block">Simulation Operational Warnings:</span>
                  {simulationResult.projectedWarnings.map((w: string, idx: number) => (
                    <p key={idx}>⚠️ {w}</p>
                  ))}
                </div>
              )}
            </div>
          ) : (
            /* Empty State */
            <div className="bg-slate-900/60 border border-dashed border-slate-800 rounded-xl p-12 text-center space-y-3">
              <div className="w-12 h-12 rounded-full bg-slate-800 flex items-center justify-center mx-auto text-slate-500">
                <SlidersHorizontal className="w-6 h-6" />
              </div>
              <h3 className="text-sm font-bold text-slate-300">No Simulation Executed</h3>
              <p className="text-xs text-slate-500 max-w-sm mx-auto">
                Configure an operational disruption on the left and click "Run What-If Simulation" to preview SLA impact and optimal rerouting.
              </p>
            </div>
          )}

          {/* Past Applied Scenario Audits */}
          <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 shadow-xl space-y-3">
            <div className="flex items-center justify-between pb-2 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <History className="w-4 h-4 text-slate-400" />
                <h3 className="text-xs font-bold text-slate-200 uppercase font-mono">
                  Applied Scenario Audit Trail ({history.length})
                </h3>
              </div>
            </div>

            {history.length === 0 ? (
              <p className="text-xs text-slate-500 py-2">No scenarios have been applied to production yet.</p>
            ) : (
              <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
                {history.map((audit: ScenarioDecisionAudit) => (
                  <div
                    key={audit.id}
                    className="p-3 rounded-lg bg-slate-950/60 border border-slate-800/80 text-xs space-y-1"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-mono font-bold text-indigo-400">{audit.scenarioName}</span>
                      <span className="text-[10px] text-slate-500 font-mono">
                        {new Date(audit.appliedAt).toLocaleString()}
                      </span>
                    </div>
                    <p className="text-slate-300 text-[11px]">{audit.stateChangesSummary}</p>
                    <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1 border-t border-slate-800/50 font-mono">
                      <span>By: {audit.dispatcherEmail}</span>
                      <span>Jobs Reassigned: {audit.reassignedJobsCount} | SLA Breaches Delta: {audit.deltaProjectedBreaches}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Human-in-the-Loop Confirmation Modal */}
      {applyModalOpen && simulationResult && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
          <div className="bg-slate-900 border border-emerald-500/40 rounded-xl w-full max-w-lg p-6 shadow-2xl relative space-y-4">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-base font-bold text-white">Confirm Human-in-the-Loop Application</h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  This will commit simulated assignments and rerouting directly to live production operations.
                </p>
              </div>
              <button
                onClick={() => setApplyModalOpen(false)}
                className="text-slate-400 hover:text-white"
              >
                ✕
              </button>
            </div>

            <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 text-xs space-y-1.5 font-mono">
              <div className="flex justify-between text-slate-400">
                <span>Scenario Type:</span>
                <span className="text-indigo-400 font-bold">{simulationResult.scenarioType}</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Jobs to Reassign:</span>
                <span className="text-emerald-400 font-bold">{simulationResult.reassignedJobsCount}</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Net SLA Breach Delta:</span>
                <span className="text-white font-bold">{deltaSign(simulationResult.deltaProjectedBreaches)}</span>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-slate-800 text-xs">
              <button
                onClick={() => setApplyModalOpen(false)}
                className="px-4 py-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 rounded-lg transition"
              >
                Cancel
              </button>
              <button
                onClick={() => applyMutation.mutate(simulationResult.simulationId)}
                disabled={applyMutation.isPending}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-semibold rounded-lg shadow-md transition"
              >
                {applyMutation.isPending ? 'Committing...' : 'Commit to Production'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

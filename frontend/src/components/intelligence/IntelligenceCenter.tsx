import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { 
  ShieldAlert, Sparkles, AlertTriangle, AlertCircle, 
  CheckCircle2, ArrowRight, UserCheck, RefreshCw, Zap,
  Navigation, Activity, FileText, Check
} from 'lucide-react';
import { intelligenceApi } from '@/services/api';

export const IntelligenceCenter: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'alerts' | 'sla' | 'dispatch' | 'routing' | 'anomalies' | 'predictive'>('alerts');

  const { data: intelligence, isLoading, refetch } = useQuery({
    queryKey: ['intelligence-overview'],
    queryFn: () => intelligenceApi.getOverview(),
    refetchInterval: 15000, // auto-refresh intelligence feed every 15s
  });

  const activeAlerts = intelligence?.activeAlerts || [];
  const slaRisks = intelligence?.highSlaRisks || [];
  const anomalies = intelligence?.anomalies || [];
  const pendingDispatches = intelligence?.pendingSmartDispatches || [];
  const sampleRoute = intelligence?.sampleOptimizedRoute;
  const recentAudits = intelligence?.recentRouteAudits || [];
  const healthScore = intelligence?.operationsHealthScore ?? 96;
  const registeredModels = Object.values(intelligence?.modelRegistry || {});
  const topStockoutRisks = intelligence?.topStockoutRisks || [];
  const topPredictedSlaBreaches = intelligence?.topPredictedBreaches || [];

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 space-y-4 shadow-xl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-slate-800">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-lg bg-indigo-500/15 text-indigo-400 border border-indigo-500/30">
            <Sparkles className="w-4 h-4 animate-pulse" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-bold text-white tracking-tight">
                INTELLIGENCE CENTER
              </h3>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                Phase 3 Multi-Job Active
              </span>
            </div>
            <p className="text-[11px] text-slate-400">
              Live operational anomaly detection, SLA breach scoring, multi-stop route telemetry, and alerts
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {/* Operations Health Score Meter */}
          <div className="flex items-center gap-2 px-3 py-1 bg-slate-950 rounded-lg border border-slate-800">
            <Activity className="w-3.5 h-3.5 text-emerald-400" />
            <div className="text-right">
              <span className="text-[10px] font-medium text-slate-400 block leading-none">GRID HEALTH</span>
              <span className="text-xs font-mono font-bold text-emerald-400 leading-none">{healthScore}%</span>
            </div>
          </div>

          <button
            onClick={() => refetch()}
            className="self-start sm:self-center p-1.5 text-slate-400 hover:text-white bg-slate-950 border border-slate-800 rounded-lg transition"
            title="Refresh Operations Intelligence"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Navigation Sub-Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-2 text-xs overflow-x-auto">
        <button
          onClick={() => setActiveTab('alerts')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition whitespace-nowrap ${
            activeTab === 'alerts'
              ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <AlertCircle className="w-3.5 h-3.5 text-rose-400" />
          <span>Active Alerts</span>
          <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-rose-500/30 rounded-full">
            {activeAlerts.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('sla')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition whitespace-nowrap ${
            activeTab === 'sla'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <ShieldAlert className="w-3.5 h-3.5 text-amber-400" />
          <span>SLA Risks</span>
          <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-amber-500/30 rounded-full">
            {slaRisks.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('dispatch')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition whitespace-nowrap ${
            activeTab === 'dispatch'
              ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <UserCheck className="w-3.5 h-3.5 text-indigo-400" />
          <span>Smart Dispatch</span>
          <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-indigo-500/30 rounded-full">
            {pendingDispatches.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('routing')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition whitespace-nowrap ${
            activeTab === 'routing'
              ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Navigation className="w-3.5 h-3.5 text-blue-400" />
          <span>Multi-Stop Routes</span>
          {sampleRoute && (
            <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-blue-500/30 rounded-full">
              {sampleRoute.totalStops} stops
            </span>
          )}
        </button>

        <button
          onClick={() => setActiveTab('anomalies')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition whitespace-nowrap ${
            activeTab === 'anomalies'
              ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <AlertTriangle className="w-3.5 h-3.5 text-purple-400" />
          <span>Anomalies</span>
          <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-purple-500/30 rounded-full">
            {anomalies.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('predictive')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition whitespace-nowrap ${
            activeTab === 'predictive'
              ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
          <span>Predictive Operations (Phase 4)</span>
          <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-indigo-500/30 rounded-full">
            {registeredModels.length} ML
          </span>
        </button>
      </div>

      {/* Tab Content */}
      {isLoading ? (
        <div className="p-8 text-center text-xs text-slate-500 animate-pulse">
          Evaluating fleet telemetry vectors...
        </div>
      ) : (
        <div className="space-y-3">
          {/* 1. ACTIVE ALERTS TAB (Phase 3) */}
          {activeTab === 'alerts' && (
            <div className="space-y-2.5">
              <div className="flex items-center justify-between pb-1">
                <span className="text-[11px] text-slate-400">
                  Deterministic NOC alert triggers requiring triage
                </span>
                <Link
                  to="/app/alerts"
                  className="text-xs text-blue-400 hover:text-blue-300 font-medium flex items-center gap-1"
                >
                  <span>Alert Center ({activeAlerts.length})</span>
                  <ArrowRight className="w-3 h-3" />
                </Link>
              </div>

              {activeAlerts.length === 0 ? (
                <div className="p-6 text-center text-xs text-slate-500 bg-slate-950/40 rounded-lg">
                  ✓ Grid nominal: Zero active operational alerts.
                </div>
              ) : (
                activeAlerts.slice(0, 4).map((alert: any) => (
                  <div
                    key={alert.id}
                    className="p-3 rounded-lg bg-slate-950/80 border border-slate-800 hover:border-slate-700 transition flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 text-xs"
                  >
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span
                          className={`text-[9px] font-bold px-1.5 py-0.5 rounded border ${
                            alert.severity === 'CRITICAL'
                              ? 'bg-rose-500/20 text-rose-400 border-rose-500/30'
                              : alert.severity === 'HIGH'
                              ? 'bg-amber-500/20 text-amber-400 border-amber-500/30'
                              : 'bg-blue-500/20 text-blue-400 border-blue-500/30'
                          }`}
                        >
                          {alert.severity}
                        </span>
                        <span className="font-semibold text-slate-200">{alert.title}</span>
                        {alert.workOrderNumber && (
                          <span className="font-mono text-slate-400 text-[11px]">
                            {alert.workOrderNumber}
                          </span>
                        )}
                      </div>
                      <p className="text-[11px] text-slate-400 line-clamp-1">{alert.message}</p>
                    </div>

                    <Link
                      to="/app/alerts"
                      className="px-2.5 py-1 text-[11px] font-medium bg-slate-900 hover:bg-slate-800 text-blue-400 border border-slate-800 rounded transition self-end sm:self-center"
                    >
                      Triage
                    </Link>
                  </div>
                ))
              )}
            </div>
          )}

          {/* 2. SLA RISKS TAB */}
          {activeTab === 'sla' && (
            <div className="space-y-2.5">
              {slaRisks.length === 0 ? (
                <div className="p-6 text-center text-xs text-slate-500 bg-slate-950/40 rounded-lg">
                  ✓ All active work orders proceeding within contractual SLA safety windows.
                </div>
              ) : (
                slaRisks.map((job: any) => (
                  <div
                    key={job.workOrderId}
                    className="p-3 rounded-lg bg-slate-950/80 border border-slate-800/90 hover:border-slate-700 transition flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 text-xs"
                  >
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-mono font-bold text-white">
                          {job.workOrderNumber}
                        </span>
                        <span
                          className={`text-[9px] font-bold px-1.5 py-0.5 rounded border ${
                            job.riskLevel === 'CRITICAL'
                              ? 'bg-rose-500/20 text-rose-400 border-rose-500/30'
                              : 'bg-amber-500/20 text-amber-400 border-amber-500/30'
                          }`}
                        >
                          {job.riskLevel}
                        </span>
                        <span className="text-slate-400 text-[11px]">
                          Tech: {job.technicianName}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-400 line-clamp-1">
                        {job.summaryRecommendation}
                      </p>
                    </div>

                    <div className="flex items-center gap-4 shrink-0 justify-between sm:justify-end">
                      <div className="text-right">
                        <span className="font-mono font-bold text-rose-400 text-sm block">
                          {job.riskScore}%
                        </span>
                        <span className="text-[10px] text-slate-500 font-mono">
                          {job.remainingSlaMinutes}m left
                        </span>
                      </div>

                      <Link
                        to={`/app/work-orders/${job.workOrderId}`}
                        className="p-2 rounded bg-slate-900 hover:bg-slate-800 text-blue-400 border border-slate-800 transition"
                        title="Open Work Order Details"
                      >
                        <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}

          {/* 2. SMART DISPATCH TAB */}
          {activeTab === 'dispatch' && (
            <div className="space-y-2.5">
              {pendingDispatches.length === 0 ? (
                <div className="p-6 text-center text-xs text-slate-500 bg-slate-950/40 rounded-lg">
                  No unassigned tickets awaiting dispatch candidate evaluation.
                </div>
              ) : (
                pendingDispatches.map((disp: any) => {
                  const top = disp.topCandidate;
                  return (
                    <div
                      key={disp.workOrderId}
                      className="p-3.5 rounded-lg bg-slate-950/80 border border-indigo-500/20 hover:border-indigo-500/40 transition flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs"
                    >
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-mono font-bold text-white">
                            {disp.workOrderNumber}
                          </span>
                          <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                            {disp.priority}
                          </span>
                        </div>
                        {top ? (
                          <div className="text-[11px] text-slate-300">
                            Top Recommendation:{' '}
                            <strong className="text-indigo-400">{top.name}</strong>{' '}
                            <span className="text-emerald-400 font-mono">({top.totalScore}% match)</span>
                            <span className="text-slate-500 ml-1.5 font-mono">
                              • {top.distanceKm} km • ETA {top.estimatedEtaMinutes}m
                            </span>
                          </div>
                        ) : (
                          <span className="text-slate-500">Evaluating technician roster...</span>
                        )}
                      </div>

                      <Link
                        to={`/app/work-orders/${disp.workOrderId}`}
                        className="px-3 py-1.5 bg-indigo-600/90 hover:bg-indigo-600 text-white rounded text-xs font-semibold shrink-0 transition flex items-center gap-1.5"
                      >
                        <span>Review & Assign</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </div>
                  );
                })
              )}
            </div>
          )}

          {/* 4. MULTI-STOP ROUTES TAB (Phase 3) */}
          {activeTab === 'routing' && (
            <div className="space-y-3">
              {sampleRoute ? (
                <div className="p-3.5 rounded-lg bg-slate-950/80 border border-blue-500/30 space-y-2.5 text-xs">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-white">{sampleRoute.technicianName}</span>
                        <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 border border-blue-500/30">
                          {sampleRoute.totalStops} Assigned Stops
                        </span>
                      </div>
                      <span className="text-[11px] text-slate-400">
                        {sampleRoute.originAddress}
                      </span>
                    </div>

                    <div className="text-right">
                      <span className="font-mono font-bold text-emerald-400 text-xs">
                        +{sampleRoute.distanceSavedKm} km saved
                      </span>
                      <span className="text-[10px] text-slate-400 block font-mono">
                        Score: {sampleRoute.optimizationScore}/100
                      </span>
                    </div>
                  </div>

                  <div className="grid grid-cols-3 gap-2 py-2 border-y border-slate-900 text-center font-mono text-[11px]">
                    <div>
                      <span className="text-[10px] text-slate-500 block uppercase">Travel</span>
                      <span className="text-slate-200 font-bold">{sampleRoute.totalTravelTimeMinutes}m</span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-500 block uppercase">Service</span>
                      <span className="text-slate-200 font-bold">{sampleRoute.totalServiceTimeMinutes}m</span>
                    </div>
                    <div>
                      <span className="text-[10px] text-slate-500 block uppercase">SLA Status</span>
                      <span className="text-emerald-400 font-bold">{sampleRoute.slaCompliantCount} Ok</span>
                      {sampleRoute.slaAtRiskCount > 0 && (
                        <span className="text-rose-400 font-bold ml-1">/ {sampleRoute.slaAtRiskCount} Risk</span>
                      )}
                    </div>
                  </div>

                  <p className="text-[11px] text-slate-400 leading-snug">
                    {sampleRoute.explanation}
                  </p>
                </div>
              ) : (
                <div className="p-6 text-center text-xs text-slate-500 bg-slate-950/40 rounded-lg">
                  Select a technician on the operations map to view deterministic multi-stop route sequence.
                </div>
              )}

              {/* Recent Audit trail */}
              {recentAudits.length > 0 && (
                <div className="pt-2 border-t border-slate-800 space-y-1.5">
                  <span className="text-[10px] font-mono text-slate-500 uppercase tracking-wider block">
                    Recent Route Decision Audits
                  </span>
                  {recentAudits.slice(0, 2).map((audit: any) => (
                    <div key={audit.id} className="text-[11px] text-slate-400 flex items-center justify-between">
                      <span className="truncate">
                        {audit.technicianName}: {audit.action} ({audit.jobCount} jobs)
                      </span>
                      <span className="text-emerald-400 font-mono shrink-0">
                        -{audit.timeSavedMinutes}m saved
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* 5. OPERATIONAL ANOMALIES TAB */}
          {activeTab === 'anomalies' && (
            <div className="space-y-2.5">
              {anomalies.length === 0 ? (
                <div className="p-6 text-center text-xs text-slate-500 bg-slate-950/40 rounded-lg">
                  ✓ Telemetry nominal: no workload or inventory anomalies flagged.
                </div>
              ) : (
                anomalies.map((anom: any) => (
                  <div
                    key={anom.id}
                    className="p-3.5 rounded-lg bg-slate-950/80 border border-amber-900/40 hover:border-amber-700/60 transition space-y-1.5 text-xs"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <AlertTriangle className={`w-3.5 h-3.5 ${
                          anom.severity === 'CRITICAL' ? 'text-rose-400' : 'text-amber-400'
                        }`} />
                        <span className="font-semibold text-slate-200">{anom.title}</span>
                      </div>
                      <span className={`text-[9px] font-mono font-bold px-1.5 py-0.5 rounded border ${
                        anom.severity === 'CRITICAL'
                          ? 'bg-rose-500/20 text-rose-400 border-rose-500/30'
                          : 'bg-amber-500/20 text-amber-400 border-amber-500/30'
                      }`}>
                        {anom.severity}
                      </span>
                    </div>

                    <p className="text-[11px] text-slate-400 leading-snug">
                      {anom.description}
                    </p>

                    <div className="pt-1.5 border-t border-slate-900 flex items-center justify-between text-[10px] text-slate-500 font-mono">
                      <span>Baseline: {anom.baselineMetric} vs Current: {anom.currentMetric} {anom.metricUnit}</span>
                      <span className="text-amber-400/90 font-sans">Action: {anom.recommendation}</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}

          {/* 6. PREDICTIVE OPERATIONS TAB (Phase 4) */}
          {activeTab === 'predictive' && (
            <div className="space-y-5">
              {/* Model Health Registry */}
              <div className="space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-mono font-bold text-slate-300 uppercase">
                    Machine Learning Model Registry & Health ({registeredModels.length})
                  </span>
                  <span className="text-[10px] text-indigo-400 font-mono">
                    Trained Off-line with Baseline Comparison
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {registeredModels.map((m: any) => (
                    <div
                      key={m.id}
                      className="p-3.5 rounded-lg bg-slate-950/80 border border-indigo-500/30 space-y-2 text-xs"
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <span className="font-bold text-white block">{m.modelName}</span>
                          <span className="text-[10px] text-slate-400 font-mono">v{m.version} • {m.algorithm}</span>
                        </div>
                        <span className={`text-[9px] font-mono font-bold px-1.5 py-0.5 rounded border ${
                          m.status === 'ACTIVE'
                            ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                            : 'bg-slate-500/20 text-slate-400 border-slate-500/40'
                        }`}>
                          {m.status}
                        </span>
                      </div>

                      <div className="p-2 bg-slate-900 rounded border border-slate-800 text-[11px] font-mono space-y-1">
                        <div className="flex justify-between text-slate-400">
                          <span>ML Metrics:</span>
                          <span className="text-emerald-400 font-bold">
                            {m.mae !== undefined ? `MAE ${m.mae?.toFixed(2)} | R² ${m.r2Score?.toFixed(2)}` : `AUC ${m.rocAuc?.toFixed(2)} | F1 ${m.f1Score?.toFixed(2)}`}
                          </span>
                        </div>
                        <div className="flex justify-between text-slate-400">
                          <span>Heuristic Baseline:</span>
                          <span className="text-slate-300">
                            {m.baselineMae !== undefined ? `MAE ${m.baselineMae?.toFixed(2)} (${m.baselineMethod})` : `AUC ${m.baselineRocAuc?.toFixed(2)} (${m.baselineMethod})`}
                          </span>
                        </div>
                      </div>

                      <p className="text-[10px] text-slate-400 leading-relaxed">
                        Features: {m.featureNames?.join(', ')}
                      </p>
                    </div>
                  ))}
                </div>
              </div>

              {/* Dual Intelligence Highlights: Top Stockout Risks & Top Predicted Breaches */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Top Stockout Risks */}
                <div className="p-3.5 rounded-lg bg-slate-950/60 border border-slate-800 space-y-2.5">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-bold text-slate-300 uppercase font-mono">
                      Top Stockout Depletion Risks
                    </span>
                    <Link to="/app/inventory" className="text-[10px] text-indigo-400 hover:underline">
                      View Inventory →
                    </Link>
                  </div>

                  {topStockoutRisks.length === 0 ? (
                    <div className="p-4 text-center text-xs text-slate-500">
                      ✓ No inventory shortages projected within 7 days.
                    </div>
                  ) : (
                    topStockoutRisks.slice(0, 3).map((f: any) => (
                      <div key={f.itemId} className="p-2 rounded bg-slate-900/80 border border-slate-800 text-xs space-y-1">
                        <div className="flex items-center justify-between">
                          <span className="font-semibold text-slate-200">{f.partName}</span>
                          <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-300 border border-rose-500/30 font-mono">
                            {f.estimatedDaysUntilStockout}d to stockout
                          </span>
                        </div>
                        <div className="flex justify-between text-[11px] text-slate-400 font-mono">
                          <span>Stock: {f.currentStock} units</span>
                          <span className="text-rose-400 font-bold">Reorder: +{f.recommendedReorderQuantity} units</span>
                        </div>
                      </div>
                    ))
                  )}
                </div>

                {/* Top Predicted SLA Breaches */}
                <div className="p-3.5 rounded-lg bg-slate-950/60 border border-slate-800 space-y-2.5">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-bold text-slate-300 uppercase font-mono">
                      Top Predicted SLA Breaches
                    </span>
                    <Link to="/app/work-orders" className="text-[10px] text-indigo-400 hover:underline">
                      View Tickets →
                    </Link>
                  </div>

                  {topPredictedSlaBreaches.length === 0 ? (
                    <div className="p-4 text-center text-xs text-slate-500">
                      ✓ All active work orders within safe delivery buffers.
                    </div>
                  ) : (
                    topPredictedSlaBreaches.slice(0, 3).map((s: any) => (
                      <div key={s.workOrderId} className="p-2 rounded bg-slate-900/80 border border-slate-800 text-xs space-y-1">
                        <div className="flex items-center justify-between">
                          <span className="font-mono font-semibold text-indigo-300">{s.workOrderNumber}</span>
                          <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-300 border border-rose-500/30 font-mono">
                            {s.finalAssessment} ({s.fusedRiskScore}%)
                          </span>
                        </div>
                        <p className="text-[11px] text-slate-400 truncate">
                          {s.summaryRecommendation}
                        </p>
                      </div>
                    ))
                  )}
                </div>
              </div>

              {/* Disclaimer */}
              <div className="p-2.5 rounded bg-amber-500/10 border border-amber-500/20 text-[10px] text-amber-400/90">
                ⚠️ DEMO / SIMULATION DATA: Synthetic demonstration datasets utilized for model training. Predictions augment deterministic rules and do not override established safety policies without human authorization.
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

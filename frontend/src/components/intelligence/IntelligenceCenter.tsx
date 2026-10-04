import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { 
  ShieldAlert, Sparkles, AlertTriangle, AlertCircle, 
  CheckCircle2, ArrowRight, UserCheck, RefreshCw, Zap
} from 'lucide-react';
import { intelligenceApi } from '@/services/api';

export const IntelligenceCenter: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'sla' | 'dispatch' | 'anomalies'>('sla');

  const { data: intelligence, isLoading, refetch } = useQuery({
    queryKey: ['intelligence-overview'],
    queryFn: () => intelligenceApi.getOverview(),
    refetchInterval: 15000, // auto-refresh intelligence feed every 15s
  });

  const slaRisks = intelligence?.highSlaRisks || [];
  const anomalies = intelligence?.anomalies || [];
  const pendingDispatches = intelligence?.pendingSmartDispatches || [];

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
                Deterministic Engine Active
              </span>
            </div>
            <p className="text-[11px] text-slate-400">
              Live operational anomaly detection, SLA breach scoring, and candidate dispatch rankings
            </p>
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

      {/* Navigation Sub-Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-2 text-xs">
        <button
          onClick={() => setActiveTab('sla')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition ${
            activeTab === 'sla'
              ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <ShieldAlert className="w-3.5 h-3.5 text-rose-400" />
          <span>SLA Risks</span>
          <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-rose-500/30 rounded-full">
            {slaRisks.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('dispatch')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition ${
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
          onClick={() => setActiveTab('anomalies')}
          className={`flex items-center gap-1.5 px-3 py-1 rounded-lg font-medium transition ${
            activeTab === 'anomalies'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />
          <span>Operational Anomalies</span>
          <span className="ml-1 text-[10px] font-mono font-bold px-1.5 py-0.2 bg-amber-500/30 rounded-full">
            {anomalies.length}
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
          {/* 1. SLA RISKS TAB */}
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

          {/* 3. OPERATIONAL ANOMALIES TAB */}
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
        </div>
      )}
    </div>
  );
};

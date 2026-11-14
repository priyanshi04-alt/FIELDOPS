import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Navigation,
  Compass,
  CheckCircle2,
  Clock,
  TrendingDown,
  ShieldAlert,
  ArrowRight,
  Sparkles,
  History,
  Check,
  AlertTriangle,
  Send,
  RefreshCw,
} from 'lucide-react';
import { routesApi, techniciansApi } from '@/services/api';
import { OptimizedRoute, RouteDecisionAudit, Technician } from '@/types';

interface RouteOptimizationPanelProps {
  technicians?: Technician[];
  onRouteSelected?: (route: OptimizedRoute | null) => void;
}

export const RouteOptimizationPanel: React.FC<RouteOptimizationPanelProps> = ({ 
  technicians: propTechnicians,
  onRouteSelected 
}) => {
  const queryClient = useQueryClient();
  const [selectedTechId, setSelectedTechId] = useState<number | null>(null);
  const [showAuditHistory, setShowAuditHistory] = useState(false);
  const [applySuccessMsg, setApplySuccessMsg] = useState<string | null>(null);

  const { data: fetchedTechnicians = [] } = useQuery({
    queryKey: ['technicians'],
    queryFn: () => techniciansApi.getAll(),
    enabled: !propTechnicians || propTechnicians.length === 0,
  });

  const technicians = propTechnicians && propTechnicians.length > 0 ? propTechnicians : fetchedTechnicians;

  // Default to first technician if none selected
  React.useEffect(() => {
    if (!selectedTechId && technicians.length > 0) {
      setSelectedTechId(technicians[0].id);
    }
  }, [technicians, selectedTechId]);

  const {
    data: route,
    isLoading: isRouteLoading,
    refetch: refetchRoute,
  } = useQuery({
    queryKey: ['optimized-route', selectedTechId],
    queryFn: () => (selectedTechId ? routesApi.getOptimizedRoute(selectedTechId) : null),
    enabled: !!selectedTechId,
  });

  // Notify parent map component
  React.useEffect(() => {
    if (onRouteSelected) {
      onRouteSelected(route || null);
    }
  }, [route, onRouteSelected]);

  const { data: auditHistory = [] } = useQuery({
    queryKey: ['route-audit', selectedTechId],
    queryFn: () => (selectedTechId ? routesApi.getRouteAudit(selectedTechId) : []),
    enabled: !!selectedTechId && showAuditHistory,
  });

  const optimizeMutation = useMutation({
    mutationFn: (techId: number) => routesApi.optimizeRoute(techId),
    onSuccess: (newRoute) => {
      queryClient.setQueryData(['optimized-route', selectedTechId], newRoute);
      queryClient.invalidateQueries({ queryKey: ['route-audit', selectedTechId] });
      setApplySuccessMsg(null);
    },
  });

  const applyMutation = useMutation({
    mutationFn: (techId: number) => routesApi.applyRoute(techId),
    onSuccess: (appliedRoute) => {
      queryClient.setQueryData(['optimized-route', selectedTechId], appliedRoute);
      queryClient.invalidateQueries({ queryKey: ['work-orders'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-work-orders'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
      queryClient.invalidateQueries({ queryKey: ['route-audit', selectedTechId] });
      setApplySuccessMsg('Route sequence scheduled and applied successfully.');
      setTimeout(() => setApplySuccessMsg(null), 5000);
    },
  });

  const selectedTech = technicians.find((t) => t.id === selectedTechId);

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 space-y-4 shadow-xl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-800">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-lg bg-cyan-500/15 text-cyan-400 border border-cyan-500/30">
            <Compass className="w-5 h-5 animate-spin-slow" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-bold text-white tracking-tight uppercase font-mono">
                Multi-Stop Route Optimizer
              </h3>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-cyan-500/15 text-cyan-300 border border-cyan-500/30">
                2-Opt Heuristic
              </span>
            </div>
            <p className="text-[11px] text-slate-400">
              Deterministic sequence balancing geographic proximity, SLA contract horizons, and travel duration.
            </p>
          </div>
        </div>

        {/* Technician Selector */}
        <div className="flex items-center gap-2">
          <select
            value={selectedTechId || ''}
            onChange={(e) => setSelectedTechId(Number(e.target.value))}
            className="text-xs bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
          >
            {technicians.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name} ({t.city}) — {t.status}
              </option>
            ))}
          </select>

          <button
            onClick={() => selectedTechId && optimizeMutation.mutate(selectedTechId)}
            disabled={optimizeMutation.isPending || !selectedTechId}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold shadow transition disabled:opacity-50"
            title="Recalculate 2-Opt Itinerary"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${optimizeMutation.isPending ? 'animate-spin' : ''}`} />
            <span>Re-Optimize</span>
          </button>
        </div>
      </div>

      {applySuccessMsg && (
        <div className="p-3 rounded-lg bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <span>{applySuccessMsg}</span>
          </div>
        </div>
      )}

      {/* Metrics Strip */}
      {route && route.stops.length > 0 ? (
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-3">
          <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
            <span className="text-[10px] text-slate-400 block font-mono">Total Stops</span>
            <span className="text-xl font-extrabold text-white font-mono mt-0.5 block">
              {route.totalStops} Stops
            </span>
            <span className="text-[10px] text-slate-500 font-mono">
              Orig: {route.originAddress}
            </span>
          </div>

          <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
            <span className="text-[10px] text-slate-400 block font-mono">Transit Distance</span>
            <span className="text-xl font-extrabold text-cyan-400 font-mono mt-0.5 block">
              {route.totalDistanceKm} km
            </span>
            <span className="text-[10px] text-emerald-400 font-mono flex items-center gap-1">
              <TrendingDown className="w-3 h-3" />
              <span>{route.distanceSavedKm} km saved</span>
            </span>
          </div>

          <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
            <span className="text-[10px] text-slate-400 block font-mono">Est. Transit Time</span>
            <span className="text-xl font-extrabold text-white font-mono mt-0.5 block">
              {route.totalTravelTimeMinutes} min
            </span>
            <span className="text-[10px] text-emerald-400 font-mono flex items-center gap-1">
              <TrendingDown className="w-3 h-3" />
              <span>{route.timeSavedMinutes} min saved</span>
            </span>
          </div>

          <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
            <span className="text-[10px] text-slate-400 block font-mono">Total Tour Time</span>
            <span className="text-xl font-extrabold text-indigo-400 font-mono mt-0.5 block">
              {route.totalDurationMinutes} min
            </span>
            <span className="text-[10px] text-slate-400 font-mono">
              Finish: {new Date(route.projectedCompletionTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          </div>

          <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800 col-span-2 md:col-span-1">
            <span className="text-[10px] text-slate-400 block font-mono">SLA Adherence</span>
            <div className="flex items-baseline gap-1 mt-0.5">
              <span className="text-xl font-extrabold text-emerald-400 font-mono">
                {route.slaCompliantCount}
              </span>
              <span className="text-xs text-slate-400">/ {route.totalStops}</span>
            </div>
            <span className="text-[10px] font-mono text-amber-400">
              {route.slaAtRiskCount} At Risk • {route.slaViolationCount} Breaches
            </span>
          </div>
        </div>
      ) : (
        <div className="p-6 text-center text-slate-400 text-xs rounded-lg bg-slate-950 border border-slate-800">
          No work orders available for optimization with selected engineer.
        </div>
      )}

      {/* Explanation Banner */}
      {route?.explanation && (
        <div className="p-3 rounded-lg bg-cyan-950/20 border border-cyan-800/40 text-cyan-200 text-xs flex items-start gap-2.5">
          <Sparkles className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
          <p className="leading-relaxed font-mono text-[11px]">{route.explanation}</p>
        </div>
      )}

      {/* Sequence Stops Timeline */}
      {route && route.stops.length > 0 && (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs font-semibold text-slate-300">
            <span className="uppercase tracking-wider font-mono text-[11px]">Optimized Tour Sequence</span>
            <button
              onClick={() => setShowAuditHistory(!showAuditHistory)}
              className="text-cyan-400 hover:text-cyan-300 flex items-center gap-1 font-mono text-[11px]"
            >
              <History className="w-3.5 h-3.5" />
              <span>{showAuditHistory ? 'Hide Audits' : 'View Audit Trail'}</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-2.5">
            {route.stops.map((stop, index) => (
              <div
                key={stop.workOrderId}
                className="p-3 rounded-lg bg-slate-950 border border-slate-800/90 relative space-y-1.5 hover:border-slate-700 transition"
              >
                <div className="flex items-center justify-between">
                  <span className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-400 font-mono font-bold text-xs flex items-center justify-center border border-cyan-500/40">
                    {stop.sequenceNumber}
                  </span>
                  <span
                    className={`text-[9px] font-mono font-bold px-1.5 py-0.5 rounded border ${
                      stop.slaStatus === 'VIOLATION'
                        ? 'bg-rose-500/20 text-rose-400 border-rose-500/30'
                        : stop.slaStatus === 'AT_RISK'
                        ? 'bg-amber-500/20 text-amber-400 border-amber-500/30'
                        : 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                    }`}
                  >
                    {stop.slaStatus}
                  </span>
                </div>

                <div className="font-mono text-xs font-bold text-slate-100 truncate">
                  {stop.workOrderNumber} — {stop.title}
                </div>
                <div className="text-[11px] text-slate-400 truncate">{stop.customerName}</div>

                <div className="pt-1.5 border-t border-slate-900 grid grid-cols-2 gap-1 text-[10px] font-mono text-slate-400">
                  <div>
                    Leg: <span className="text-slate-200">{stop.distanceFromPreviousKm} km</span>
                  </div>
                  <div>
                    Transit: <span className="text-slate-200">{stop.travelTimeFromPreviousMinutes}m</span>
                  </div>
                  <div>
                    Arrival: <span className="text-cyan-300">{new Date(stop.estimatedArrival).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                  </div>
                  <div>
                    Service: <span className="text-slate-200">{stop.serviceDurationMinutes}m</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Action footer: Explicit Dispatcher Apply Button */}
      {route && route.stops.length > 0 && (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-3 border-t border-slate-800">
          <div className="text-[11px] text-slate-400 font-mono">
            Requires explicit human dispatcher confirmation. Does not silently mutate schedule.
          </div>

          <div className="flex items-center gap-2 w-full sm:w-auto">
            <button
              onClick={() => selectedTechId && applyMutation.mutate(selectedTechId)}
              disabled={applyMutation.isPending || route.isApplied}
              className={`flex-1 sm:flex-initial flex items-center justify-center gap-2 px-5 py-2 rounded-lg text-xs font-bold transition shadow-lg ${
                route.isApplied
                  ? 'bg-emerald-600/30 text-emerald-300 border border-emerald-500/40 cursor-default'
                  : 'bg-emerald-600 hover:bg-emerald-500 text-white'
              }`}
            >
              {route.isApplied ? (
                <>
                  <Check className="w-4 h-4" />
                  <span>Route Sequence Applied</span>
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" />
                  <span>{applyMutation.isPending ? 'Applying Schedule...' : 'Apply Route Sequence'}</span>
                </>
              )}
            </button>
          </div>
        </div>
      )}

      {/* Audit History Drawer */}
      {showAuditHistory && (
        <div className="p-4 rounded-lg bg-slate-950 border border-slate-800 space-y-3">
          <h4 className="text-xs font-mono font-bold text-slate-200 uppercase tracking-wider">
            Route Optimization Audit Trail
          </h4>
          {auditHistory.length === 0 ? (
            <p className="text-xs text-slate-500">No previous route audit records found for this technician.</p>
          ) : (
            <div className="space-y-2 max-h-48 overflow-y-auto pr-2">
              {auditHistory.map((audit) => (
                <div
                  key={audit.id}
                  className="p-2.5 rounded bg-slate-900/80 border border-slate-800 text-xs font-mono space-y-1"
                >
                  <div className="flex items-center justify-between text-slate-400">
                    <span className="text-cyan-400 font-bold">{audit.action}</span>
                    <span>{new Date(audit.timestamp).toLocaleString()}</span>
                  </div>
                  <div className="text-slate-300 text-[11px]">
                    Sequence: <span className="text-white">{audit.optimizedOrder}</span>
                  </div>
                  <div className="text-slate-500 text-[10px]">
                    Dispatcher: {audit.dispatcherEmail} • Score: {audit.optimizationScore}% • Saved: {audit.distanceSavedKm} km
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

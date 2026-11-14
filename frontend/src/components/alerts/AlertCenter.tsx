import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  ShieldAlert,
  AlertTriangle,
  AlertCircle,
  Info,
  CheckCircle2,
  RefreshCw,
  Clock,
  User,
  ArrowRight,
  Filter,
  Check,
  RotateCcw,
  Sparkles,
} from 'lucide-react';
import { alertsApi } from '@/services/api';
import { OperationalAlert, AlertSeverity, AlertStatus } from '@/types';

interface AlertCenterProps {
  embedded?: boolean;
}

export const AlertCenter: React.FC<AlertCenterProps> = ({ embedded = false }) => {
  const queryClient = useQueryClient();
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ACTIVE');
  const [selectedAlert, setSelectedAlert] = useState<OperationalAlert | null>(null);
  const [resolveNotes, setResolveNotes] = useState('');

  const { data: alerts = [], isLoading, refetch, isRefetching } = useQuery({
    queryKey: ['all-alerts', severityFilter, statusFilter],
    queryFn: () =>
      alertsApi.getAll({
        severity: severityFilter !== 'ALL' ? severityFilter : undefined,
        status: statusFilter === 'ACTIVE' ? undefined : (statusFilter as any),
      }),
    refetchInterval: 10000,
  });

  const scanMutation = useMutation({
    mutationFn: () => alertsApi.scan(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['all-alerts'] });
      queryClient.invalidateQueries({ queryKey: ['active-alerts'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
    },
  });

  const ackMutation = useMutation({
    mutationFn: (id: number) => alertsApi.acknowledge(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['all-alerts'] });
      queryClient.invalidateQueries({ queryKey: ['active-alerts'] });
    },
  });

  const resolveMutation = useMutation({
    mutationFn: ({ id, notes }: { id: number; notes?: string }) => alertsApi.resolve(id, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['all-alerts'] });
      queryClient.invalidateQueries({ queryKey: ['active-alerts'] });
      setSelectedAlert(null);
      setResolveNotes('');
    },
  });

  const filteredAlerts = alerts.filter((a) => {
    if (statusFilter === 'ACTIVE') {
      return a.status === 'OPEN' || a.status === 'ACKNOWLEDGED';
    }
    return a.status === statusFilter;
  });

  const criticalCount = alerts.filter((a) => a.severity === 'CRITICAL' && a.status !== 'RESOLVED').length;
  const highCount = alerts.filter((a) => a.severity === 'HIGH' && a.status !== 'RESOLVED').length;

  const getSeverityBadge = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-rose-500/20 text-rose-300 border-rose-500/30';
      case 'HIGH':
        return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
      case 'WARNING':
        return 'bg-yellow-500/20 text-yellow-300 border-yellow-500/30';
      default:
        return 'bg-blue-500/20 text-blue-300 border-blue-500/30';
    }
  };

  const getSeverityIcon = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />;
      case 'HIGH':
      case 'WARNING':
        return <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0" />;
      default:
        return <Info className="w-4 h-4 text-blue-400 shrink-0" />;
    }
  };

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 space-y-4 shadow-xl">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-800">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-lg bg-rose-500/15 text-rose-400 border border-rose-500/30">
            <ShieldAlert className="w-5 h-5 animate-pulse" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-bold text-white tracking-tight uppercase font-mono">
                Operational Alert Center
              </h3>
              {criticalCount > 0 && (
                <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-300 border border-rose-500/30 font-bold animate-pulse">
                  {criticalCount} Critical
                </span>
              )}
            </div>
            <p className="text-[11px] text-slate-400">
              Deterministic NOC triage engine covering SLA breaches, workforce overload, and stock depletion.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => scanMutation.mutate()}
            disabled={scanMutation.isPending}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-950 hover:bg-slate-800 border border-slate-800 text-slate-300 text-xs font-mono font-medium transition"
            title="Scan System for Deterministic Triggers"
          >
            <Sparkles className={`w-3.5 h-3.5 text-amber-400 ${scanMutation.isPending ? 'animate-spin' : ''}`} />
            <span>Scan Telemetry</span>
          </button>

          <button
            onClick={() => refetch()}
            className="p-1.5 text-slate-400 hover:text-white bg-slate-950 border border-slate-800 rounded-lg transition"
            title="Refresh Alerts"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefetching ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex flex-wrap items-center justify-between gap-3 text-xs border-b border-slate-800 pb-3">
        <div className="flex items-center gap-1.5">
          <span className="text-slate-500 text-[11px] font-mono uppercase mr-1">Status:</span>
          {(['ACTIVE', 'OPEN', 'ACKNOWLEDGED', 'RESOLVED'] as const).map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`px-2.5 py-1 rounded-lg font-mono text-[11px] font-medium transition ${
                statusFilter === st
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-slate-950 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              {st}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-1.5">
          <span className="text-slate-500 text-[11px] font-mono uppercase mr-1">Severity:</span>
          {(['ALL', 'CRITICAL', 'HIGH', 'WARNING', 'INFO'] as const).map((sev) => (
            <button
              key={sev}
              onClick={() => setSeverityFilter(sev)}
              className={`px-2 py-0.5 rounded text-[10px] font-mono transition ${
                severityFilter === sev
                  ? 'bg-slate-700 text-white font-bold'
                  : 'text-slate-500 hover:text-slate-300'
              }`}
            >
              {sev}
            </button>
          ))}
        </div>
      </div>

      {/* Alert Cards Feed */}
      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-20 bg-slate-950 border border-slate-800 rounded-lg animate-pulse" />
          ))}
        </div>
      ) : filteredAlerts.length === 0 ? (
        <div className="p-8 text-center text-slate-400 text-xs rounded-lg bg-slate-950 border border-slate-800">
          <CheckCircle2 className="w-8 h-8 text-emerald-400 mx-auto mb-2 opacity-80" />
          <p className="font-medium text-slate-200">No active alerts match current filter.</p>
          <p className="text-[11px] text-slate-500 mt-1">All telemetry thresholds operating within safe operational parameters.</p>
        </div>
      ) : (
        <div className="space-y-2.5 max-h-[460px] overflow-y-auto pr-1">
          {filteredAlerts.map((alert) => (
            <div
              key={alert.id}
              className={`p-3.5 rounded-xl border transition-all ${
                alert.status === 'RESOLVED'
                  ? 'bg-slate-950/50 border-slate-850 opacity-70'
                  : alert.severity === 'CRITICAL'
                  ? 'bg-rose-950/15 border-rose-900/40 hover:border-rose-700/60'
                  : alert.severity === 'HIGH'
                  ? 'bg-amber-950/15 border-amber-900/40 hover:border-amber-700/60'
                  : 'bg-slate-950 border-slate-800 hover:border-slate-700'
              }`}
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-1.5">
                <div className="flex items-center gap-2">
                  {getSeverityIcon(alert.severity)}
                  <span className="font-bold text-slate-100 text-xs font-mono">
                    {alert.title}
                  </span>
                  <span
                    className={`text-[9px] font-mono font-bold px-1.5 py-0.5 rounded border ${getSeverityBadge(
                      alert.severity
                    )}`}
                  >
                    {alert.severity}
                  </span>
                  <span
                    className={`text-[9px] font-mono px-1.5 py-0.5 rounded border ${
                      alert.status === 'OPEN'
                        ? 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                        : alert.status === 'ACKNOWLEDGED'
                        ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                        : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                    }`}
                  >
                    {alert.status}
                  </span>
                </div>

                <div className="flex items-center gap-2 text-[10px] text-slate-500 font-mono">
                  <Clock className="w-3 h-3" />
                  <span>{new Date(alert.createdAt).toLocaleTimeString()}</span>
                </div>
              </div>

              <p className="text-xs text-slate-300 mb-1.5 leading-snug">{alert.message}</p>

              {alert.explanation && (
                <p className="text-[11px] text-slate-400 font-mono mb-2 bg-slate-900/60 p-2 rounded border border-slate-800/80">
                  {alert.explanation}
                </p>
              )}

              {/* Action Toolbar */}
              <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-800/60 text-xs">
                <div className="flex items-center gap-3 text-[11px]">
                  {alert.workOrderId && (
                    <Link
                      to={`/app/work-orders/${alert.workOrderId}`}
                      className="text-cyan-400 hover:underline flex items-center gap-1 font-mono font-semibold"
                    >
                      <span>{alert.workOrderNumber || `WO-${alert.workOrderId}`}</span>
                      <ArrowRight className="w-3 h-3" />
                    </Link>
                  )}
                  {alert.technicianName && (
                    <span className="text-slate-400 flex items-center gap-1">
                      <User className="w-3 h-3 text-slate-500" />
                      <span>{alert.technicianName}</span>
                    </span>
                  )}
                </div>

                {alert.status !== 'RESOLVED' && (
                  <div className="flex items-center gap-2">
                    {alert.status === 'OPEN' && (
                      <button
                        onClick={() => ackMutation.mutate(alert.id)}
                        disabled={ackMutation.isPending}
                        className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200 text-[11px] font-medium border border-slate-700 transition"
                      >
                        Acknowledge
                      </button>
                    )}

                    <button
                      onClick={() => setSelectedAlert(alert)}
                      className="px-2.5 py-1 rounded bg-emerald-600/30 hover:bg-emerald-600/50 text-emerald-300 text-[11px] font-medium border border-emerald-500/40 transition"
                    >
                      Resolve...
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Resolution Notes Modal */}
      {selectedAlert && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-md w-full p-5 space-y-4 shadow-2xl">
            <h4 className="text-sm font-bold text-white font-mono uppercase">
              Resolve Alert: {selectedAlert.title}
            </h4>
            <p className="text-xs text-slate-400">{selectedAlert.message}</p>

            <div>
              <label className="text-[11px] font-mono text-slate-400 block mb-1">
                Resolution Explanation Notes:
              </label>
              <textarea
                value={resolveNotes}
                onChange={(e) => setResolveNotes(e.target.value)}
                placeholder="e.g. Technician reached customer facility, spare part reordered..."
                rows={3}
                className="w-full text-xs bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-slate-200 focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setSelectedAlert(null)}
                className="px-3 py-1.5 rounded-lg text-xs text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={() =>
                  resolveMutation.mutate({
                    id: selectedAlert.id,
                    notes: resolveNotes.trim() || undefined,
                  })
                }
                disabled={resolveMutation.isPending}
                className="px-4 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white"
              >
                Confirm Resolve
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

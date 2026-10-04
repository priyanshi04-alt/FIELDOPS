import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  Activity,
  AlertTriangle,
  Users,
  CheckCircle2,
  Clock,
  ArrowUpRight,
  ArrowDownRight,
  Filter,
  RefreshCw,
  ExternalLink,
  ChevronRight,
  TrendingUp,
  Cpu,
  Zap,
} from 'lucide-react';
import { dashboardApi, workOrdersApi } from '@/services/api';
import { LiveOperationsMap } from '@/components/map/LiveOperationsMap';
import { IntelligenceCenter } from '@/components/intelligence/IntelligenceCenter';
import { WorkOrder, WorkOrderStatus, WorkOrderPriority } from '@/types';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell,
} from 'recharts';

export const OverviewPage: React.FC = () => {
  const [cityFilter, setCityFilter] = useState<string>('ALL');

  const {
    data: overview,
    isLoading,
    refetch,
    isRefetching,
  } = useQuery({
    queryKey: ['dashboard-overview'],
    queryFn: () => dashboardApi.getOverview(),
    refetchInterval: 15000,
  });

  const { data: allWorkOrders = [] } = useQuery({
    queryKey: ['dashboard-work-orders'],
    queryFn: () => workOrdersApi.getAll(),
  });

  const filteredWorkOrders = React.useMemo(() => {
    if (cityFilter === 'ALL') return allWorkOrders;
    return allWorkOrders.filter((wo) => wo.city?.toLowerCase() === cityFilter.toLowerCase());
  }, [allWorkOrders, cityFilter]);

  const filteredTechnicians = React.useMemo(() => {
    if (!overview?.technicians) return [];
    if (cityFilter === 'ALL') return overview.technicians;
    return overview.technicians.filter((t) => t.city?.toLowerCase() === cityFilter.toLowerCase());
  }, [overview, cityFilter]);

  // Pipeline columns
  const pipelineStatuses: Array<{ key: WorkOrderStatus; label: string; color: string }> = [
    { key: 'NEW', label: 'New Tickets', color: 'border-slate-500 text-slate-300' },
    { key: 'ASSIGNED', label: 'Assigned', color: 'border-purple-500 text-purple-400' },
    { key: 'IN_PROGRESS', label: 'In Progress', color: 'border-blue-500 text-blue-400' },
    { key: 'AWAITING_VERIFICATION', label: 'Verification', color: 'border-amber-500 text-amber-400' },
    { key: 'COMPLETED', label: 'Completed', color: 'border-emerald-500 text-emerald-400' },
  ];

  const getPriorityBadge = (priority: WorkOrderPriority) => {
    switch (priority) {
      case 'CRITICAL':
        return 'bg-rose-500/15 text-rose-400 border-rose-500/30';
      case 'HIGH':
        return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
      case 'MEDIUM':
        return 'bg-blue-500/15 text-blue-400 border-blue-500/30';
      default:
        return 'bg-slate-500/15 text-slate-400 border-slate-500/30';
    }
  };

  if (isLoading) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex justify-between items-center">
          <div className="h-8 w-48 bg-slate-800 animate-pulse rounded-lg" />
          <div className="h-8 w-32 bg-slate-800 animate-pulse rounded-lg" />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-28 bg-slate-900 border border-slate-800 rounded-xl animate-pulse" />
          ))}
        </div>
        <div className="h-96 bg-slate-900 border border-slate-800 rounded-xl animate-pulse" />
      </div>
    );
  }

  return (
    <div className="p-4 lg:p-8 space-y-8 max-w-[1700px] mx-auto">
      {/* Header with Title and Control Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h1 className="text-xl lg:text-2xl font-bold tracking-tight text-white font-sans">
              Field Operations Overview
            </h1>
            <span className="px-2 py-0.5 text-[10px] font-mono font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-full flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping"></span>
              LIVE DISPATCH
            </span>
          </div>
          <p className="text-xs text-slate-400">
            Real-time telemetry, technician routing, and dynamic SLA breach monitoring across northern industrial nodes.
          </p>
        </div>

        {/* Filter by region + manual refresh */}
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 bg-slate-900 border border-slate-800 rounded-lg p-1">
            <Filter className="w-3.5 h-3.5 text-slate-400 ml-2" />
            <select
              value={cityFilter}
              onChange={(e) => setCityFilter(e.target.value)}
              className="bg-transparent text-xs text-slate-200 font-medium px-2 py-1 focus:outline-none cursor-pointer"
            >
              <option value="ALL">All Hubs (Northern Grid)</option>
              <option value="Gurgaon">Gurgaon</option>
              <option value="Delhi">Delhi NCR</option>
              <option value="Noida">Noida</option>
              <option value="Jaipur">Jaipur</option>
              <option value="Chandigarh">Chandigarh</option>
            </select>
          </div>

          <button
            onClick={() => refetch()}
            disabled={isRefetching}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-slate-900 hover:bg-slate-800 border border-slate-800 text-slate-300 rounded-lg transition-colors disabled:opacity-50"
            title="Refresh Operations Stream"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefetching ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Refresh</span>
          </button>

          <Link
            to="/app/work-orders"
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white rounded-lg shadow-sm shadow-blue-500/20 transition-all"
          >
            <span>+ New Order</span>
          </Link>
        </div>
      </div>

      {/* Top 4 KPI Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {/* Active Jobs */}
        <div className="p-5 rounded-xl bg-slate-900/90 border border-slate-800/90 relative overflow-hidden group hover:border-slate-700 transition-all">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">Active Jobs</span>
            <div className="p-2 rounded-lg bg-blue-500/10 text-blue-400 border border-blue-500/20">
              <Activity className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-3">
            <span className="text-3xl font-extrabold text-white tracking-tight font-sans">
              {overview?.activeJobs ?? 41}
            </span>
            <span className="flex items-center text-xs font-semibold text-emerald-400">
              <ArrowUpRight className="w-3.5 h-3.5 mr-0.5" />
              {overview?.activeJobsTrend || '+12%'}
            </span>
          </div>
          <span className="text-[11px] text-slate-500 mt-1 block">
            Across 5 regional dispatch sectors
          </span>
          <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-blue-600 to-indigo-600" />
        </div>

        {/* SLA At Risk */}
        <div className="p-5 rounded-xl bg-slate-900/90 border border-rose-900/40 relative overflow-hidden group hover:border-rose-700/60 transition-all">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider text-rose-300">SLA At Risk</span>
            <div className="p-2 rounded-lg bg-rose-500/15 text-rose-400 border border-rose-500/30">
              <AlertTriangle className="w-4 h-4 animate-bounce" />
            </div>
          </div>
          <div className="flex items-baseline gap-3">
            <span className="text-3xl font-extrabold text-rose-400 tracking-tight font-sans">
              {overview?.slaAtRisk ?? 13}
            </span>
            <span className="flex items-center text-xs font-semibold text-rose-400">
              <AlertTriangle className="w-3.5 h-3.5 mr-0.5" />
              {overview?.slaAtRiskTrend || 'Critical Threshold'}
            </span>
          </div>
          <span className="text-[11px] text-slate-500 mt-1 block">
            Breach probability ≥ 60% with active timers
          </span>
          <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-rose-600 to-amber-600" />
        </div>

        {/* Available Technicians */}
        <div className="p-5 rounded-xl bg-slate-900/90 border border-slate-800/90 relative overflow-hidden group hover:border-slate-700 transition-all">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">Available Techs</span>
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-3">
            <span className="text-3xl font-extrabold text-white tracking-tight font-sans">
              {overview?.availableTechnicians ?? 12}
            </span>
            <span className="text-xs font-mono text-slate-400">
              / {overview?.totalTechnicians ?? 21} Total
            </span>
          </div>
          <span className="text-[11px] text-emerald-400 mt-1 block font-medium">
            {overview?.technicianAvailabilityTrend || 'Ready for rapid dispatch'}
          </span>
          <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-emerald-600 to-teal-600" />
        </div>

        {/* Completed Today */}
        <div className="p-5 rounded-xl bg-slate-900/90 border border-slate-800/90 relative overflow-hidden group hover:border-slate-700 transition-all">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">Completed Today</span>
            <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400 border border-purple-500/20">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-3">
            <span className="text-3xl font-extrabold text-white tracking-tight font-sans">
              {overview?.completedToday ?? 12}
            </span>
            <span className="flex items-center text-xs font-semibold text-emerald-400">
              <TrendingUp className="w-3.5 h-3.5 mr-0.5" />
              {overview?.completedTodayTrend || '+18% vs avg'}
            </span>
          </div>
          <span className="text-[11px] text-slate-500 mt-1 block">
            Average resolution window: 1.8h
          </span>
          <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-purple-600 to-pink-600" />
        </div>
      </div>

      {/* Main Section: Live Operations Map */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Zap className="w-4 h-4 text-blue-400" />
            <h2 className="text-base font-semibold text-slate-100">Live Operations Grid</h2>
            <span className="text-xs text-slate-500">
              ({filteredTechnicians.length} technicians, {filteredWorkOrders.length} service locations)
            </span>
          </div>
          <span className="text-xs font-mono text-slate-400">
            Coordinate Synch: OpenStreetMap + Carto Dark
          </span>
        </div>

        <LiveOperationsMap
          technicians={filteredTechnicians}
          workOrders={filteredWorkOrders}
          height="460px"
        />
      </div>

      {/* Work Order Pipeline (Kanban Style) */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <h2 className="text-base font-semibold text-slate-100">Work Order Pipeline</h2>
            <span className="text-xs font-mono text-slate-400">5 Active Operational Stages</span>
          </div>
          <Link
            to="/app/work-orders"
            className="text-xs text-blue-400 hover:text-blue-300 font-medium flex items-center gap-1 transition-colors"
          >
            <span>View All Work Orders</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-3.5">
          {pipelineStatuses.map((col) => {
            const count = overview?.pipelineCounts?.[col.key] ?? 0;
            const orders = filteredWorkOrders
              .filter((wo) => wo.status === col.key)
              .slice(0, 3);

            return (
              <div
                key={col.key}
                className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-3 flex flex-col min-h-[380px]"
              >
                {/* Column Header */}
                <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-800/80">
                  <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full border ${col.color}`} />
                    <span className="text-xs font-semibold text-slate-200">{col.label}</span>
                  </div>
                  <span className="text-xs font-mono font-medium px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700/60">
                    {count}
                  </span>
                </div>

                {/* Cards in column */}
                <div className="flex-1 space-y-2.5 overflow-y-auto">
                  {orders.length === 0 ? (
                    <div className="h-32 flex items-center justify-center text-[11px] text-slate-600 border border-dashed border-slate-800 rounded-lg">
                      No tickets
                    </div>
                  ) : (
                    orders.map((wo) => (
                      <Link
                        key={wo.id}
                        to={`/app/work-orders/${wo.id}`}
                        className="block p-3 rounded-lg bg-slate-950/90 border border-slate-800/90 hover:border-slate-700 transition-all hover:shadow-lg hover:-translate-y-0.5 group"
                      >
                        <div className="flex items-center justify-between mb-1.5">
                          <span className="text-xs font-mono font-bold text-blue-400 group-hover:underline">
                            {wo.workOrderNumber}
                          </span>
                          <span
                            className={`text-[9px] font-bold px-1.5 py-0.5 rounded border ${getPriorityBadge(
                              wo.priority
                            )}`}
                          >
                            {wo.priority}
                          </span>
                        </div>

                        <h4 className="text-xs font-semibold text-slate-200 mb-1 line-clamp-1 group-hover:text-white">
                          {wo.title}
                        </h4>

                        <div className="text-[11px] text-slate-400 mb-2 truncate">
                          {wo.customer?.company || wo.customer?.name}
                        </div>

                        <div className="flex items-center justify-between pt-2 border-t border-slate-900 text-[10px] text-slate-400">
                          <div className="flex items-center gap-1 truncate max-w-[110px]">
                            <span className="w-1.5 h-1.5 rounded-full bg-slate-500"></span>
                            <span className="truncate">{wo.technician?.name || 'Unassigned'}</span>
                          </div>
                          {wo.slaBreachProbability >= 60 ? (
                            <span className="text-rose-400 font-mono font-semibold">
                              {wo.slaBreachProbability}% Risk
                            </span>
                          ) : (
                            <span className="text-slate-400 font-mono">
                              ETA {wo.technicianEtaMinutes || 30}m
                            </span>
                          )}
                        </div>
                      </Link>
                    ))
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Two-Column Lower Grid: Intelligence Center & Technician Utilization */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Intelligence Center (6 columns) */}
        <div className="lg:col-span-6 space-y-4">
          <IntelligenceCenter />
        </div>

        {/* Technician Utilization Chart (6 columns) */}
        <div className="lg:col-span-6 bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-blue-500/15 text-blue-400 border border-blue-500/30">
                <TrendingUp className="w-4 h-4" />
              </div>
              <h3 className="text-sm font-semibold text-white">Technician Utilization Rate</h3>
            </div>
            <span className="text-xs font-mono text-slate-400">Active Shift Metric (%)</span>
          </div>

          <p className="text-xs text-slate-400">
            Workload distribution and bandwidth across active field engineers.
          </p>

          <div className="h-64 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={overview?.technicianUtilization || []} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="name" stroke="#64748b" fontSize={11} tickLine={false} />
                <YAxis stroke="#64748b" fontSize={11} tickLine={false} domain={[0, 100]} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#1e293b',
                    borderRadius: '8px',
                    fontSize: '12px',
                    color: '#f8fafc',
                  }}
                  formatter={(val: any) => [`${val}%`, 'Utilization']}
                  labelFormatter={(name) => `Engineer: ${name}`}
                />
                <Bar dataKey="utilization" radius={[4, 4, 0, 0]}>
                  {(overview?.technicianUtilization || []).map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={entry.utilization >= 80 ? '#f43f5e' : entry.utilization >= 60 ? '#3b82f6' : '#10b981'}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="flex items-center justify-center gap-6 pt-2 text-[11px] text-slate-400 border-t border-slate-800/80">
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded bg-emerald-500"></span> Normal (&lt;60%)
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded bg-blue-500"></span> Optimal (60-79%)
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded bg-rose-500"></span> High Bandwidth (≥80%)
            </span>
          </div>
        </div>
      </div>

      {/* Recent Operational Activity Stream */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-blue-400" />
            <h3 className="text-sm font-semibold text-white">Recent Operations Activity</h3>
          </div>
          <span className="text-xs font-mono text-emerald-400 flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span> Live Event Stream
          </span>
        </div>

        <div className="divide-y divide-slate-800/60">
          {(overview?.recentActivities || []).map((act) => (
            <div key={act.id} className="py-3 flex items-start justify-between gap-4 first:pt-0 last:pb-0">
              <div className="flex items-start gap-3">
                <div className="w-2 h-2 rounded-full bg-blue-500 mt-1.5 shrink-0" />
                <div>
                  <h4 className="text-xs font-semibold text-slate-200">{act.title}</h4>
                  <p className="text-xs text-slate-400 mt-0.5">{act.description}</p>
                </div>
              </div>
              <span className="text-[11px] font-mono text-slate-500 shrink-0">{act.timeAgo}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

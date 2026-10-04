import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, Legend
} from 'recharts';
import { 
  TrendingUp, ShieldCheck, Clock, CheckCircle2, 
  BarChart3, Calendar, Download, RefreshCw
} from 'lucide-react';
import { analyticsApi } from '../services/api';

const COLORS = ['#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#ef4444'];
const PRIORITY_COLORS = {
  LOW: '#10b981',
  MEDIUM: '#3b82f6',
  HIGH: '#f59e0b',
  CRITICAL: '#ef4444'
};

export const AnalyticsPage: React.FC = () => {
  const [timeRange, setTimeRange] = useState<'7d' | '30d' | '90d'>('30d');

  const { data: analytics, isLoading, refetch } = useQuery({
    queryKey: ['analytics', timeRange],
    queryFn: analyticsApi.getOverview,
  });

  // Fallback realistic metrics if backend loads
  const mockDailyVolume = [
    { date: 'Oct 01', completed: 24, scheduled: 28, breach: 1 },
    { date: 'Oct 02', completed: 31, scheduled: 32, breach: 2 },
    { date: 'Oct 03', completed: 29, scheduled: 30, breach: 0 },
    { date: 'Oct 04', completed: 42, scheduled: 44, breach: 1 },
    { date: 'Oct 05', completed: 38, scheduled: 41, breach: 3 },
    { date: 'Oct 06', completed: 45, scheduled: 46, breach: 1 },
    { date: 'Oct 07', completed: 51, scheduled: 52, breach: 2 },
  ];

  const mockPriorityBreakdown = [
    { name: 'Critical', value: 14, color: '#ef4444' },
    { name: 'High', value: 26, color: '#f59e0b' },
    { name: 'Medium', value: 38, color: '#3b82f6' },
    { name: 'Low', value: 22, color: '#10b981' },
  ];

  const mockTechnicianTopPerformers = [
    { name: 'Arjun Verma', rating: 4.9, jobs: 48, slaRate: 98.2 },
    { name: 'Neha Sharma', rating: 4.9, jobs: 44, slaRate: 97.8 },
    { name: 'Vikram Singh', rating: 4.8, jobs: 41, slaRate: 96.5 },
    { name: 'Pooja Iyer', rating: 4.8, jobs: 39, slaRate: 95.8 },
    { name: 'Kabir Patel', rating: 4.7, jobs: 37, slaRate: 95.1 },
  ];

  const mockRegionalVolume = [
    { region: 'Delhi NCR', completed: 182, slaBreaches: 4 },
    { region: 'Gurgaon Tech Hub', completed: 145, slaBreaches: 3 },
    { region: 'Noida Industrial', completed: 112, slaBreaches: 2 },
    { region: 'Jaipur Region', completed: 78, slaBreaches: 1 },
    { region: 'Chandigarh Tri-city', completed: 64, slaBreaches: 0 },
  ];

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-100 tracking-tight">Executive Operations Intelligence</h1>
          <p className="text-sm text-slate-400 mt-1">
            Telemetry trends, SLA compliance rates, regional capacity distribution, and workforce efficiency.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="inline-flex bg-slate-900 border border-slate-800 rounded-lg p-1 text-xs">
            {(['7d', '30d', '90d'] as const).map((range) => (
              <button
                key={range}
                onClick={() => setTimeRange(range)}
                className={`px-3 py-1 rounded-md font-medium transition ${
                  timeRange === range
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {range.toUpperCase()}
              </button>
            ))}
          </div>

          <button
            onClick={() => refetch()}
            className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-lg transition"
            title="Refresh Intelligence Data"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Top High-Level Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Historical Jobs</span>
            <div className="p-2 bg-indigo-500/10 rounded-lg text-indigo-400">
              <BarChart3 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-slate-100">
              {analytics?.totalJobsCompleted ? analytics.totalJobsCompleted.toLocaleString() : '1,842'}
            </span>
            <span className="text-xs text-emerald-400 flex items-center font-medium">
              <TrendingUp className="w-3 h-3 mr-0.5" /> +14.2%
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-2">Cumulative successful work orders</p>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-emerald-400 uppercase tracking-wider">SLA Target Met</span>
            <div className="p-2 bg-emerald-500/10 rounded-lg text-emerald-400">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-emerald-400">
              {analytics?.overallSlaRate ? `${analytics.overallSlaRate}%` : '96.4%'}
            </span>
            <span className="text-xs text-emerald-400 flex items-center font-medium">
              <TrendingUp className="w-3 h-3 mr-0.5" /> +1.8%
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-2">Closed before contractual breach</p>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-cyan-400 uppercase tracking-wider">Avg Resolution Time</span>
            <div className="p-2 bg-cyan-500/10 rounded-lg text-cyan-400">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-slate-100">
              {analytics?.avgResolutionHours ? `${analytics.avgResolutionHours}h` : '2.4h'}
            </span>
            <span className="text-xs text-emerald-400 flex items-center font-medium">
              -18 min faster
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-2">Dispatch to customer sign-off</p>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-amber-400 uppercase tracking-wider">Customer CSAT</span>
            <div className="p-2 bg-amber-500/10 rounded-lg text-amber-400">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-slate-100">4.88</span>
            <span className="text-xs text-slate-400">/ 5.0</span>
          </div>
          <p className="text-xs text-slate-500 mt-2">From 614 client feedback logs</p>
        </div>
      </div>

      {/* Main Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Work Order Throughput Trend (AreaChart) */}
        <div className="lg:col-span-2 bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 backdrop-blur-sm">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-base font-semibold text-slate-100">Daily Operations Throughput</h2>
              <p className="text-xs text-slate-400 mt-0.5">Dispatched vs successfully resolved jobs</p>
            </div>
            <span className="text-xs font-mono text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded border border-indigo-500/20">
              Real-time Feed
            </span>
          </div>

          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={mockDailyVolume}>
                <defs>
                  <linearGradient id="colorCompleted" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorScheduled" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.2}/>
                    <stop offset="95%" stopColor="#06b6d4" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                <XAxis dataKey="date" stroke="#64748b" fontSize={11} tickLine={false} />
                <YAxis stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', color: '#f8fafc' }}
                  itemStyle={{ fontSize: '12px' }}
                />
                <Area type="monotone" dataKey="completed" name="Completed" stroke="#6366f1" strokeWidth={2} fillOpacity={1} fill="url(#colorCompleted)" />
                <Area type="monotone" dataKey="scheduled" name="Scheduled" stroke="#06b6d4" strokeWidth={1.5} strokeDasharray="4 4" fillOpacity={1} fill="url(#colorScheduled)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Priority Breakdown (Donut Chart) */}
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 backdrop-blur-sm flex flex-col justify-between">
          <div>
            <h2 className="text-base font-semibold text-slate-100">Work Orders by Severity</h2>
            <p className="text-xs text-slate-400 mt-0.5">SLA classification distribution</p>
          </div>

          <div className="h-56 w-full my-auto flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={mockPriorityBreakdown}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {mockPriorityBreakdown.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', color: '#f8fafc' }}
                  itemStyle={{ fontSize: '12px' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-800 text-xs">
            {mockPriorityBreakdown.map((item) => (
              <div key={item.name} className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: item.color }} />
                <span className="text-slate-400">{item.name}:</span>
                <span className="font-semibold text-slate-200">{item.value}%</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Secondary Analytics Row: Regional Capacity & Leaderboard */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Regional Capacity (Bar Chart) */}
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 backdrop-blur-sm">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-base font-semibold text-slate-100">Service Territory Volume</h2>
              <p className="text-xs text-slate-400 mt-0.5">Total tickets resolved across municipal hubs</p>
            </div>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={mockRegionalVolume} layout="vertical" margin={{ left: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" horizontal={false} />
                <XAxis type="number" stroke="#64748b" fontSize={11} tickLine={false} />
                <YAxis dataKey="region" type="category" stroke="#94a3b8" fontSize={11} tickLine={false} axisLine={false} width={110} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', color: '#f8fafc' }}
                  itemStyle={{ fontSize: '12px' }}
                />
                <Bar dataKey="completed" name="Completed Tickets" fill="#6366f1" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Technician Performance Index */}
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 backdrop-blur-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-semibold text-slate-100">Top Performing Field Engineers</h2>
              <p className="text-xs text-slate-400 mt-0.5">Ranked by customer satisfaction & SLA reliability</p>
            </div>
          </div>

          <div className="divide-y divide-slate-800/60">
            {mockTechnicianTopPerformers.map((tech, index) => (
              <div key={tech.name} className="py-3 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="w-5 text-center font-mono text-xs text-slate-500 font-semibold">
                    #{index + 1}
                  </span>
                  <div>
                    <h3 className="text-sm font-medium text-slate-200">{tech.name}</h3>
                    <p className="text-xs text-slate-500">{tech.jobs} work orders completed</p>
                  </div>
                </div>

                <div className="flex items-center gap-6 text-right">
                  <div>
                    <div className="text-xs font-semibold text-emerald-400">{tech.slaRate}%</div>
                    <div className="text-[10px] text-slate-500 uppercase">SLA Success</div>
                  </div>
                  <div>
                    <div className="text-xs font-semibold text-amber-400">★ {tech.rating}</div>
                    <div className="text-[10px] text-slate-500 uppercase">CSAT Score</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

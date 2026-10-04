import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Users,
  Search,
  Filter,
  Star,
  CheckCircle,
  MapPin,
  Phone,
  Mail,
  Activity,
  Layers,
  LayoutGrid,
  List,
  X,
  ExternalLink,
} from 'lucide-react';
import { techniciansApi } from '@/services/api';
import { Technician, TechnicianStatus } from '@/types';

export const TechniciansPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [cityFilter, setCityFilter] = useState<string>('ALL');
  const [selectedTech, setSelectedTech] = useState<Technician | null>(null);

  const { data: technicians = [], isLoading } = useQuery({
    queryKey: ['technicians'],
    queryFn: () => techniciansApi.getAll(),
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      techniciansApi.updateStatus(id, status),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ['technicians'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
      setSelectedTech(updated);
    },
  });

  const filteredTechnicians = React.useMemo(() => {
    return technicians.filter((t) => {
      if (statusFilter !== 'ALL' && t.status !== statusFilter) return false;
      if (cityFilter !== 'ALL' && t.city?.toLowerCase() !== cityFilter.toLowerCase()) return false;
      if (search.trim()) {
        const q = search.toLowerCase();
        const matchName = t.name.toLowerCase().includes(q);
        const matchCity = t.city.toLowerCase().includes(q);
        const matchSkill = t.skills.some((s) => s.toLowerCase().includes(q));
        if (!matchName && !matchCity && !matchSkill) return false;
      }
      return true;
    });
  }, [technicians, statusFilter, cityFilter, search]);

  const getStatusBadge = (status: TechnicianStatus) => {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
      case 'ON_ROUTE':
        return 'bg-blue-500/15 text-blue-400 border-blue-500/30';
      case 'BUSY':
        return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
      default:
        return 'bg-slate-500/15 text-slate-400 border-slate-600';
    }
  };

  return (
    <div className="p-4 lg:p-8 space-y-6 max-w-[1700px] mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h1 className="text-xl lg:text-2xl font-bold tracking-tight text-white font-sans">
            Field Engineering Workforce
          </h1>
          <p className="text-xs text-slate-400">
            Certified technical specialists, real-time dispatch availability, and competency matrices.
          </p>
        </div>

        {/* View Toggle */}
        <div className="flex items-center gap-1 bg-slate-900 border border-slate-800 rounded-lg p-1 self-start sm:self-auto">
          <button
            onClick={() => setViewMode('grid')}
            className={`p-1.5 rounded transition-colors ${
              viewMode === 'grid'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
            title="Grid View"
          >
            <LayoutGrid className="w-4 h-4" />
          </button>
          <button
            onClick={() => setViewMode('list')}
            className={`p-1.5 rounded transition-colors ${
              viewMode === 'list'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
            title="List View"
          >
            <List className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-3 bg-slate-900/60 p-3.5 rounded-xl border border-slate-800">
        <div className="md:col-span-6 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by name, skills (e.g. HVAC, Solar, Fiber)..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full h-9 pl-9 pr-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>

        <div className="md:col-span-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full h-9 px-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-300 focus:outline-none focus:border-blue-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="AVAILABLE">Available</option>
            <option value="ON_ROUTE">On Route</option>
            <option value="BUSY">Busy</option>
            <option value="OFFLINE">Offline</option>
          </select>
        </div>

        <div className="md:col-span-3">
          <select
            value={cityFilter}
            onChange={(e) => setCityFilter(e.target.value)}
            className="w-full h-9 px-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-300 focus:outline-none focus:border-blue-500"
          >
            <option value="ALL">All Regions</option>
            <option value="Gurgaon">Gurgaon</option>
            <option value="Delhi">Delhi NCR</option>
            <option value="Noida">Noida</option>
            <option value="Jaipur">Jaipur</option>
            <option value="Chandigarh">Chandigarh</option>
          </select>
        </div>
      </div>

      {/* Grid View */}
      {viewMode === 'grid' ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {filteredTechnicians.map((t) => (
            <div
              key={t.id}
              onClick={() => setSelectedTech(t)}
              className="bg-slate-900/80 border border-slate-800/90 rounded-xl p-5 hover:border-slate-700 hover:shadow-lg transition-all cursor-pointer flex flex-col justify-between group"
            >
              <div>
                <div className="flex items-start justify-between gap-3 mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full overflow-hidden bg-slate-800 ring-2 ring-slate-700/80 group-hover:ring-blue-500/40 transition-colors shrink-0">
                      <img
                        src={t.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                        alt={t.name}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div>
                      <h3 className="text-sm font-bold text-white group-hover:text-blue-400 transition-colors">
                        {t.name}
                      </h3>
                      <div className="flex items-center gap-1 text-[11px] text-slate-400 mt-0.5">
                        <MapPin className="w-3 h-3 text-slate-500" />
                        <span>{t.city}</span>
                      </div>
                    </div>
                  </div>

                  <span
                    className={`text-[10px] font-semibold px-2 py-0.5 rounded border ${getStatusBadge(
                      t.status
                    )}`}
                  >
                    {t.status}
                  </span>
                </div>

                {/* Skills tags */}
                <div className="flex flex-wrap gap-1.5 mb-4">
                  {t.skills.map((skill) => (
                    <span
                      key={skill}
                      className="text-[10px] px-2 py-0.5 rounded bg-slate-950 text-slate-300 border border-slate-800"
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </div>

              {/* Metrics footer */}
              <div className="grid grid-cols-3 gap-2 pt-3 border-t border-slate-800/80 text-center font-mono text-xs">
                <div className="p-1.5 rounded bg-slate-950/60">
                  <div className="text-[10px] text-slate-500 uppercase">Rating</div>
                  <div className="font-bold text-slate-200">★ {t.rating}</div>
                </div>
                <div className="p-1.5 rounded bg-slate-950/60">
                  <div className="text-[10px] text-slate-500 uppercase">Completed</div>
                  <div className="font-bold text-slate-200">{t.completedJobs}</div>
                </div>
                <div className="p-1.5 rounded bg-slate-950/60">
                  <div className="text-[10px] text-slate-500 uppercase">SLA Success</div>
                  <div className="font-bold text-emerald-400">{t.slaSuccessRate}%</div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        /* List View */
        <div className="bg-slate-900/80 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-950/60 border-b border-slate-800/80 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <tr>
                  <th className="py-3 px-4">Technician</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">City / Region</th>
                  <th className="py-3 px-4">Core Competencies</th>
                  <th className="py-3 px-4">Rating</th>
                  <th className="py-3 px-4">Completed</th>
                  <th className="py-3 px-4">SLA Success</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {filteredTechnicians.map((t) => (
                  <tr
                    key={t.id}
                    onClick={() => setSelectedTech(t)}
                    className="hover:bg-slate-800/40 transition-colors cursor-pointer"
                  >
                    <td className="py-3.5 px-4">
                      <div className="font-medium text-slate-200">{t.name}</div>
                      <div className="text-[11px] text-slate-500">{t.phone}</div>
                    </td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`text-[10px] font-semibold px-2 py-0.5 rounded border ${getStatusBadge(
                          t.status
                        )}`}
                      >
                        {t.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">{t.city}</td>
                    <td className="py-3.5 px-4">
                      <div className="flex flex-wrap gap-1 max-w-xs">
                        {t.skills.slice(0, 2).map((s) => (
                          <span
                            key={s}
                            className="text-[10px] px-1.5 py-0.5 rounded bg-slate-950 text-slate-400 border border-slate-800"
                          >
                            {s}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-3.5 px-4 font-mono">★ {t.rating}</td>
                    <td className="py-3.5 px-4 font-mono">{t.completedJobs}</td>
                    <td className="py-3.5 px-4 font-mono text-emerald-400">{t.slaSuccessRate}%</td>
                    <td className="py-3.5 px-4 text-right">
                      <button className="text-blue-400 hover:text-blue-300 font-medium">
                        Profile →
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Technician Profile Detail Slide-Over Modal */}
      {selectedTech && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-xl w-full p-6 shadow-2xl relative space-y-5">
            <button
              onClick={() => setSelectedTech(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-full overflow-hidden bg-slate-800 ring-2 ring-blue-500 shrink-0">
                <img
                  src={selectedTech.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                  alt={selectedTech.name}
                  className="w-full h-full object-cover"
                />
              </div>
              <div>
                <h2 className="text-lg font-bold text-white">{selectedTech.name}</h2>
                <p className="text-xs text-slate-400">Senior Field Operations Specialist</p>
                <div className="flex items-center gap-2 mt-1">
                  <span
                    className={`text-[10px] font-semibold px-2 py-0.5 rounded border ${getStatusBadge(
                      selectedTech.status
                    )}`}
                  >
                    {selectedTech.status}
                  </span>
                  <span className="text-xs text-slate-400 font-mono">★ {selectedTech.rating} Rating</span>
                </div>
              </div>
            </div>

            {/* Quick Status Dispatch Changer */}
            <div className="p-3.5 rounded-lg bg-slate-950/80 border border-slate-800 text-xs space-y-2">
              <div className="text-slate-400 font-medium">Update Dispatch Availability:</div>
              <div className="grid grid-cols-4 gap-2">
                {(['AVAILABLE', 'ON_ROUTE', 'BUSY', 'OFFLINE'] as TechnicianStatus[]).map((st) => (
                  <button
                    key={st}
                    onClick={() =>
                      updateStatusMutation.mutate({ id: selectedTech.id, status: st })
                    }
                    className={`py-1.5 px-2 rounded text-[11px] font-medium border transition-colors ${
                      selectedTech.status === st
                        ? 'bg-blue-600 text-white border-blue-500'
                        : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'
                    }`}
                  >
                    {st}
                  </button>
                ))}
              </div>
            </div>

            {/* Metrics */}
            <div className="grid grid-cols-3 gap-3 text-center text-xs font-mono">
              <div className="p-3 rounded-lg bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500 text-[10px] block">JOBS COMPLETED</span>
                <span className="text-lg font-bold text-white">{selectedTech.completedJobs}</span>
              </div>
              <div className="p-3 rounded-lg bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500 text-[10px] block">SLA ADHERENCE</span>
                <span className="text-lg font-bold text-emerald-400">
                  {selectedTech.slaSuccessRate}%
                </span>
              </div>
              <div className="p-3 rounded-lg bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500 text-[10px] block">UTILIZATION</span>
                <span className="text-lg font-bold text-blue-400">
                  {selectedTech.utilizationRate || 75}%
                </span>
              </div>
            </div>

            {/* Contact & Location */}
            <div className="space-y-2 text-xs text-slate-300">
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-slate-500" />
                <span>{selectedTech.phone}</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-slate-500" />
                <span>{selectedTech.email}</span>
              </div>
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-slate-500" />
                <span>
                  {selectedTech.address || selectedTech.city} (Lat: {selectedTech.latitude?.toFixed(4)}, Lon: {selectedTech.longitude?.toFixed(4)})
                </span>
              </div>
            </div>

            {/* Skills */}
            <div>
              <div className="text-xs font-semibold text-slate-400 mb-2 uppercase">
                Technical Certifications
              </div>
              <div className="flex flex-wrap gap-1.5">
                {selectedTech.skills.map((s) => (
                  <span
                    key={s}
                    className="px-2.5 py-1 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 text-xs"
                  >
                    {s}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

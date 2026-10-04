import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import {
  Search,
  Filter,
  Plus,
  ArrowUpDown,
  AlertTriangle,
  Clock,
  User,
  MapPin,
  CheckCircle,
  ExternalLink,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react';
import { workOrdersApi, customersApi, techniciansApi } from '@/services/api';
import { WorkOrder, WorkOrderStatus, WorkOrderPriority } from '@/types';

export const WorkOrdersPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const queryClient = useQueryClient();

  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [cityFilter, setCityFilter] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState<'createdAt' | 'slaBreachProbability'>('createdAt');
  const [page, setPage] = useState(1);
  const pageSize = 12;

  // New Work Order Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [newPriority, setNewPriority] = useState<WorkOrderPriority>('HIGH');
  const [newCustomerId, setNewCustomerId] = useState<number | ''>('');
  const [newTechnicianId, setNewTechnicianId] = useState<number | ''>('');

  const { data: workOrders = [], isLoading } = useQuery({
    queryKey: ['work-orders'],
    queryFn: () => workOrdersApi.getAll(),
  });

  const { data: customers = [] } = useQuery({
    queryKey: ['customers-list'],
    queryFn: () => customersApi.getAll(),
  });

  const { data: technicians = [] } = useQuery({
    queryKey: ['technicians-list'],
    queryFn: () => techniciansApi.getAll(),
  });

  const createMutation = useMutation({
    mutationFn: (data: any) => workOrdersApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['work-orders'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
      setModalOpen(false);
      setNewTitle('');
      setNewDescription('');
    },
  });

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim() || !newCustomerId) return;
    createMutation.mutate({
      title: newTitle.trim(),
      description: newDescription.trim(),
      priority: newPriority,
      customerId: Number(newCustomerId),
      technicianId: newTechnicianId ? Number(newTechnicianId) : undefined,
    });
  };

  const filteredOrders = React.useMemo(() => {
    return workOrders
      .filter((wo) => {
        if (statusFilter !== 'ALL' && wo.status !== statusFilter) return false;
        if (priorityFilter !== 'ALL' && wo.priority !== priorityFilter) return false;
        if (cityFilter !== 'ALL' && wo.city?.toLowerCase() !== cityFilter.toLowerCase()) return false;
        if (search.trim()) {
          const q = search.toLowerCase();
          const matchNum = wo.workOrderNumber?.toLowerCase().includes(q);
          const matchTitle = wo.title?.toLowerCase().includes(q);
          const matchCust = wo.customer?.company?.toLowerCase().includes(q) || wo.customer?.name?.toLowerCase().includes(q);
          const matchTech = wo.technician?.name?.toLowerCase().includes(q);
          if (!matchNum && !matchTitle && !matchCust && !matchTech) return false;
        }
        return true;
      })
      .sort((a, b) => {
        if (sortBy === 'slaBreachProbability') {
          return (b.slaBreachProbability || 0) - (a.slaBreachProbability || 0);
        }
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      });
  }, [workOrders, statusFilter, priorityFilter, cityFilter, search, sortBy]);

  const totalPages = Math.ceil(filteredOrders.length / pageSize) || 1;
  const paginatedOrders = filteredOrders.slice((page - 1) * pageSize, page * pageSize);

  const getPriorityBadge = (p: WorkOrderPriority) => {
    switch (p) {
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

  const getStatusBadge = (s: WorkOrderStatus) => {
    switch (s) {
      case 'COMPLETED':
        return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
      case 'IN_PROGRESS':
        return 'bg-blue-500/15 text-blue-400 border-blue-500/30';
      case 'ASSIGNED':
        return 'bg-purple-500/15 text-purple-400 border-purple-500/30';
      case 'AWAITING_VERIFICATION':
        return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
      default:
        return 'bg-slate-500/15 text-slate-300 border-slate-700';
    }
  };

  return (
    <div className="p-4 lg:p-8 space-y-6 max-w-[1700px] mx-auto">
      {/* Page Title & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h1 className="text-xl lg:text-2xl font-bold tracking-tight text-white font-sans">
            Work Order Operations
          </h1>
          <p className="text-xs text-slate-400">
            Lifecycle monitoring, real-time dispatch assignment, and service SLA governance.
          </p>
        </div>

        <button
          onClick={() => setModalOpen(true)}
          className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white rounded-lg shadow-sm shadow-blue-500/20 transition-all self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Create Work Order</span>
        </button>
      </div>

      {/* Filter and Search Controls */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-3 bg-slate-900/60 p-3.5 rounded-xl border border-slate-800">
        {/* Search */}
        <div className="md:col-span-4 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by ID, customer, technician..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(1);
            }}
            className="w-full h-9 pl-9 pr-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>

        {/* Status Filter */}
        <div className="md:col-span-2">
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(1);
            }}
            className="w-full h-9 px-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-300 focus:outline-none focus:border-blue-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="NEW">New</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="AWAITING_VERIFICATION">Awaiting Verification</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>

        {/* Priority Filter */}
        <div className="md:col-span-2">
          <select
            value={priorityFilter}
            onChange={(e) => {
              setPriorityFilter(e.target.value);
              setPage(1);
            }}
            className="w-full h-9 px-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-300 focus:outline-none focus:border-blue-500"
          >
            <option value="ALL">All Priorities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>

        {/* Region Filter */}
        <div className="md:col-span-2">
          <select
            value={cityFilter}
            onChange={(e) => {
              setCityFilter(e.target.value);
              setPage(1);
            }}
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

        {/* Sort By */}
        <div className="md:col-span-2">
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="w-full h-9 px-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-300 focus:outline-none focus:border-blue-500"
          >
            <option value="createdAt">Sort: Newest First</option>
            <option value="slaBreachProbability">Sort: SLA Risk (Highest)</option>
          </select>
        </div>
      </div>

      {/* Main Table View */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 border-b border-slate-800/80 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="py-3 px-4">WO ID</th>
                <th className="py-3 px-4">Customer & Location</th>
                <th className="py-3 px-4">Issue Description</th>
                <th className="py-3 px-4">Priority</th>
                <th className="py-3 px-4">Assigned Engineer</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">SLA Risk</th>
                <th className="py-3 px-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Loading work orders telemetry...
                  </td>
                </tr>
              ) : paginatedOrders.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    No work orders found matching the filter criteria.
                  </td>
                </tr>
              ) : (
                paginatedOrders.map((wo) => (
                  <tr
                    key={wo.id}
                    className="hover:bg-slate-800/40 transition-colors group"
                  >
                    <td className="py-3.5 px-4 font-mono font-bold text-blue-400 whitespace-nowrap">
                      <Link to={`/app/work-orders/${wo.id}`} className="hover:underline">
                        {wo.workOrderNumber}
                      </Link>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="font-medium text-slate-200">
                        {wo.customer?.company || wo.customer?.name}
                      </div>
                      <div className="text-[11px] text-slate-500 flex items-center gap-1 mt-0.5">
                        <MapPin className="w-3 h-3 text-slate-500 shrink-0" />
                        <span className="truncate max-w-[150px]">{wo.city || wo.locationName}</span>
                      </div>
                    </td>

                    <td className="py-3.5 px-4 max-w-xs">
                      <div className="font-medium text-slate-200 line-clamp-1">{wo.title}</div>
                      <div className="text-[11px] text-slate-500 line-clamp-1 mt-0.5">
                        {wo.description}
                      </div>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded border ${getPriorityBadge(
                          wo.priority
                        )}`}
                      >
                        {wo.priority}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {wo.technician ? (
                        <div className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                          <span className="text-slate-200 font-medium">{wo.technician.name}</span>
                        </div>
                      ) : (
                        <span className="text-slate-500 italic">Unassigned</span>
                      )}
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <span
                        className={`text-[10px] font-semibold px-2 py-0.5 rounded border ${getStatusBadge(
                          wo.status
                        )}`}
                      >
                        {wo.status.replace('_', ' ')}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap font-mono">
                      <div className="flex items-center gap-2">
                        <span
                          className={`font-semibold ${
                            wo.slaBreachProbability >= 60
                              ? 'text-rose-400'
                              : 'text-slate-300'
                          }`}
                        >
                          {wo.slaBreachProbability}%
                        </span>
                        {wo.slaBreachProbability >= 60 && (
                          <AlertTriangle className="w-3.5 h-3.5 text-rose-400 animate-pulse" />
                        )}
                      </div>
                    </td>

                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <Link
                        to={`/app/work-orders/${wo.id}`}
                        className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-blue-400 hover:text-white hover:bg-blue-600 rounded border border-blue-500/30 transition-all"
                      >
                        <span>Details</span>
                        <ExternalLink className="w-3 h-3" />
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div className="px-4 py-3 bg-slate-950/60 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
          <div>
            Showing <span className="text-slate-200 font-semibold">{paginatedOrders.length}</span> of{' '}
            <span className="text-slate-200 font-semibold">{filteredOrders.length}</span> orders
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page === 1}
              className="p-1 rounded bg-slate-900 border border-slate-800 text-slate-300 hover:bg-slate-800 disabled:opacity-40"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-mono text-slate-300">
              Page {page} of {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
              className="p-1 rounded bg-slate-900 border border-slate-800 text-slate-300 hover:bg-slate-800 disabled:opacity-40"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Create Work Order Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-lg w-full p-6 shadow-2xl relative">
            <button
              onClick={() => setModalOpen(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-lg font-bold text-white mb-1">Create New Work Order</h2>
            <p className="text-xs text-slate-400 mb-4">
              Dispatch a new ticket into the operational grid.
            </p>

            <form onSubmit={handleCreateSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-medium mb-1">Title / Issue *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. 500kVA Transformer B-Phase High Temp"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  className="w-full h-9 px-3 bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Description</label>
                <textarea
                  rows={3}
                  placeholder="Operational details, failure symptoms, access instructions..."
                  value={newDescription}
                  onChange={(e) => setNewDescription(e.target.value)}
                  className="w-full p-3 bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Customer / Facility *</label>
                  <select
                    required
                    value={newCustomerId}
                    onChange={(e) => setNewCustomerId(Number(e.target.value))}
                    className="w-full h-9 px-2 bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:border-blue-500"
                  >
                    <option value="">Select customer...</option>
                    {customers.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.company || c.name} ({c.city})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-slate-300 font-medium mb-1">Priority *</label>
                  <select
                    value={newPriority}
                    onChange={(e) => setNewPriority(e.target.value as WorkOrderPriority)}
                    className="w-full h-9 px-2 bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:border-blue-500"
                  >
                    <option value="CRITICAL">Critical (Immediate SLA)</option>
                    <option value="HIGH">High (2-4h SLA)</option>
                    <option value="MEDIUM">Medium (8h SLA)</option>
                    <option value="LOW">Low (24h SLA)</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-300 font-medium mb-1">Assign Field Technician (Optional)</label>
                <select
                  value={newTechnicianId}
                  onChange={(e) => setNewTechnicianId(e.target.value ? Number(e.target.value) : '')}
                  className="w-full h-9 px-2 bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:border-blue-500"
                >
                  <option value="">Unassigned (Queue in Dispatch)</option>
                  {technicians.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.name} — {t.status} ({t.city})
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 text-xs font-medium text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white rounded-lg transition-colors"
                >
                  {createMutation.isPending ? 'Dispatching...' : 'Confirm Dispatch'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

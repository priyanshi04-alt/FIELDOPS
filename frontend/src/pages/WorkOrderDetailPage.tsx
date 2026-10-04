import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  ArrowLeft,
  Clock,
  AlertTriangle,
  CheckCircle2,
  MapPin,
  Phone,
  Mail,
  User,
  Wrench,
  Bot,
  FileText,
  Calendar,
  Send,
  Sparkles,
} from 'lucide-react';
import { workOrdersApi } from '@/services/api';
import { WorkOrderStatus, WorkOrderPriority } from '@/types';
import { SmartDispatchPanel } from '@/components/intelligence/SmartDispatchPanel';
import { SlaIntelligenceCard } from '@/components/intelligence/SlaIntelligenceCard';
import { RouteIntelligenceCard } from '@/components/intelligence/RouteIntelligenceCard';
import { DecisionAuditTrail } from '@/components/intelligence/DecisionAuditTrail';

export const WorkOrderDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [newNote, setNewNote] = useState('');

  const { data: wo, isLoading } = useQuery({
    queryKey: ['work-order', id],
    queryFn: () => workOrdersApi.getById(Number(id)),
    enabled: !!id,
  });

  const updateMutation = useMutation({
    mutationFn: (data: any) => workOrdersApi.update(Number(id), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['work-order', id] });
      queryClient.invalidateQueries({ queryKey: ['work-orders'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
    },
  });

  if (isLoading) {
    return (
      <div className="p-8 space-y-6 max-w-6xl mx-auto">
        <div className="h-6 w-32 bg-slate-800 animate-pulse rounded" />
        <div className="h-24 bg-slate-900 border border-slate-800 rounded-xl animate-pulse" />
        <div className="grid grid-cols-3 gap-6">
          <div className="col-span-2 h-96 bg-slate-900 border border-slate-800 rounded-xl animate-pulse" />
          <div className="h-96 bg-slate-900 border border-slate-800 rounded-xl animate-pulse" />
        </div>
      </div>
    );
  }

  if (!wo) {
    return (
      <div className="p-12 text-center text-slate-400">
        <p>Work order not found.</p>
        <Link to="/app/work-orders" className="text-blue-400 underline text-xs mt-2 block">
          Back to Work Orders
        </Link>
      </div>
    );
  }

  const steps: Array<{ status: WorkOrderStatus; label: string; desc: string }> = [
    { status: 'NEW', label: 'Request Created', desc: 'Ticket registered by NOC' },
    { status: 'ASSIGNED', label: 'Technician Assigned', desc: 'Dispatched to primary engineer' },
    { status: 'IN_PROGRESS', label: 'Service In Progress', desc: 'Engineer active on site' },
    { status: 'AWAITING_VERIFICATION', label: 'Awaiting Verification', desc: 'Quality audit & client sign-off' },
    { status: 'COMPLETED', label: 'Completed', desc: 'SLA closed & telemetry stored' },
  ];

  const currentStepIndex = steps.findIndex((s) => s.status === wo.status);

  const handleStatusAdvance = (nextStatus: WorkOrderStatus) => {
    updateMutation.mutate({ status: nextStatus });
  };

  const handleAddNote = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNote.trim()) return;
    const existing = wo.serviceNotes ? `${wo.serviceNotes}\n\n` : '';
    const updated = `${existing}[${new Date().toLocaleTimeString()}]: ${newNote.trim()}`;
    updateMutation.mutate({ serviceNotes: updated });
    setNewNote('');
  };

  return (
    <div className="p-4 lg:p-8 space-y-6 max-w-[1500px] mx-auto">
      {/* Back Button and Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <Link
            to="/app/work-orders"
            className="p-2 rounded-lg bg-slate-900 border border-slate-800 text-slate-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-xl font-bold text-blue-400 tracking-tight">
                {wo.workOrderNumber}
              </span>
              <span
                className={`text-xs font-bold px-2 py-0.5 rounded border ${
                  wo.priority === 'CRITICAL'
                    ? 'bg-rose-500/15 text-rose-400 border-rose-500/30'
                    : wo.priority === 'HIGH'
                    ? 'bg-amber-500/15 text-amber-400 border-amber-500/30'
                    : 'bg-blue-500/15 text-blue-400 border-blue-500/30'
                }`}
              >
                {wo.priority}
              </span>
              <span className="text-xs font-semibold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                {wo.status.replace('_', ' ')}
              </span>
            </div>
            <h1 className="text-lg font-bold text-white mt-1">{wo.title}</h1>
          </div>
        </div>

        {/* Quick Lifecycle Action Buttons */}
        <div className="flex items-center gap-2">
          {wo.status === 'NEW' && (
            <button
              onClick={() => handleStatusAdvance('ASSIGNED')}
              className="px-3.5 py-1.5 text-xs font-semibold bg-purple-600 hover:bg-purple-500 text-white rounded-lg transition-colors"
            >
              Assign & Dispatch
            </button>
          )}
          {wo.status === 'ASSIGNED' && (
            <button
              onClick={() => handleStatusAdvance('IN_PROGRESS')}
              className="px-3.5 py-1.5 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white rounded-lg transition-colors"
            >
              Start Onsite Repair
            </button>
          )}
          {wo.status === 'IN_PROGRESS' && (
            <button
              onClick={() => handleStatusAdvance('AWAITING_VERIFICATION')}
              className="px-3.5 py-1.5 text-xs font-semibold bg-amber-600 hover:bg-amber-500 text-white rounded-lg transition-colors"
            >
              Submit for Verification
            </button>
          )}
          {wo.status === 'AWAITING_VERIFICATION' && (
            <button
              onClick={() => handleStatusAdvance('COMPLETED')}
              className="px-3.5 py-1.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg transition-colors"
            >
              Verify & Complete
            </button>
          )}
        </div>
      </div>

      {/* Operational Lifecycle Stepper */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5">
        <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
          Operational Lifecycle Stepper
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
          {steps.map((step, idx) => {
            const isDone = idx < currentStepIndex || wo.status === 'COMPLETED';
            const isCurrent = idx === currentStepIndex && wo.status !== 'COMPLETED';

            return (
              <div
                key={step.status}
                className={`p-3 rounded-lg border transition-all ${
                  isCurrent
                    ? 'bg-blue-500/10 border-blue-500/40 text-blue-300 ring-1 ring-blue-500/20'
                    : isDone
                    ? 'bg-emerald-500/5 border-emerald-500/20 text-emerald-400'
                    : 'bg-slate-950/40 border-slate-800/80 text-slate-500'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="text-[11px] font-mono font-bold">0{idx + 1}</span>
                  {isDone ? (
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                  ) : isCurrent ? (
                    <span className="w-2 h-2 rounded-full bg-blue-400 animate-ping" />
                  ) : (
                    <span className="w-1.5 h-1.5 rounded-full bg-slate-700" />
                  )}
                </div>
                <div className="text-xs font-bold text-slate-200">{step.label}</div>
                <div className="text-[10px] text-slate-400 mt-0.5 line-clamp-1">{step.desc}</div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Main Grid: Details Left, Intelligence & Summary Right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column (8 cols): Description, Customer, Technician, Notes */}
        <div className="lg:col-span-8 space-y-6">
          {/* Issue Scope Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-3">
            <h3 className="text-sm font-semibold text-white">Issue Specification</h3>
            <p className="text-xs leading-relaxed text-slate-300 whitespace-pre-wrap">
              {wo.description || 'No detailed issue logs provided.'}
            </p>

            <div className="pt-3 border-t border-slate-800/80 flex flex-wrap gap-2">
              <span className="text-xs text-slate-400 self-center mr-1">Required Competencies:</span>
              {(wo.requiredSkills || []).map((sk) => (
                <span
                  key={sk}
                  className="px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 text-[11px] font-medium"
                >
                  {sk}
                </span>
              ))}
            </div>
          </div>

          {/* Customer & Location Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-white">Service Location & Customer</h3>
              <span className="text-xs text-slate-400 font-mono">
                Lat: {wo.latitude?.toFixed(4)}, Lon: {wo.longitude?.toFixed(4)}
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
              <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800/80 space-y-1.5">
                <div className="text-slate-400 font-medium">Customer / Account</div>
                <div className="font-semibold text-slate-200 text-sm">
                  {wo.customer?.company || wo.customer?.name}
                </div>
                <div className="flex items-center gap-1.5 text-slate-400">
                  <User className="w-3.5 h-3.5 text-slate-500" />
                  <span>Contact: {wo.customer?.name}</span>
                </div>
                <div className="flex items-center gap-1.5 text-slate-400">
                  <Phone className="w-3.5 h-3.5 text-slate-500" />
                  <span>{wo.customer?.phone}</span>
                </div>
              </div>

              <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800/80 space-y-1.5">
                <div className="text-slate-400 font-medium">Facility Address</div>
                <div className="font-semibold text-slate-200 text-sm">{wo.locationName}</div>
                <div className="flex items-start gap-1.5 text-slate-400">
                  <MapPin className="w-3.5 h-3.5 text-slate-500 shrink-0 mt-0.5" />
                  <span>{wo.address || wo.city}</span>
                </div>
                <div className="text-slate-500 text-[11px]">Region Node: {wo.city}</div>
              </div>
            </div>
          </div>

          {/* Assigned Field Engineer Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-3">
            <h3 className="text-sm font-semibold text-white">Assigned Field Engineer</h3>

            {wo.technician ? (
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-lg bg-slate-950/70 border border-slate-800">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-full overflow-hidden bg-slate-800 ring-2 ring-blue-500/30">
                    <img
                      src={wo.technician.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                      alt={wo.technician.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-slate-100">{wo.technician.name}</h4>
                    <p className="text-xs text-slate-400">{wo.technician.phone}</p>
                    <div className="flex items-center gap-2 mt-1 text-[11px] font-mono text-slate-300">
                      <span>★ {wo.technician.rating} Rating</span>
                      <span>•</span>
                      <span>{wo.technician.completedJobs} Jobs Completed</span>
                    </div>
                  </div>
                </div>

                <div className="text-right sm:border-l sm:border-slate-800 sm:pl-4 space-y-1 text-xs">
                  <div className="text-slate-400">Dispatch Status:</div>
                  <span className="font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/30 text-[11px]">
                    {wo.technician.status}
                  </span>
                  <div className="text-slate-400 pt-1">
                    ETA to Site:{' '}
                    <span className="text-slate-200 font-mono font-semibold">
                      {wo.technicianEtaMinutes || 21} min
                    </span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-6 text-center text-slate-500 border border-dashed border-slate-800 rounded-lg text-xs">
                No engineer currently assigned. Assign a technician from the dispatcher console.
              </div>
            )}
          </div>

          {/* Service Notes & Work Log */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-4">
            <h3 className="text-sm font-semibold text-white">Service Log & Notes</h3>

            <div className="p-3 bg-slate-950/70 border border-slate-800 rounded-lg text-xs text-slate-300 font-mono whitespace-pre-wrap leading-relaxed max-h-48 overflow-y-auto">
              {wo.serviceNotes || 'No operational notes filed yet.'}
            </div>

            <form onSubmit={handleAddNote} className="flex gap-2">
              <input
                type="text"
                placeholder="Append diagnostic note, part replaced, or client feedback..."
                value={newNote}
                onChange={(e) => setNewNote(e.target.value)}
                className="flex-1 h-9 px-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-blue-500"
              />
              <button
                type="submit"
                disabled={updateMutation.isPending || !newNote.trim()}
                className="px-3.5 py-1.5 text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-blue-400 border border-slate-700 rounded-lg transition-colors flex items-center gap-1.5"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Log</span>
              </button>
            </form>
          </div>

          {/* Decision Audit Trail */}
          <DecisionAuditTrail workOrderId={Number(id)} />
        </div>

        {/* Right Column (4 cols): Intelligent Operations Engine Suite */}
        <div className="lg:col-span-4 space-y-6">
          {/* Smart Dispatch Recommendation Engine */}
          <SmartDispatchPanel
            workOrderId={Number(id)}
            workOrderNumber={wo.workOrderNumber}
            isAssigned={!!wo.technician}
            currentTechnicianName={wo.technician?.name}
          />

          {/* Real Dynamic SLA Risk Intelligence Engine */}
          <SlaIntelligenceCard workOrderId={Number(id)} />

          {/* Route & Transit Telemetry */}
          <RouteIntelligenceCard
            workOrderId={Number(id)}
            technicianId={wo.technician?.id}
          />

          {/* Operational Metrics Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-4">
            <h3 className="text-sm font-semibold text-white">Lifecycle Timing Baseline</h3>

            <div className="space-y-3 text-xs">
              <div className="flex justify-between items-center py-2 border-b border-slate-800/80">
                <span className="text-slate-400">Contractual SLA Deadline:</span>
                <span className="font-mono text-slate-200">
                  {new Date(wo.slaDeadline).toLocaleTimeString()}
                </span>
              </div>

              <div className="flex justify-between items-center py-2 border-b border-slate-800/80">
                <span className="text-slate-400">Estimated Duration:</span>
                <span className="font-mono text-slate-200">
                  {wo.estimatedDurationHours} Hours
                </span>
              </div>

              <div className="flex justify-between items-center py-2 border-b border-slate-800/80">
                <span className="text-slate-400">Target Resolution:</span>
                <span className="font-mono text-slate-200">{wo.city} Regional Depot</span>
              </div>

              <div className="flex justify-between items-center py-2">
                <span className="text-slate-400">Registered At:</span>
                <span className="font-mono text-slate-200">
                  {new Date(wo.createdAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

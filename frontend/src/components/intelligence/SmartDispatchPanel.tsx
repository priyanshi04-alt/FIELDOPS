import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  Sparkles, CheckCircle2, XCircle, ChevronDown, ChevronUp, 
  MapPin, Clock, Award, ShieldAlert, User, ArrowRight
} from 'lucide-react';
import { intelligenceApi } from '@/services/api';
import { CandidateTechnicianScore } from '@/types';

interface SmartDispatchPanelProps {
  workOrderId: number;
  workOrderNumber: string;
  isAssigned: boolean;
  currentTechnicianName?: string;
}

export const SmartDispatchPanel: React.FC<SmartDispatchPanelProps> = ({
  workOrderId,
  workOrderNumber,
  isAssigned,
  currentTechnicianName,
}) => {
  const queryClient = useQueryClient();
  const [expandedCandidateId, setExpandedCandidateId] = useState<number | null>(null);
  const [isRejectOpen, setIsRejectOpen] = useState(false);
  const [selectedAltTechId, setSelectedAltTechId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  const { data: recommendation, isLoading, refetch } = useQuery({
    queryKey: ['assignment-recommendation', workOrderId],
    queryFn: () => intelligenceApi.getAssignmentRecommendation(workOrderId),
    enabled: !!workOrderId,
  });

  const acceptMutation = useMutation({
    mutationFn: (technicianId?: number) => intelligenceApi.acceptRecommendation(workOrderId, technicianId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['work-order', String(workOrderId)] });
      queryClient.invalidateQueries({ queryKey: ['work-orders'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-overview'] });
      queryClient.invalidateQueries({ queryKey: ['intelligence-overview'] });
      queryClient.invalidateQueries({ queryKey: ['audit-trail', workOrderId] });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: ({ selectedTechId, reason }: { selectedTechId?: number; reason: string }) =>
      intelligenceApi.rejectRecommendation(workOrderId, selectedTechId, reason),
    onSuccess: () => {
      setIsRejectOpen(false);
      setSelectedAltTechId(null);
      setRejectReason('');
      queryClient.invalidateQueries({ queryKey: ['work-order', String(workOrderId)] });
      queryClient.invalidateQueries({ queryKey: ['work-orders'] });
      queryClient.invalidateQueries({ queryKey: ['audit-trail', workOrderId] });
    },
  });

  if (isLoading) {
    return (
      <div className="p-5 rounded-xl bg-slate-900/80 border border-slate-800 space-y-3 animate-pulse">
        <div className="h-4 w-40 bg-slate-800 rounded" />
        <div className="h-20 bg-slate-950/70 rounded" />
      </div>
    );
  }

  if (!recommendation || !recommendation.topCandidate) {
    return (
      <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 text-xs text-slate-400">
        No candidate technicians available in active roster.
      </div>
    );
  }

  const top = recommendation.topCandidate;
  const otherCandidates = recommendation.candidates.filter((c: any) => c.technicianId !== top.technicianId).slice(0, 3);

  const toggleExpand = (techId: number) => {
    setExpandedCandidateId(expandedCandidateId === techId ? null : techId);
  };

  return (
    <div className="p-5 rounded-xl bg-slate-900/90 border border-indigo-500/30 shadow-xl space-y-4 relative overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-lg bg-indigo-500/15 text-indigo-400 border border-indigo-500/30">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-white">Smart Dispatch Recommendation</h3>
            <p className="text-[11px] text-slate-400">
              Deterministic 100-point match engine (Skills 35% • Distance 25% • Availability 15%)
            </p>
          </div>
        </div>
        <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-indigo-500/15 text-indigo-300 border border-indigo-500/30 font-semibold">
          AI Candidate Ranking
        </span>
      </div>

      {/* Recommended Top Candidate Card */}
      <div className="p-4 rounded-xl bg-slate-950/80 border border-indigo-500/40 relative">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-full overflow-hidden bg-slate-800 ring-2 ring-indigo-500/50 shrink-0">
              <img
                src={top.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                alt={top.name}
                className="w-full h-full object-cover"
              />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-sm font-bold text-white">{top.name}</span>
                <span className="text-xs font-mono font-extrabold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                  {top.totalScore}% Match
                </span>
              </div>
              <div className="flex items-center gap-3 mt-1 text-[11px] text-slate-400 font-mono">
                <span className="flex items-center gap-1">
                  <MapPin className="w-3 h-3 text-indigo-400" />
                  {top.distanceKm} km away
                </span>
                <span className="flex items-center gap-1">
                  <Clock className="w-3 h-3 text-indigo-400" />
                  ETA {top.estimatedEtaMinutes} min
                </span>
                <span className="flex items-center gap-1 text-amber-400 font-sans">
                  ★ {top.rating}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2 pt-2 sm:pt-0">
            <button
              onClick={() => toggleExpand(top.technicianId)}
              className="px-2.5 py-1.5 text-[11px] font-medium text-slate-300 hover:text-white bg-slate-900 border border-slate-800 rounded-lg transition flex items-center gap-1"
            >
              <span>Why {top.name}?</span>
              {expandedCandidateId === top.technicianId ? (
                <ChevronUp className="w-3 h-3" />
              ) : (
                <ChevronDown className="w-3 h-3" />
              )}
            </button>

            <button
              onClick={() => acceptMutation.mutate(top.technicianId)}
              disabled={acceptMutation.isPending}
              className="px-3.5 py-1.5 text-xs font-semibold bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg shadow-sm shadow-indigo-600/30 transition flex items-center gap-1.5 disabled:opacity-50"
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>{acceptMutation.isPending ? 'Assigning...' : `Assign ${top.name.split(' ')[0]}`}</span>
            </button>
          </div>
        </div>

        {/* Quick Highlights Pills */}
        <div className="flex flex-wrap gap-2 mt-3 pt-3 border-t border-slate-900 text-[11px]">
          <span className="flex items-center gap-1 text-emerald-400">
            ✓ Skills: {top.skillScore}/35
          </span>
          <span className="text-slate-600">•</span>
          <span className="flex items-center gap-1 text-emerald-400">
            ✓ Distance: {top.distanceScore}/25
          </span>
          <span className="text-slate-600">•</span>
          <span className="flex items-center gap-1 text-emerald-400">
            ✓ Status: {top.status} ({top.availabilityScore}/15)
          </span>
          <span className="text-slate-600">•</span>
          <span className="flex items-center gap-1 text-emerald-400">
            ✓ Feasibility: {top.slaFeasibilityScore}/10
          </span>
        </div>

        {/* Explainability Breakdown Panel */}
        {expandedCandidateId === top.technicianId && (
          <div className="mt-4 pt-3 border-t border-slate-800/80 space-y-2.5 animate-fadeIn">
            <div className="text-[11px] font-semibold text-slate-300 uppercase tracking-wider">
              Scoring Factors & Logic:
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
              {top.factors.map((f: any, i: number) => (
                <div key={i} className="p-2.5 rounded bg-slate-900 border border-slate-800">
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-semibold text-slate-200">{f.factorName.replace('_', ' ')}</span>
                    <span className="font-mono text-indigo-400 font-bold">{f.score}/{f.maxScore} pts</span>
                  </div>
                  <p className="text-[11px] text-slate-400 leading-snug">{f.explanation}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Alternative Candidates */}
      {otherCandidates.length > 0 && (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Alternative Qualified Candidates</span>
            <button
              onClick={() => setIsRejectOpen(!isRejectOpen)}
              className="text-slate-400 hover:text-slate-200 text-[11px] underline"
            >
              Manual Dispatch Override / Reject
            </button>
          </div>

          <div className="space-y-1.5">
            {otherCandidates.map((c: any) => (
              <div
                key={c.technicianId}
                className="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800/80 hover:border-slate-700 transition flex items-center justify-between text-xs"
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-7 h-7 rounded-full bg-slate-800 overflow-hidden ring-1 ring-slate-700">
                    <img
                      src={c.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                      alt={c.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div>
                    <span className="font-semibold text-slate-200">{c.name}</span>
                    <span className="text-[11px] text-slate-500 ml-2 font-mono">
                      {c.distanceKm} km • ETA {c.estimatedEtaMinutes}m
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span className="font-mono font-bold text-slate-300">{c.totalScore}%</span>
                  <button
                    onClick={() => acceptMutation.mutate(c.technicianId)}
                    disabled={acceptMutation.isPending}
                    className="px-2 py-1 text-[11px] font-medium bg-slate-800 hover:bg-slate-700 text-indigo-300 rounded border border-slate-700 transition"
                  >
                    Select
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Reject / Manual Override Form */}
      {isRejectOpen && (
        <div className="p-3.5 rounded-lg bg-slate-950 border border-rose-900/40 space-y-3 animate-fadeIn">
          <div className="text-xs font-semibold text-rose-300 flex items-center gap-1.5">
            <XCircle className="w-3.5 h-3.5" />
            <span>Override System Recommendation (Audited Event)</span>
          </div>
          <div className="space-y-2">
            <select
              value={selectedAltTechId || ''}
              onChange={(e) => setSelectedAltTechId(Number(e.target.value) || null)}
              className="w-full px-2.5 py-1.5 text-xs bg-slate-900 border border-slate-800 rounded text-slate-200"
            >
              <option value="">Select Alternative Technician to Assign...</option>
              {recommendation.candidates.map((c: any) => (
                <option key={c.technicianId} value={c.technicianId}>
                  {c.name} ({c.totalScore}% match - {c.distanceKm} km away)
                </option>
              ))}
            </select>

            <input
              type="text"
              placeholder="Operational reason for rejecting top candidate (e.g., specific customer request)..."
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              className="w-full px-2.5 py-1.5 text-xs bg-slate-900 border border-slate-800 rounded text-slate-200 placeholder:text-slate-500"
            />

            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setIsRejectOpen(false)}
                className="px-2.5 py-1 text-xs text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={rejectMutation.isPending || !selectedAltTechId}
                onClick={() =>
                  rejectMutation.mutate({
                    selectedTechId: selectedAltTechId || undefined,
                    reason: rejectReason,
                  })
                }
                className="px-3 py-1 text-xs font-semibold bg-rose-600 hover:bg-rose-500 text-white rounded transition disabled:opacity-50"
              >
                {rejectMutation.isPending ? 'Logging...' : 'Confirm Override & Assign'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

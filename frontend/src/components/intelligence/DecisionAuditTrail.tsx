import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { History, CheckCircle, XCircle, Bot, User, Clock } from 'lucide-react';
import { intelligenceApi } from '@/services/api';

interface DecisionAuditTrailProps {
  workOrderId: number;
}

export const DecisionAuditTrail: React.FC<DecisionAuditTrailProps> = ({ workOrderId }) => {
  const { data: logs = [], isLoading } = useQuery({
    queryKey: ['audit-trail', workOrderId],
    queryFn: () => intelligenceApi.getAuditTrail(workOrderId),
    enabled: !!workOrderId,
  });

  if (isLoading) {
    return (
      <div className="p-4 bg-slate-900/60 rounded-xl border border-slate-800 animate-pulse">
        <div className="h-4 w-32 bg-slate-800 rounded" />
      </div>
    );
  }

  if (logs.length === 0) {
    return (
      <div className="p-4 bg-slate-900/60 rounded-xl border border-slate-800 text-xs text-slate-500">
        No decision audit events recorded yet for this work order.
      </div>
    );
  }

  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-5 space-y-4">
      <div className="flex items-center gap-2">
        <History className="w-4 h-4 text-indigo-400" />
        <h3 className="text-sm font-semibold text-white">Decision & Audit History</h3>
        <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-2 py-0.5 rounded">
          {logs.length} Events
        </span>
      </div>

      <div className="relative pl-6 space-y-4 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-800">
        {logs.map((log) => {
          const isAccepted = log.decisionType === 'RECOMMENDATION_ACCEPTED';
          const isRejected = log.decisionType === 'RECOMMENDATION_REJECTED';

          return (
            <div key={log.id} className="relative text-xs space-y-1">
              {/* Dot on timeline */}
              <div
                className={`absolute -left-6 top-0.5 w-4 h-4 rounded-full border-2 border-slate-950 flex items-center justify-center ${
                  isAccepted
                    ? 'bg-emerald-500 text-white'
                    : isRejected
                    ? 'bg-rose-500 text-white'
                    : 'bg-indigo-500 text-white'
                }`}
              >
                {isAccepted ? (
                  <CheckCircle className="w-2.5 h-2.5" />
                ) : isRejected ? (
                  <XCircle className="w-2.5 h-2.5" />
                ) : (
                  <Bot className="w-2.5 h-2.5" />
                )}
              </div>

              <div className="flex items-center justify-between">
                <span className="font-semibold text-slate-200">
                  {log.decisionType.replace('_', ' ')}
                </span>
                <span className="text-[10px] font-mono text-slate-500">
                  {new Date(log.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>

              <p className="text-[11px] text-slate-400 leading-snug">
                {log.explanation}
              </p>

              <div className="flex items-center gap-3 text-[10px] text-slate-500 font-mono pt-1">
                <span>By: {log.dispatcherEmail}</span>
                {log.score && <span>Match Score: {log.score}%</span>}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

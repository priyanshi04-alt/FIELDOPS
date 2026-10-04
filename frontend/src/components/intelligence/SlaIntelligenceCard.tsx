import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { 
  ShieldAlert, AlertTriangle, CheckCircle2, 
  ChevronDown, ChevronUp, Clock, Info
} from 'lucide-react';
import { intelligenceApi } from '@/services/api';

interface SlaIntelligenceCardProps {
  workOrderId: number;
}

export const SlaIntelligenceCard: React.FC<SlaIntelligenceCardProps> = ({ workOrderId }) => {
  const [showFactors, setShowFactors] = useState(false);

  const { data: slaRisk, isLoading } = useQuery({
    queryKey: ['sla-risk', workOrderId],
    queryFn: () => intelligenceApi.getSlaRisk(workOrderId),
    enabled: !!workOrderId,
  });

  if (isLoading) {
    return (
      <div className="p-5 rounded-xl bg-slate-900/80 border border-slate-800 space-y-3 animate-pulse">
        <div className="h-4 w-32 bg-slate-800 rounded" />
        <div className="h-16 bg-slate-950/70 rounded" />
      </div>
    );
  }

  if (!slaRisk) return null;

  const getRiskColors = (level: string) => {
    switch (level) {
      case 'CRITICAL':
        return {
          border: 'border-rose-500/40',
          text: 'text-rose-400',
          bg: 'bg-rose-500/10',
          badge: 'bg-rose-500/20 text-rose-300 border-rose-500/30',
          bar: 'bg-rose-500',
        };
      case 'HIGH':
        return {
          border: 'border-amber-500/40',
          text: 'text-amber-400',
          bg: 'bg-amber-500/10',
          badge: 'bg-amber-500/20 text-amber-300 border-amber-500/30',
          bar: 'bg-amber-500',
        };
      case 'MEDIUM':
        return {
          border: 'border-blue-500/40',
          text: 'text-blue-400',
          bg: 'bg-blue-500/10',
          badge: 'bg-blue-500/20 text-blue-300 border-blue-500/30',
          bar: 'bg-blue-500',
        };
      default:
        return {
          border: 'border-emerald-500/40',
          text: 'text-emerald-400',
          bg: 'bg-emerald-500/10',
          badge: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
          bar: 'bg-emerald-500',
        };
    }
  };

  const colors = getRiskColors(slaRisk.riskLevel);

  return (
    <div className={`p-5 rounded-xl bg-slate-900/90 border ${colors.border} shadow-xl space-y-3.5 relative overflow-hidden`}>
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className={`p-1.5 rounded-lg ${colors.bg} ${colors.text} border border-current`}>
            <ShieldAlert className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider font-mono text-white">
              SLA Risk Engine
            </h3>
            <span className="text-[10px] text-slate-400">Dynamic Multi-Factor Anomaly Scoring</span>
          </div>
        </div>
        <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${colors.badge}`}>
          {slaRisk.riskLevel}
        </span>
      </div>

      {/* Probability Gauge Bar */}
      <div className="p-3 rounded-lg bg-slate-950/80 border border-slate-800">
        <div className="flex items-center justify-between mb-1.5 text-xs font-mono">
          <span className="text-slate-400">Breach Probability:</span>
          <span className={`font-extrabold ${colors.text} text-sm`}>
            {slaRisk.riskScore}%
          </span>
        </div>
        <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
          <div
            className={`h-full ${colors.bar} transition-all duration-500`}
            style={{ width: `${slaRisk.riskScore}%` }}
          />
        </div>
        <div className="flex justify-between items-center text-[10px] text-slate-500 mt-2 font-mono">
          <span>Remaining SLA: {slaRisk.remainingSlaMinutes}m</span>
          <span>Transit ETA: {slaRisk.estimatedArrivalMinutes}m</span>
        </div>
      </div>

      {/* Summary Reasoning Text */}
      <p className="text-xs text-slate-300 leading-relaxed font-sans">
        {slaRisk.summaryRecommendation}
      </p>

      {/* Expand Contributing Factors */}
      <div className="pt-2 border-t border-slate-800/80">
        <button
          onClick={() => setShowFactors(!showFactors)}
          className="w-full flex items-center justify-between text-xs text-slate-400 hover:text-white transition py-1"
        >
          <span className="font-medium flex items-center gap-1.5">
            <Info className="w-3.5 h-3.5 text-slate-500" />
            <span>View Contributing Risk Factors ({slaRisk.contributingFactors.length})</span>
          </span>
          {showFactors ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
        </button>

        {showFactors && (
          <div className="mt-2.5 space-y-2 animate-fadeIn">
            {slaRisk.contributingFactors.map((f: any, i: number) => (
              <div key={i} className="p-2 rounded bg-slate-950/70 border border-slate-800/90 text-xs">
                <div className="flex justify-between items-center mb-0.5">
                  <span className="font-semibold text-slate-200">{f.factorName.replace('_', ' ')}</span>
                  <span className={`font-mono font-bold ${f.impactScore > 10 ? 'text-rose-400' : 'text-slate-400'}`}>
                    +{f.impactScore}
                  </span>
                </div>
                <p className="text-[11px] text-slate-400 leading-snug">{f.explanation}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

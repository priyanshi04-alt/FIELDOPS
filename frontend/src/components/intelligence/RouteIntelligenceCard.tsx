import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { 
  Navigation, MapPin, Clock, ShieldCheck, 
  AlertTriangle, XCircle, ArrowRight
} from 'lucide-react';
import { intelligenceApi } from '@/services/api';

interface RouteIntelligenceCardProps {
  workOrderId: number;
  technicianId?: number;
}

export const RouteIntelligenceCard: React.FC<RouteIntelligenceCardProps> = ({
  workOrderId,
  technicianId,
}) => {
  const { data: route, isLoading } = useQuery({
    queryKey: ['route-estimate', workOrderId, technicianId],
    queryFn: () => intelligenceApi.getRouteEstimate(workOrderId, technicianId),
    enabled: !!workOrderId,
  });

  if (isLoading) {
    return (
      <div className="p-4 rounded-xl bg-slate-900/80 border border-slate-800 space-y-2 animate-pulse">
        <div className="h-4 w-32 bg-slate-800 rounded" />
        <div className="h-12 bg-slate-950/70 rounded" />
      </div>
    );
  }

  if (!route) return null;

  const getFeasibilityBadge = (status: string) => {
    switch (status) {
      case 'FEASIBLE':
        return {
          icon: <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />,
          text: 'FEASIBLE',
          style: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30',
        };
      case 'AT_RISK':
        return {
          icon: <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />,
          text: 'AT RISK',
          style: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
        };
      case 'UNFEASIBLE':
      case 'BREACHED':
        return {
          icon: <XCircle className="w-3.5 h-3.5 text-rose-400" />,
          text: 'BREACH HAZARD',
          style: 'bg-rose-500/15 text-rose-400 border-rose-500/30',
        };
      default:
        return {
          icon: <Clock className="w-3.5 h-3.5 text-blue-400" />,
          text: status,
          style: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
        };
    }
  };

  const badge = getFeasibilityBadge(route.travelFeasibility);

  return (
    <div className="p-5 rounded-xl bg-slate-900/90 border border-slate-800 shadow-xl space-y-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-lg bg-cyan-500/15 text-cyan-400 border border-cyan-500/30">
            <Navigation className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider font-mono text-white">
              Route & Transit Telemetry
            </h3>
            <span className="text-[10px] text-slate-400">Great-Circle Haversine Vector</span>
          </div>
        </div>
        <span className={`inline-flex items-center gap-1 text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${badge.style}`}>
          {badge.icon}
          <span>{badge.text}</span>
        </span>
      </div>

      {/* Metric Grid */}
      <div className="grid grid-cols-3 gap-2 text-center">
        <div className="p-2.5 rounded-lg bg-slate-950/70 border border-slate-800">
          <span className="text-[10px] text-slate-400 block font-mono">Distance</span>
          <span className="text-base font-bold text-white font-mono mt-0.5 block">
            {route.distanceKm} km
          </span>
        </div>

        <div className="p-2.5 rounded-lg bg-slate-950/70 border border-slate-800">
          <span className="text-[10px] text-slate-400 block font-mono">Estimated ETA</span>
          <span className="text-base font-bold text-cyan-400 font-mono mt-0.5 block">
            {route.estimatedTravelMinutes} min
          </span>
        </div>

        <div className="p-2.5 rounded-lg bg-slate-950/70 border border-slate-800">
          <span className="text-[10px] text-slate-400 block font-mono">SLA Remaining</span>
          <span className="text-base font-bold text-slate-200 font-mono mt-0.5 block">
            {route.slaRemainingMinutes} min
          </span>
        </div>
      </div>

      {/* Transit Path Node Info */}
      <div className="p-2.5 rounded-lg bg-slate-950/50 border border-slate-800/80 text-[11px] text-slate-400 space-y-1">
        <div className="flex items-center justify-between">
          <span>Technician: <strong className="text-slate-200">{route.technicianName}</strong></span>
          <span className="text-[10px] font-mono text-slate-500">
            {route.technicianLatitude?.toFixed(2)}°, {route.technicianLongitude?.toFixed(2)}°
          </span>
        </div>
        <div className="flex items-center justify-between pt-1 border-t border-slate-900">
          <span className="truncate pr-2">Destination: <strong className="text-slate-200">{route.destinationAddress || 'Site Facility'}</strong></span>
          <span className="text-[10px] font-mono text-slate-500 shrink-0">
            {route.destinationLatitude?.toFixed(2)}°, {route.destinationLongitude?.toFixed(2)}°
          </span>
        </div>
      </div>
    </div>
  );
};

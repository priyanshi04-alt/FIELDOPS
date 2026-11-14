import React, { useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import { Technician, WorkOrder, OptimizedRoute } from '@/types';
import { Link } from 'react-router-dom';
import { ShieldAlert, User, Clock, Phone, MapPin, CheckCircle, Navigation, Flag, AlertTriangle } from 'lucide-react';

interface LiveOperationsMapProps {
  technicians?: Technician[];
  workOrders?: WorkOrder[];
  optimizedRoute?: OptimizedRoute | null;
  center?: [number, number];
  zoom?: number;
  height?: string;
  selectedId?: number;
}

// Custom DivIcons for high-polish SaaS aesthetic
const createTechIcon = (status: string) => {
  const color =
    status === 'AVAILABLE'
      ? '#10b981'
      : status === 'ON_ROUTE'
      ? '#3b82f6'
      : status === 'BUSY'
      ? '#f59e0b'
      : '#64748b';

  return L.divIcon({
    className: 'custom-marker-pin',
    html: `
      <div style="
        position: relative;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <div style="
          position: absolute;
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: ${color}25;
          animation: ping 2s cubic-bezier(0, 0, 0.2, 1) infinite;
        "></div>
        <div style="
          width: 26px;
          height: 26px;
          border-radius: 50%;
          background: #0f172a;
          border: 2px solid ${color};
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 12px rgba(0,0,0,0.4);
          color: white;
        ">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
        </div>
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 16],
  });
};

const createWorkOrderIcon = (priority: string, probability: number) => {
  const isCritical = priority === 'CRITICAL' || probability >= 75;
  const color =
    priority === 'CRITICAL'
      ? '#ef4444'
      : priority === 'HIGH'
      ? '#f97316'
      : priority === 'MEDIUM'
      ? '#3b82f6'
      : '#10b981';

  return L.divIcon({
    className: 'custom-marker-pin',
    html: `
      <div style="
        position: relative;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        ${
          isCritical
            ? `<div style="
                position: absolute;
                width: 36px;
                height: 36px;
                border-radius: 8px;
                background: rgba(239, 68, 68, 0.35);
                animation: ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite;
              "></div>`
            : ''
        }
        <div style="
          width: 24px;
          height: 24px;
          border-radius: 6px;
          background: ${color};
          border: 2px solid #ffffff;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 10px rgba(0,0,0,0.5);
          color: white;
          font-weight: 800;
          font-size: 11px;
        ">
          !
        </div>
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 16],
  });
};

const createStopMarkerIcon = (sequenceNumber: number, slaStatus: string) => {
  const color =
    slaStatus === 'VIOLATION'
      ? '#ef4444'
      : slaStatus === 'AT_RISK'
      ? '#f59e0b'
      : '#10b981';

  return L.divIcon({
    className: 'custom-stop-marker-pin',
    html: `
      <div style="
        position: relative;
        width: 34px;
        height: 34px;
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <div style="
          position: absolute;
          width: 34px;
          height: 34px;
          border-radius: 50%;
          background: ${color}33;
        "></div>
        <div style="
          width: 26px;
          height: 26px;
          border-radius: 50%;
          background: #090d16;
          border: 2px solid ${color};
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 12px rgba(0,0,0,0.8);
          color: ${color};
          font-weight: 900;
          font-family: monospace;
          font-size: 12px;
        ">
          ${sequenceNumber}
        </div>
      </div>
    `,
    iconSize: [34, 34],
    iconAnchor: [17, 17],
  });
};

export const LiveOperationsMap: React.FC<LiveOperationsMapProps> = ({
  technicians = [],
  workOrders = [],
  optimizedRoute = null,
  center = [28.5355, 77.2500], // Delhi NCR center
  zoom = 10,
  height = '480px',
  selectedId,
}) => {
  // Multi-stop polyline path
  const multiStopPositions = useMemo(() => {
    if (!optimizedRoute || !optimizedRoute.stops || optimizedRoute.stops.length === 0) {
      return [];
    }
    const coords: [number, number][] = [];
    if (optimizedRoute.originLat && optimizedRoute.originLon) {
      coords.push([optimizedRoute.originLat, optimizedRoute.originLon]);
    }
    optimizedRoute.stops.forEach((s) => {
      if (s.latitude && s.longitude) {
        coords.push([s.latitude, s.longitude]);
      }
    });
    return coords;
  }, [optimizedRoute]);

  return (
    <div className="relative w-full rounded-xl overflow-hidden border border-slate-800 bg-slate-950 shadow-inner" style={{ height }}>
      {/* Map Legend Overlay */}
      <div className="absolute top-3 left-3 z-[400] bg-slate-950/90 backdrop-blur-md border border-slate-800 px-3 py-2 rounded-lg text-xs space-y-1.5 shadow-xl">
        <div className="text-[11px] font-semibold text-slate-300 uppercase tracking-wider mb-1">
          Live Geo-Telemetry
        </div>
        <div className="flex items-center gap-2 text-slate-400">
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 shadow-sm shadow-emerald-500/50"></span>
          <span>Available Tech ({technicians.filter((t) => t.status === 'AVAILABLE').length})</span>
        </div>
        <div className="flex items-center gap-2 text-slate-400">
          <span className="w-2.5 h-2.5 rounded-full bg-blue-500 shadow-sm shadow-blue-500/50"></span>
          <span>En Route ({technicians.filter((t) => t.status === 'ON_ROUTE').length})</span>
        </div>
        {optimizedRoute ? (
          <div className="flex items-center gap-2 text-cyan-400 pt-1 border-t border-slate-800">
            <span className="w-2.5 h-2.5 rounded-full bg-cyan-400 border border-cyan-300 animate-pulse"></span>
            <span className="font-medium">Active Multi-Stop Tour ({optimizedRoute.totalStops} Stops)</span>
          </div>
        ) : (
          <div className="flex items-center gap-2 text-slate-400">
            <span className="w-2.5 h-2.5 rounded bg-rose-500 animate-pulse"></span>
            <span>SLA At Risk / Critical</span>
          </div>
        )}
      </div>

      <MapContainer
        center={center}
        zoom={zoom}
        scrollWheelZoom={false}
        className="w-full h-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://carto.com/">CARTO</a> &amp; OpenStreetMap'
          url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
          maxZoom={18}
        />

        {/* Technician Markers */}
        {technicians.map((tech) => {
          if (!tech.latitude || !tech.longitude) return null;
          return (
            <Marker
              key={`tech-${tech.id}`}
              position={[tech.latitude, tech.longitude]}
              icon={createTechIcon(tech.status)}
            >
              <Popup>
                <div className="p-1 min-w-[220px] text-slate-200">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-2 mb-2">
                    <span className="font-semibold text-sm text-white">{tech.name}</span>
                    <span
                      className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                        tech.status === 'AVAILABLE'
                          ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                          : tech.status === 'ON_ROUTE'
                          ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                          : 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                      }`}
                    >
                      {tech.status}
                    </span>
                  </div>

                  <div className="space-y-1 text-xs text-slate-400 mb-3">
                    <div className="flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-slate-500" />
                      <span className="truncate">{tech.address || tech.city}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Phone className="w-3.5 h-3.5 text-slate-500" />
                      <span>{tech.phone}</span>
                    </div>
                    <div className="flex items-center gap-2 pt-1 font-mono text-[11px] text-slate-300">
                      <span>Rating: ★{tech.rating}</span>
                      <span>•</span>
                      <span>SLA: {tech.slaSuccessRate}%</span>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-1 mb-3">
                    {tech.skills.slice(0, 3).map((s) => (
                      <span
                        key={s}
                        className="text-[9px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700"
                      >
                        {s}
                      </span>
                    ))}
                  </div>

                  <Link
                    to={`/app/technicians`}
                    className="block w-full py-1.5 text-center text-xs font-medium bg-blue-600 hover:bg-blue-500 text-white rounded transition-colors"
                  >
                    View Technician Profile
                  </Link>
                </div>
              </Popup>
            </Marker>
          );
        })}

        {/* Standard Work Order Markers (rendered when no multi-stop route is focused) */}
        {!optimizedRoute && workOrders.map((wo) => {
          if (!wo.latitude || !wo.longitude) return null;
          return (
            <Marker
              key={`wo-${wo.id}`}
              position={[wo.latitude, wo.longitude]}
              icon={createWorkOrderIcon(wo.priority, wo.slaBreachProbability)}
            >
              <Popup>
                <div className="p-1 min-w-[240px] text-slate-200">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-2 mb-2">
                    <span className="font-mono font-bold text-sm text-blue-400">
                      {wo.workOrderNumber}
                    </span>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                        wo.priority === 'CRITICAL'
                          ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                          : wo.priority === 'HIGH'
                          ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                          : 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                      }`}
                    >
                      {wo.priority}
                    </span>
                  </div>

                  <h4 className="text-xs font-semibold text-slate-100 mb-1 leading-snug">
                    {wo.title}
                  </h4>
                  <p className="text-[11px] text-slate-400 mb-2 truncate">
                    {wo.customer?.company || wo.locationName}
                  </p>

                  <div className="p-2 rounded bg-slate-950 border border-slate-800/80 mb-3 space-y-1 font-mono text-[11px]">
                    <div className="flex justify-between items-center">
                      <span className="text-slate-400">SLA Breach Risk:</span>
                      <span
                        className={`font-semibold ${
                          wo.slaBreachProbability >= 60
                            ? 'text-rose-400'
                            : 'text-emerald-400'
                        }`}
                      >
                        {wo.slaBreachProbability}%
                      </span>
                    </div>
                    {wo.technicianEtaMinutes !== undefined && wo.technicianEtaMinutes !== null && (
                      <div className="flex justify-between items-center text-slate-400">
                        <span>Technician ETA:</span>
                        <span className="text-slate-200">{wo.technicianEtaMinutes} min</span>
                      </div>
                    )}
                  </div>

                  <Link
                    to={`/app/work-orders/${wo.id}`}
                    className="block w-full py-1.5 text-center text-xs font-medium bg-slate-800 hover:bg-slate-700 text-blue-400 border border-slate-700 rounded transition-colors"
                  >
                    Open Dispatch View
                  </Link>
                </div>
              </Popup>
            </Marker>
          );
        })}

        {/* Phase 3: Multi-Stop Numbered Markers */}
        {optimizedRoute && optimizedRoute.stops.map((stop) => {
          if (!stop.latitude || !stop.longitude) return null;
          return (
            <Marker
              key={`route-stop-${stop.workOrderId}`}
              position={[stop.latitude, stop.longitude]}
              icon={createStopMarkerIcon(stop.sequenceNumber, stop.slaStatus)}
            >
              <Popup>
                <div className="p-1 min-w-[260px] text-slate-200">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-2 mb-2">
                    <div className="flex items-center gap-2">
                      <span className="w-5 h-5 rounded-full bg-cyan-500/20 text-cyan-300 font-mono font-bold text-xs flex items-center justify-center border border-cyan-500/40">
                        {stop.sequenceNumber}
                      </span>
                      <span className="font-mono font-bold text-sm text-cyan-400">
                        {stop.workOrderNumber}
                      </span>
                    </div>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                        stop.slaStatus === 'VIOLATION'
                          ? 'bg-rose-500/20 text-rose-400 border-rose-500/30'
                          : stop.slaStatus === 'AT_RISK'
                          ? 'bg-amber-500/20 text-amber-400 border-amber-500/30'
                          : 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                      }`}
                    >
                      {stop.slaStatus}
                    </span>
                  </div>

                  <h4 className="text-xs font-semibold text-slate-100 mb-1 leading-snug">
                    {stop.title}
                  </h4>
                  <p className="text-[11px] text-slate-400 mb-2 truncate">
                    {stop.customerName} • {stop.address}
                  </p>

                  <div className="p-2.5 rounded bg-slate-950 border border-slate-800/80 mb-3 space-y-1 font-mono text-[11px]">
                    <div className="flex justify-between items-center text-slate-400">
                      <span>Leg Transit:</span>
                      <span className="text-slate-200">
                        {stop.distanceFromPreviousKm} km ({stop.travelTimeFromPreviousMinutes} min)
                      </span>
                    </div>
                    <div className="flex justify-between items-center text-slate-400">
                      <span>Service Duration:</span>
                      <span className="text-slate-200">{stop.serviceDurationMinutes} min</span>
                    </div>
                    <div className="flex justify-between items-center text-slate-400">
                      <span>Est. Arrival:</span>
                      <span className="text-cyan-300">
                        {new Date(stop.estimatedArrival).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex justify-between items-center text-slate-400 pt-1 border-t border-slate-900">
                      <span>SLA Remaining:</span>
                      <span className={stop.slaRemainingMinutes < 0 ? 'text-rose-400 font-bold' : 'text-emerald-400 font-bold'}>
                        {stop.slaRemainingMinutes} min
                      </span>
                    </div>
                  </div>

                  <Link
                    to={`/app/work-orders/${stop.workOrderId}`}
                    className="block w-full py-1.5 text-center text-xs font-medium bg-slate-800 hover:bg-slate-700 text-cyan-400 border border-slate-700 rounded transition-colors"
                  >
                    Open Work Order Details →
                  </Link>
                </div>
              </Popup>
            </Marker>
          );
        })}

        {/* Phase 3: Multi-Stop Polyline Vectors */}
        {multiStopPositions.length > 1 && (
          <Polyline
            positions={multiStopPositions}
            pathOptions={{
              color: '#06b6d4',
              weight: 4,
              dashArray: '8, 8',
              opacity: 0.95,
            }}
          >
            <Tooltip sticky>
              <div className="font-mono text-xs">
                <strong>{optimizedRoute?.technicianName} Optimized Route</strong><br />
                {optimizedRoute?.totalStops} Stops • {optimizedRoute?.totalDistanceKm} km Total<br />
                Travel: {optimizedRoute?.totalTravelTimeMinutes} min • Saved: ~{optimizedRoute?.timeSavedMinutes} min
              </div>
            </Tooltip>
          </Polyline>
        )}

        {/* Standard Single-Leg Route Trajectories (when no multi-stop route is focused) */}
        {!optimizedRoute && workOrders.map((wo) => {
          if (!wo.technician || !wo.technician.latitude || !wo.technician.longitude || !wo.latitude || !wo.longitude) {
            return null;
          }
          const isSelected = selectedId === wo.id;
          const isCritical = wo.priority === 'CRITICAL' || wo.slaBreachProbability >= 60;
          const routeColor = isCritical ? '#f43f5e' : '#6366f1';

          return (
            <Polyline
              key={`route-line-${wo.id}`}
              positions={[
                [wo.technician.latitude, wo.technician.longitude],
                [wo.latitude, wo.longitude],
              ]}
              pathOptions={{
                color: routeColor,
                weight: isSelected ? 4 : 2,
                dashArray: '6, 8',
                opacity: isSelected ? 0.9 : 0.6,
              }}
            >
              <Tooltip sticky>
                <div className="font-mono text-xs">
                  <strong>{wo.workOrderNumber} Route Vector</strong><br />
                  Tech: {wo.technician.name} → Site ({wo.distanceKm || 6.5} km)<br />
                  ETA: ~{wo.technicianEtaMinutes || 25} min
                </div>
              </Tooltip>
            </Polyline>
          );
        })}
      </MapContainer>
    </div>
  );
};

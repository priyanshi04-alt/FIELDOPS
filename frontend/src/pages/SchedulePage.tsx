import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  Calendar as CalendarIcon,
  Clock,
  User,
  ChevronLeft,
  ChevronRight,
  Filter,
  MapPin,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';
import { workOrdersApi, techniciansApi } from '@/services/api';
import { WorkOrder, Technician } from '@/types';

export const SchedulePage: React.FC = () => {
  const [view, setView] = useState<'day' | 'week'>('day');
  const [selectedTechId, setSelectedTechId] = useState<number | 'ALL'>('ALL');

  const { data: workOrders = [] } = useQuery({
    queryKey: ['work-orders'],
    queryFn: () => workOrdersApi.getAll(),
  });

  const { data: technicians = [] } = useQuery({
    queryKey: ['technicians'],
    queryFn: () => techniciansApi.getAll(),
  });

  const timeSlots = [
    '08:00',
    '09:00',
    '10:00',
    '11:00',
    '12:00',
    '13:00',
    '14:00',
    '15:00',
    '16:00',
    '17:00',
    '18:00',
  ];

  // Specific schedule sample mappings for top work orders
  const scheduledBlocks: Array<{
    time: string;
    woNumber: string;
    title: string;
    techName: string;
    customer: string;
    priority: string;
    city: string;
    id?: number;
  }> = [
    {
      time: '09:00',
      woNumber: 'WO-1024',
      title: 'Central Chiller Compressor Vibration Alert',
      techName: 'Deepika Nair',
      customer: 'DLF CyberPark Facilities',
      priority: 'MEDIUM',
      city: 'Gurgaon',
      id: 4,
    },
    {
      time: '11:00',
      woNumber: 'WO-1031',
      title: '500kW Solar Inverter Synchronization Failure',
      techName: 'Ananya Singh',
      customer: 'Noida Tech One Campus',
      priority: 'HIGH',
      city: 'Noida',
      id: 5,
    },
    {
      time: '14:00',
      woNumber: 'WO-1042',
      title: 'Critical HVAC Inverter Failure - ICU Chiller Loop',
      techName: 'Rahul Sharma',
      customer: 'Fortis Healthcare North',
      priority: 'CRITICAL',
      city: 'Gurgaon',
      id: 1,
    },
    {
      time: '16:00',
      woNumber: 'WO-1047',
      title: 'Cold Storage Main Switchgear Thermal Anomaly',
      techName: 'Amit Patel',
      customer: 'Vertex Logistics Park',
      priority: 'HIGH',
      city: 'Gurgaon',
      id: 2,
    },
  ];

  return (
    <div className="p-4 lg:p-8 space-y-6 max-w-[1700px] mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h1 className="text-xl lg:text-2xl font-bold tracking-tight text-white font-sans">
            Dispatch Dispatch & Shift Schedule
          </h1>
          <p className="text-xs text-slate-400">
            Resource time allocations, planned appointments, and preventative maintenance windows.
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Day / Week Switcher */}
          <div className="flex items-center bg-slate-900 border border-slate-800 rounded-lg p-1">
            <button
              onClick={() => setView('day')}
              className={`px-3 py-1.5 text-xs font-medium rounded transition-colors ${
                view === 'day'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Day View
            </button>
            <button
              onClick={() => setView('week')}
              className={`px-3 py-1.5 text-xs font-medium rounded transition-colors ${
                view === 'week'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Week View
            </button>
          </div>

          <div className="flex items-center gap-1 bg-slate-900 border border-slate-800 rounded-lg p-1">
            <button className="p-1.5 text-slate-400 hover:text-white rounded">
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-xs font-medium px-2 text-slate-200">Today, Active Shift</span>
            <button className="p-1.5 text-slate-400 hover:text-white rounded">
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Technician Selector Filter */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2">
        <span className="text-xs font-medium text-slate-400 shrink-0">Engineer:</span>
        <button
          onClick={() => setSelectedTechId('ALL')}
          className={`px-3 py-1.5 text-xs font-medium rounded-lg border whitespace-nowrap transition-colors ${
            selectedTechId === 'ALL'
              ? 'bg-blue-600 text-white border-blue-500'
              : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'
          }`}
        >
          All Field Engineers
        </button>
        {technicians.slice(0, 8).map((tech) => (
          <button
            key={tech.id}
            onClick={() => setSelectedTechId(tech.id)}
            className={`px-3 py-1.5 text-xs font-medium rounded-lg border whitespace-nowrap transition-colors flex items-center gap-1.5 ${
              selectedTechId === tech.id
                ? 'bg-blue-600 text-white border-blue-500'
                : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'
            }`}
          >
            <span
              className={`w-2 h-2 rounded-full ${
                tech.status === 'AVAILABLE' ? 'bg-emerald-400' : 'bg-blue-400'
              }`}
            />
            <span>{tech.name}</span>
          </button>
        ))}
      </div>

      {/* Schedule Time Slots Timeline View */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-sm">
        <div className="space-y-4">
          {timeSlots.map((time) => {
            const block = scheduledBlocks.find((b) => b.time === time);

            return (
              <div key={time} className="flex items-start gap-4 group">
                {/* Time Stamp */}
                <div className="w-16 pt-2 font-mono text-xs font-bold text-slate-400 shrink-0">
                  {time}
                </div>

                {/* Timeline Axis Line */}
                <div className="relative flex flex-col items-center self-stretch">
                  <div
                    className={`w-3 h-3 rounded-full border-2 ${
                      block
                        ? block.priority === 'CRITICAL'
                          ? 'bg-rose-500 border-rose-400'
                          : 'bg-blue-500 border-blue-400'
                        : 'bg-slate-900 border-slate-700'
                    }`}
                  />
                  <div className="w-0.5 flex-1 bg-slate-800 group-last:bg-transparent" />
                </div>

                {/* Slot Content */}
                <div className="flex-1 pb-6">
                  {block ? (
                    <div
                      className={`p-4 rounded-xl border transition-all ${
                        block.priority === 'CRITICAL'
                          ? 'bg-rose-950/20 border-rose-800/60 shadow-lg shadow-rose-950/30'
                          : 'bg-slate-950 border-slate-800 hover:border-slate-700'
                      }`}
                    >
                      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-2">
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-xs font-bold text-blue-400">
                            {block.woNumber}
                          </span>
                          <span
                            className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                              block.priority === 'CRITICAL'
                                ? 'bg-rose-500/15 text-rose-400 border-rose-500/30'
                                : 'bg-amber-500/15 text-amber-400 border-amber-500/30'
                            }`}
                          >
                            {block.priority}
                          </span>
                        </div>

                        <div className="flex items-center gap-2 text-xs text-slate-400">
                          <User className="w-3.5 h-3.5 text-slate-500" />
                          <span className="text-slate-200 font-medium">{block.techName}</span>
                          <span>•</span>
                          <MapPin className="w-3.5 h-3.5 text-slate-500" />
                          <span>{block.city}</span>
                        </div>
                      </div>

                      <h3 className="text-sm font-semibold text-white mb-1">{block.title}</h3>
                      <p className="text-xs text-slate-400">{block.customer}</p>

                      <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs">
                        <span className="text-slate-500">Duration Block: 2h 00m</span>
                        <Link
                          to={`/app/work-orders/${block.id || 1}`}
                          className="text-blue-400 hover:underline font-medium"
                        >
                          View Work Order →
                        </Link>
                      </div>
                    </div>
                  ) : (
                    <div className="h-10 flex items-center text-xs text-slate-600 border border-dashed border-slate-800/60 rounded-lg px-4 hover:border-slate-700 hover:text-slate-400 cursor-pointer transition-colors">
                      + Open dispatch slot (Click to allocate)
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

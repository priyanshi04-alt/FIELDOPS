import React from 'react';
import { AlertCenter } from '@/components/alerts/AlertCenter';

export const AlertsPage: React.FC = () => {
  return (
    <div className="p-4 lg:p-8 space-y-6 max-w-[1700px] mx-auto">
      <div>
        <h1 className="text-xl lg:text-2xl font-bold tracking-tight text-white font-sans">
          Operational Alert Center
        </h1>
        <p className="text-xs text-slate-400">
          Real-time deterministic incident monitoring, SLA breach mitigation, and field workforce guardrails.
        </p>
      </div>

      <AlertCenter embedded={false} />
    </div>
  );
};

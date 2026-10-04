import React, { useState } from 'react';
import { 
  Sliders, Bell, Shield, MapPin, Database, Cpu, 
  Check, Save, AlertCircle, Key
} from 'lucide-react';
import { useAuth } from '../features/auth/AuthContext';

export const SettingsPage: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'general' | 'sla' | 'dispatch' | 'api'>('general');
  const [savedNotice, setSavedNotice] = useState(false);

  // Configuration state
  const [slaWarningMinutes, setSlaWarningMinutes] = useState(45);
  const [autoDispatchRadiusKm, setAutoDispatchRadiusKm] = useState(25);
  const [allowOvertimeDispatch, setAllowOvertimeDispatch] = useState(false);
  const [requireCustomerSignoff, setRequireCustomerSignoff] = useState(true);
  const [smsAlertsEnabled, setSmsAlertsEnabled] = useState(true);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSavedNotice(true);
    setTimeout(() => setSavedNotice(false), 3000);
  };

  return (
    <div className="p-8 space-y-8 max-w-5xl mx-auto">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-semibold text-slate-100 tracking-tight">Platform Configuration</h1>
        <p className="text-sm text-slate-400 mt-1">
          Tune operational parameters, automated dispatch thresholds, SLA guardrails, and enterprise integrations.
        </p>
      </div>

      {savedNotice && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 rounded-xl flex items-center gap-3 text-emerald-400 text-sm animate-fade-in">
          <Check className="w-5 h-5 flex-shrink-0" />
          <span>Configuration changes successfully applied to active cluster nodes.</span>
        </div>
      )}

      {/* Tabs */}
      <div className="flex border-b border-slate-800 gap-6">
        <button
          onClick={() => setActiveTab('general')}
          className={`pb-3 text-sm font-medium transition flex items-center gap-2 border-b-2 -mb-px ${
            activeTab === 'general'
              ? 'border-indigo-500 text-indigo-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Sliders className="w-4 h-4" />
          General & Organization
        </button>

        <button
          onClick={() => setActiveTab('sla')}
          className={`pb-3 text-sm font-medium transition flex items-center gap-2 border-b-2 -mb-px ${
            activeTab === 'sla'
              ? 'border-indigo-500 text-indigo-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Shield className="w-4 h-4" />
          SLA Breach Policies
        </button>

        <button
          onClick={() => setActiveTab('dispatch')}
          className={`pb-3 text-sm font-medium transition flex items-center gap-2 border-b-2 -mb-px ${
            activeTab === 'dispatch'
              ? 'border-indigo-500 text-indigo-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <MapPin className="w-4 h-4" />
          Dispatch Engine Rules
        </button>

        <button
          onClick={() => setActiveTab('api')}
          className={`pb-3 text-sm font-medium transition flex items-center gap-2 border-b-2 -mb-px ${
            activeTab === 'api'
              ? 'border-indigo-500 text-indigo-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Key className="w-4 h-4" />
          API & Webhooks
        </button>
      </div>

      <form onSubmit={handleSave} className="space-y-6">
        {activeTab === 'general' && (
          <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 space-y-6 backdrop-blur-sm">
            <h3 className="text-base font-medium text-slate-200">Organization Settings</h3>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">Tenant Name</label>
                <input
                  type="text"
                  disabled
                  defaultValue="FieldOps Industrial Solutions (NCR Central)"
                  className="w-full px-3.5 py-2 bg-slate-950/60 border border-slate-800 rounded-lg text-sm text-slate-400 cursor-not-allowed"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">Primary Operational Timezone</label>
                <input
                  type="text"
                  disabled
                  defaultValue="Asia/Kolkata (IST - UTC+05:30)"
                  className="w-full px-3.5 py-2 bg-slate-950/60 border border-slate-800 rounded-lg text-sm text-slate-400 cursor-not-allowed"
                />
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800">
              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={smsAlertsEnabled}
                  onChange={(e) => setSmsAlertsEnabled(e.target.checked)}
                  className="rounded bg-slate-950 border-slate-800 text-indigo-600 focus:ring-indigo-500 h-4 w-4"
                />
                <div>
                  <div className="text-sm font-medium text-slate-200">Emergency Dispatch SMS Broadcast</div>
                  <div className="text-xs text-slate-500">Notify assigned technicians instantly when Critical priority tickets are issued.</div>
                </div>
              </label>
            </div>
          </div>
        )}

        {activeTab === 'sla' && (
          <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 space-y-6 backdrop-blur-sm">
            <h3 className="text-base font-medium text-slate-200">SLA Breach Warning Thresholds</h3>
            
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5">
                Pre-Breach Early Alert Buffer (Minutes)
              </label>
              <div className="flex items-center gap-4">
                <input
                  type="range"
                  min="15"
                  max="120"
                  step="5"
                  value={slaWarningMinutes}
                  onChange={(e) => setSlaWarningMinutes(parseInt(e.target.value))}
                  className="w-72 accent-indigo-500 cursor-pointer"
                />
                <span className="font-mono text-indigo-400 text-sm font-semibold">{slaWarningMinutes} minutes before deadline</span>
              </div>
              <p className="text-xs text-slate-500 mt-2">
                Triggers visual amber warning badges on the dashboard live operations map and alerts on-duty dispatchers.
              </p>
            </div>

            <div className="pt-4 border-t border-slate-800">
              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={requireCustomerSignoff}
                  onChange={(e) => setRequireCustomerSignoff(e.target.checked)}
                  className="rounded bg-slate-950 border-slate-800 text-indigo-600 focus:ring-indigo-500 h-4 w-4"
                />
                <div>
                  <div className="text-sm font-medium text-slate-200">Require Customer Digital OTP / Sign-off for Closure</div>
                  <div className="text-xs text-slate-500">Prevent technicians from marking jobs completed without verified customer confirmation.</div>
                </div>
              </label>
            </div>
          </div>
        )}

        {activeTab === 'dispatch' && (
          <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 space-y-6 backdrop-blur-sm">
            <h3 className="text-base font-medium text-slate-200">Intelligent Routing & Geofence</h3>
            
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5">
                Maximum Proximity Radius for Auto-Matching (km)
              </label>
              <div className="flex items-center gap-4">
                <input
                  type="range"
                  min="5"
                  max="60"
                  step="5"
                  value={autoDispatchRadiusKm}
                  onChange={(e) => setAutoDispatchRadiusKm(parseInt(e.target.value))}
                  className="w-72 accent-indigo-500 cursor-pointer"
                />
                <span className="font-mono text-indigo-400 text-sm font-semibold">{autoDispatchRadiusKm} km</span>
              </div>
              <p className="text-xs text-slate-500 mt-2">
                Limits recommendations to field technicians within this GPS radius to minimize transit latency.
              </p>
            </div>

            <div className="pt-4 border-t border-slate-800">
              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={allowOvertimeDispatch}
                  onChange={(e) => setAllowOvertimeDispatch(e.target.checked)}
                  className="rounded bg-slate-950 border-slate-800 text-indigo-600 focus:ring-indigo-500 h-4 w-4"
                />
                <div>
                  <div className="text-sm font-medium text-slate-200">Allow After-Hours Overtime Dispatch</div>
                  <div className="text-xs text-slate-500">Allow dispatching work orders to technicians outside their configured shift window if priority is Critical.</div>
                </div>
              </label>
            </div>
          </div>
        )}

        {activeTab === 'api' && (
          <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-6 space-y-6 backdrop-blur-sm">
            <h3 className="text-base font-medium text-slate-200">Service API Credentials</h3>
            
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5">Live Environment REST API Key</label>
              <div className="flex items-center gap-3">
                <input
                  type="password"
                  disabled
                  value="fld_live_8f332f57_9942a_secret_key"
                  className="w-full max-w-md px-3.5 py-2 bg-slate-950/60 border border-slate-800 rounded-lg text-sm font-mono text-slate-300"
                />
                <button
                  type="button"
                  onClick={() => alert('API Key copied to clipboard.')}
                  className="px-3 py-2 text-xs font-medium text-indigo-400 bg-indigo-500/10 hover:bg-indigo-500/20 border border-indigo-500/30 rounded-lg transition"
                >
                  Copy Key
                </button>
              </div>
              <p className="text-xs text-slate-500 mt-2">Used for enterprise ERP / CRM webhook ingestion.</p>
            </div>
          </div>
        )}

        {/* Submit */}
        <div className="flex justify-end pt-4">
          <button
            type="submit"
            className="inline-flex items-center gap-2 px-5 py-2.5 text-xs font-medium text-white bg-indigo-600 hover:bg-indigo-500 rounded-lg shadow-sm transition"
          >
            <Save className="w-4 h-4" />
            Save Operational Policies
          </button>
        </div>
      </form>
    </div>
  );
};

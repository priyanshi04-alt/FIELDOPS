import React from 'react';
import { Link } from 'react-router-dom';
import { Logo } from '../components/common/Logo';
import { 
  ArrowRight, ShieldCheck, Zap, Activity, Users, 
  MapPin, Clock, BarChart3, ChevronRight, CheckCircle2,
  Cpu, Sparkles, AlertTriangle, Layers
} from 'lucide-react';

export const LandingPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500 selection:text-white">
      {/* 1. NAVBAR */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-slate-950/80 backdrop-blur-md border-b border-slate-800/80">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <Logo size="md" />

          <div className="hidden md:flex items-center gap-8 text-sm text-slate-400">
            <a href="#features" className="hover:text-slate-200 transition">Product</a>
            <a href="#solutions" className="hover:text-slate-200 transition">Solutions</a>
            <a href="#workflow" className="hover:text-slate-200 transition">Operations</a>
            <a href="#metrics" className="hover:text-slate-200 transition">Analytics</a>
            <a href="#resources" className="hover:text-slate-200 transition">Resources</a>
          </div>

          <div className="flex items-center gap-4">
            <Link
              to="/login"
              className="text-sm font-medium text-slate-300 hover:text-white transition px-3 py-1.5"
            >
              Sign In
            </Link>
            <Link
              to="/app/overview"
              className="inline-flex items-center gap-2 text-sm font-medium bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg shadow-sm shadow-indigo-600/30 transition"
            >
              Live Demo
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </nav>

      {/* 2. HERO */}
      <section className="pt-36 pb-20 relative overflow-hidden">
        {/* Ambient background glows */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-[600px] h-[350px] bg-indigo-600/15 rounded-full blur-[120px] pointer-events-none" />
        <div className="absolute top-1/3 left-1/4 w-[300px] h-[250px] bg-cyan-600/10 rounded-full blur-[100px] pointer-events-none" />

        <div className="max-w-7xl mx-auto px-6 text-center relative z-10">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-900 border border-slate-800 text-xs text-indigo-400 mb-8 backdrop-blur-sm">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Next-Generation Field Service Orchestration</span>
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-500" />
            <span className="text-slate-400">v1.0 Live</span>
          </div>

          <h1 className="text-4xl sm:text-6xl lg:text-7xl font-bold tracking-tight text-slate-100 max-w-4xl mx-auto leading-[1.1]">
            Every field operation.{' '}
            <span className="bg-gradient-to-r from-indigo-400 via-sky-300 to-indigo-200 bg-clip-text text-transparent">
              One intelligent command center.
            </span>
          </h1>

          <p className="mt-6 text-lg sm:text-xl text-slate-400 max-w-2xl mx-auto font-normal leading-relaxed">
            FieldOps unifies technicians, work orders, telemetry scheduling, and SLA guardrails 
            into a real-time autonomous operational cockpit.
          </p>

          <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/app/overview"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-base font-medium shadow-lg shadow-indigo-600/25 transition cursor-pointer"
            >
              Explore Platform
              <ArrowRight className="w-4.5 h-4.5" />
            </Link>
            <Link
              to="/login"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 bg-slate-900 hover:bg-slate-800/80 border border-slate-800 text-slate-300 hover:text-white rounded-xl text-base font-medium transition cursor-pointer"
            >
              View Dispatcher Demo
            </Link>
          </div>

          {/* 3. PRODUCT PREVIEW SECTION */}
          <div className="mt-16 sm:mt-20 max-w-6xl mx-auto">
            <div className="rounded-2xl p-2 bg-gradient-to-b from-slate-800/80 via-slate-900/60 to-slate-950 border border-slate-800 shadow-2xl shadow-indigo-950/40">
              <div className="rounded-xl bg-slate-950 border border-slate-800/80 overflow-hidden text-left">
                {/* Browser-like window header */}
                <div className="h-10 bg-slate-900/80 border-b border-slate-800 px-4 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-rose-500/80" />
                    <div className="w-3 h-3 rounded-full bg-amber-500/80" />
                    <div className="w-3 h-3 rounded-full bg-emerald-500/80" />
                    <span className="ml-3 text-xs font-mono text-slate-500">app.fieldops.io/command-center</span>
                  </div>
                  <div className="text-[11px] font-mono text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                    ● 21 Technicians Live
                  </div>
                </div>

                {/* Dashboard preview screenshot mockup */}
                <div className="p-6 grid grid-cols-1 md:grid-cols-4 gap-4 bg-slate-950/60">
                  <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-lg">
                    <div className="text-xs text-slate-400">ACTIVE DISPATCHES</div>
                    <div className="text-2xl font-bold text-slate-100 mt-1">41</div>
                    <div className="text-xs text-emerald-400 mt-1">↑ 12% vs last week</div>
                  </div>
                  <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-lg">
                    <div className="text-xs text-amber-400">SLA RISK COUNT</div>
                    <div className="text-2xl font-bold text-amber-400 mt-1">13</div>
                    <div className="text-xs text-amber-500/80 mt-1">Critical WO-1042 at 82%</div>
                  </div>
                  <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-lg">
                    <div className="text-xs text-slate-400">ON-DUTY ENGINEERS</div>
                    <div className="text-2xl font-bold text-slate-100 mt-1">12 / 21</div>
                    <div className="text-xs text-slate-400 mt-1">GPS telemetry synced</div>
                  </div>
                  <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-lg">
                    <div className="text-xs text-emerald-400">RESOLVED TODAY</div>
                    <div className="text-2xl font-bold text-emerald-400 mt-1">12</div>
                    <div className="text-xs text-emerald-400 mt-1">98.2% on-time SLA</div>
                  </div>

                  {/* Simulated map & pipeline row */}
                  <div className="col-span-1 md:col-span-3 bg-slate-900/70 border border-slate-800 rounded-lg p-5 flex flex-col justify-between min-h-[200px]">
                    <div className="flex items-center justify-between text-xs">
                      <span className="font-semibold text-slate-200 flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-indigo-400" />
                        National Capital Region Field Telemetry
                      </span>
                      <span className="text-slate-500 font-mono">28.6139° N, 77.2090° E</span>
                    </div>
                    <div className="grid grid-cols-3 gap-3 my-4">
                      <div className="p-3 bg-slate-950/80 rounded border border-slate-800 text-xs">
                        <div className="text-indigo-400 font-mono font-medium">WO-1042</div>
                        <div className="text-slate-300 font-medium mt-1 truncate">Transformer Overheat</div>
                        <div className="text-[11px] text-rose-400 font-medium mt-1">SLA Breach: 82% Risk</div>
                      </div>
                      <div className="p-3 bg-slate-950/80 rounded border border-slate-800 text-xs">
                        <div className="text-indigo-400 font-mono font-medium">WO-1047</div>
                        <div className="text-slate-300 font-medium mt-1 truncate">UPS Inverter Flashing</div>
                        <div className="text-[11px] text-amber-400 font-medium mt-1">SLA Breach: 67% Risk</div>
                      </div>
                      <div className="p-3 bg-slate-950/80 rounded border border-slate-800 text-xs">
                        <div className="text-emerald-400 font-mono font-medium">WO-1051</div>
                        <div className="text-slate-300 font-medium mt-1 truncate">SCADA Telemetry Check</div>
                        <div className="text-[11px] text-emerald-400 font-medium mt-1">Technician On Site</div>
                      </div>
                    </div>
                    <div className="text-right">
                      <Link to="/app/overview" className="text-xs text-indigo-400 hover:underline">
                        Open Full Live Operations Console →
                      </Link>
                    </div>
                  </div>

                  <div className="bg-slate-900/70 border border-slate-800 rounded-lg p-5 flex flex-col justify-between">
                    <div>
                      <div className="text-xs font-semibold text-slate-200">SLA PREDICTOR</div>
                      <p className="text-[11px] text-slate-400 mt-1">Deterministic AI anomaly scoring</p>
                    </div>
                    <div className="space-y-3">
                      <div>
                        <div className="flex justify-between text-xs mb-1">
                          <span className="text-slate-400">WO-1042</span>
                          <span className="text-rose-400 font-bold">82%</span>
                        </div>
                        <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div className="bg-rose-500 h-full w-[82%]" />
                        </div>
                      </div>
                      <div>
                        <div className="flex justify-between text-xs mb-1">
                          <span className="text-slate-400">WO-1047</span>
                          <span className="text-amber-400 font-bold">67%</span>
                        </div>
                        <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div className="bg-amber-400 h-full w-[67%]" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 4. METRICS SECTION */}
      <section id="metrics" className="py-20 border-y border-slate-800/80 bg-slate-900/30">
        <div className="max-w-7xl mx-auto px-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
            <div>
              <div className="text-4xl sm:text-5xl font-bold text-slate-100 font-mono tracking-tight">1,842+</div>
              <p className="text-sm text-slate-400 mt-2 font-medium">Jobs Completed</p>
            </div>
            <div>
              <div className="text-4xl sm:text-5xl font-bold text-emerald-400 font-mono tracking-tight">96.4%</div>
              <p className="text-sm text-slate-400 mt-2 font-medium">Contractual SLA Success</p>
            </div>
            <div>
              <div className="text-4xl sm:text-5xl font-bold text-indigo-400 font-mono tracking-tight">128</div>
              <p className="text-sm text-slate-400 mt-2 font-medium">Active Field Engineers</p>
            </div>
            <div>
              <div className="text-4xl sm:text-5xl font-bold text-amber-400 font-mono tracking-tight">4.9/5</div>
              <p className="text-sm text-slate-400 mt-2 font-medium">Average CSAT Rating</p>
            </div>
          </div>
        </div>
      </section>

      {/* 5. CORE FEATURES SECTION */}
      <section id="features" className="py-24 max-w-7xl mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-xs font-semibold text-indigo-400 uppercase tracking-widest mb-3">Enterprise Capabilities</h2>
          <h3 className="text-3xl sm:text-4xl font-bold text-slate-100 tracking-tight">
            Built for mission-critical infrastructure
          </h3>
          <p className="mt-4 text-base text-slate-400">
            From smart dispatching to automated inventory stock depletion, every facet of your field service lifecycle is connected.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="bg-slate-900/50 border border-slate-800/80 rounded-2xl p-7 hover:border-slate-700 transition">
            <div className="p-3 bg-indigo-500/10 text-indigo-400 rounded-xl w-fit mb-5">
              <Layers className="w-6 h-6" />
            </div>
            <h4 className="text-lg font-semibold text-slate-100 mb-2">Work Order Orchestration</h4>
            <p className="text-sm text-slate-400 leading-relaxed">
              5-stage lifecycle state machine with real-time telemetry updates, priority tags, and customer audit notes.
            </p>
          </div>

          <div className="bg-slate-900/50 border border-slate-800/80 rounded-2xl p-7 hover:border-slate-700 transition">
            <div className="p-3 bg-cyan-500/10 text-cyan-400 rounded-xl w-fit mb-5">
              <MapPin className="w-6 h-6" />
            </div>
            <h4 className="text-lg font-semibold text-slate-100 mb-2">Live Operations Telemetry</h4>
            <p className="text-sm text-slate-400 leading-relaxed">
              OpenStreetMap + Leaflet integration displaying live GPS technician beacons, customer depots, and priority indicators.
            </p>
          </div>

          <div className="bg-slate-900/50 border border-slate-800/80 rounded-2xl p-7 hover:border-slate-700 transition">
            <div className="p-3 bg-rose-500/10 text-rose-400 rounded-xl w-fit mb-5">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <h4 className="text-lg font-semibold text-slate-100 mb-2">SLA Risk Intelligence</h4>
            <p className="text-sm text-slate-400 leading-relaxed">
              Predictive risk scoring algorithms calculate breach probability based on distance, technician backlog, and part availability.
            </p>
          </div>

          <div className="bg-slate-900/50 border border-slate-800/80 rounded-2xl p-7 hover:border-slate-700 transition">
            <div className="p-3 bg-emerald-500/10 text-emerald-400 rounded-xl w-fit mb-5">
              <Users className="w-6 h-6" />
            </div>
            <h4 className="text-lg font-semibold text-slate-100 mb-2">Technician Roster & Scheduling</h4>
            <p className="text-sm text-slate-400 leading-relaxed">
              Daily and weekly timeline visualization with skill taxonomy matching, utilization charts, and shift allocations.
            </p>
          </div>

          <div className="bg-slate-900/50 border border-slate-800/80 rounded-2xl p-7 hover:border-slate-700 transition">
            <div className="p-3 bg-amber-500/10 text-amber-400 rounded-xl w-fit mb-5">
              <Zap className="w-6 h-6" />
            </div>
            <h4 className="text-lg font-semibold text-slate-100 mb-2">Depot Parts & Inventory</h4>
            <p className="text-sm text-slate-400 leading-relaxed">
              Safety stock thresholds, SKU tracking, and instant stock consumption linked directly to field work orders.
            </p>
          </div>

          <div className="bg-slate-900/50 border border-slate-800/80 rounded-2xl p-7 hover:border-slate-700 transition">
            <div className="p-3 bg-purple-500/10 text-purple-400 rounded-xl w-fit mb-5">
              <BarChart3 className="w-6 h-6" />
            </div>
            <h4 className="text-lg font-semibold text-slate-100 mb-2">Executive Analytics</h4>
            <p className="text-sm text-slate-400 leading-relaxed">
              Resolution throughput curves, municipal volume heatmaps, and technician leaderboards for operational leaders.
            </p>
          </div>
        </div>
      </section>

      {/* 6. WORKFLOW VISUALIZATION STEPPER */}
      <section id="workflow" className="py-20 border-t border-slate-800/80 bg-slate-900/20">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-semibold text-indigo-400 uppercase tracking-widest mb-3">Service Lifecycle</h2>
            <h3 className="text-3xl font-bold text-slate-100">End-to-End Operational Pipeline</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-6 gap-4">
            {[
              { step: '01', title: 'Request Created', desc: 'Customer logs ticket via portal or automated SCADA webhook.' },
              { step: '02', title: 'Skill Assignment', desc: 'Dispatcher matches verified skills, GPS proximity, and availability.' },
              { step: '03', title: 'Live Dispatch', desc: 'Technician receives push notification with customer coordinates & tools.' },
              { step: '04', title: 'On-Site Service', desc: 'Engineer updates telemetry status and logs replacement parts.' },
              { step: '05', title: 'Verification', desc: 'Customer digital OTP sign-off and photographic inspection check.' },
              { step: '06', title: 'SLA Resolved', desc: 'Metrics logged to analytics pipeline and invoice finalized.' },
            ].map((st, i) => (
              <div key={st.step} className="bg-slate-900/60 border border-slate-800 p-5 rounded-xl relative">
                <span className="text-xs font-mono font-bold text-indigo-400">{st.step}</span>
                <h4 className="text-sm font-semibold text-slate-200 mt-2">{st.title}</h4>
                <p className="text-xs text-slate-400 mt-1 leading-relaxed">{st.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 7. FINAL CALL TO ACTION */}
      <section className="py-24 text-center relative overflow-hidden">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />
        
        <div className="max-w-4xl mx-auto px-6 relative z-10">
          <h2 className="text-3xl sm:text-5xl font-bold text-slate-100 tracking-tight">
            Bring your field operations under control.
          </h2>
          <p className="mt-4 text-base sm:text-lg text-slate-400 max-w-xl mx-auto">
            Deploy FieldOps today to eliminate SLA breaches, optimize routes, and empower your frontline engineers.
          </p>

          <div className="mt-8 flex justify-center gap-4">
            <Link
              to="/app/overview"
              className="inline-flex items-center gap-2 px-8 py-3.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-base font-medium shadow-lg shadow-indigo-600/30 transition"
            >
              Launch Live App Demo
              <ArrowRight className="w-5 h-5" />
            </Link>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="border-t border-slate-800 py-12 bg-slate-950">
        <div className="max-w-7xl mx-auto px-6 flex flex-col sm:flex-row items-center justify-between gap-6">
          <Logo size="sm" />
          <p className="text-xs text-slate-500">
            © 2026 FieldOps Inc. Intelligent Field Service Operations Platform. All rights reserved.
          </p>
          <div className="flex gap-6 text-xs text-slate-400">
            <a href="#privacy" className="hover:text-slate-300">Privacy</a>
            <a href="#terms" className="hover:text-slate-300">Terms</a>
            <a href="#status" className="hover:text-slate-300">System Status: All Systems Operational</a>
          </div>
        </div>
      </footer>
    </div>
  );
};

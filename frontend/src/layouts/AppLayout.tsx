import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  ClipboardList,
  Users,
  Calendar,
  Building2,
  Boxes,
  BarChart3,
  Settings,
  Search,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Sun,
  Moon,
  Menu,
  X,
  Radio,
  Shield,
  HelpCircle,
} from 'lucide-react';
import { useAuth } from '@/features/auth/AuthContext';
import { Logo } from '@/components/common/Logo';
import { NotificationPopover } from '@/components/common/NotificationPopover';
import { Role } from '@/types';

export const AppLayout: React.FC = () => {
  const { user, logout, quickLogin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [isDark, setIsDark] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  const navItems = [
    { label: 'Overview', to: '/app/overview', icon: LayoutDashboard },
    { label: 'Work Orders', to: '/app/work-orders', icon: ClipboardList, badge: '53' },
    { label: 'Technicians', to: '/app/technicians', icon: Users, badge: '21' },
    { label: 'Schedule', to: '/app/schedule', icon: Calendar },
    { label: 'Customers', to: '/app/customers', icon: Building2 },
    { label: 'Inventory', to: '/app/inventory', icon: Boxes },
    { label: 'Analytics', to: '/app/analytics', icon: BarChart3 },
    { label: 'Settings', to: '/app/settings', icon: Settings },
  ];

  const toggleTheme = () => {
    setIsDark(!isDark);
    if (!isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/app/work-orders?search=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const getRoleBadgeColor = (role?: Role) => {
    switch (role) {
      case 'ADMIN':
        return 'bg-purple-500/15 text-purple-400 border-purple-500/30';
      case 'DISPATCHER':
        return 'bg-blue-500/15 text-blue-400 border-blue-500/30';
      case 'TECHNICIAN':
        return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
      default:
        return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col antialiased selection:bg-blue-500/30 selection:text-white">
      {/* Top Header */}
      <header className="sticky top-0 z-40 h-16 bg-slate-950/80 backdrop-blur-md border-b border-slate-800/80 px-4 lg:px-6 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="p-2 rounded-lg text-slate-400 hover:text-slate-100 hover:bg-slate-800 lg:hidden focus:outline-none"
            aria-label="Toggle navigation"
          >
            {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>

          <Logo collapsed={false} to="/app/overview" className="hidden lg:flex" />
          <Logo collapsed={true} to="/app/overview" className="flex lg:hidden" />

          {/* Telemetry live status indicator */}
          <div className="hidden md:flex items-center gap-2 pl-4 border-l border-slate-800 ml-2">
            <span className="flex h-2 w-2 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            <span className="text-xs font-mono text-slate-400 font-medium">
              DISPATCH NOC <span className="text-emerald-400 font-semibold">99.98% SLA</span>
            </span>
          </div>
        </div>

        {/* Global Search Bar */}
        <form onSubmit={handleSearchSubmit} className="flex-1 max-w-md hidden sm:block">
          <div className="relative group">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 transition-colors group-focus-within:text-blue-400" />
            <input
              type="text"
              placeholder="Search work orders, technicians, assets... (Press Enter)"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-9 pl-9 pr-14 text-xs bg-slate-900 border border-slate-800 rounded-lg text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all"
            />
            <span className="absolute right-2.5 top-1/2 -translate-y-1/2 px-1.5 py-0.5 text-[10px] font-mono font-medium text-slate-500 bg-slate-800/80 rounded border border-slate-700/50">
              ⌘K
            </span>
          </div>
        </form>

        {/* Right Action Icons & Persona Switcher */}
        <div className="flex items-center gap-2">
          {/* Quick Persona Switcher for Evaluation */}
          <div className="hidden xl:flex items-center gap-1 bg-slate-900/90 border border-slate-800 rounded-lg p-0.5">
            <span className="text-[10px] font-semibold text-slate-500 px-2 uppercase tracking-wider">
              Role:
            </span>
            {(['ADMIN', 'DISPATCHER', 'TECHNICIAN', 'CUSTOMER'] as Role[]).map((r) => (
              <button
                key={r}
                onClick={() => quickLogin(r)}
                className={`text-[11px] font-medium px-2 py-1 rounded transition-all ${
                  user?.role === r
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                }`}
                title={`Switch to demo persona ${r}`}
              >
                {r.charAt(0) + r.slice(1).toLowerCase()}
              </button>
            ))}
          </div>

          <NotificationPopover />

          <button
            onClick={toggleTheme}
            className="p-2 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 transition-colors focus:outline-none"
            title="Toggle Theme"
          >
            {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
          </button>

          {/* User Profile dropdown */}
          <div className="flex items-center gap-2 pl-2 border-l border-slate-800">
            <div className="w-8 h-8 rounded-full overflow-hidden bg-slate-800 ring-1 ring-slate-700 shrink-0">
              {user?.avatarUrl ? (
                <img src={user.avatarUrl} alt={user.fullName} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-xs font-bold text-slate-300">
                  {user?.fullName?.charAt(0) || 'U'}
                </div>
              )}
            </div>
            <div className="hidden md:flex flex-col text-left">
              <span className="text-xs font-semibold text-slate-200 truncate max-w-[120px]">
                {user?.fullName || 'Sarah Jenkins'}
              </span>
              <span className={`text-[10px] font-medium px-1.5 py-0.2 rounded border w-fit ${getRoleBadgeColor(user?.role)}`}>
                {user?.role || 'ADMIN'}
              </span>
            </div>

            <button
              onClick={logout}
              className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors ml-1"
              title="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        {/* Desktop Sidebar */}
        <aside
          className={`hidden lg:flex flex-col border-r border-slate-800/80 bg-slate-950/60 backdrop-blur-sm transition-all duration-200 shrink-0 select-none ${
            collapsed ? 'w-20' : 'w-64'
          }`}
        >
          <div className="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `group flex items-center gap-3 px-3 py-2.5 rounded-lg text-xs font-medium transition-all ${
                      isActive
                        ? 'bg-blue-600/15 text-blue-400 border border-blue-500/30 shadow-sm'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60 border border-transparent'
                    }`
                  }
                  title={collapsed ? item.label : undefined}
                >
                  <Icon className="w-4 h-4 shrink-0 transition-transform group-hover:scale-105" />
                  {!collapsed && (
                    <div className="flex-1 flex items-center justify-between">
                      <span>{item.label}</span>
                      {item.badge && (
                        <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 group-hover:text-slate-200 border border-slate-700/60">
                          {item.badge}
                        </span>
                      )}
                    </div>
                  )}
                </NavLink>
              );
            })}
          </div>

          {/* Bottom Sidebar Info & Collapse Toggle */}
          <div className="p-3 border-t border-slate-800/80 bg-slate-950/40 space-y-2">
            {!collapsed && (
              <div className="px-2 py-1.5 rounded-lg bg-slate-900/80 border border-slate-800/60 text-[11px]">
                <div className="text-slate-400 truncate font-medium">
                  {user?.organization || 'FieldOps Global Operations'}
                </div>
                <div className="text-[10px] text-emerald-400 flex items-center gap-1.5 mt-0.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                  Active Node: Delhi-NCR
                </div>
              </div>
            )}

            <button
              onClick={() => setCollapsed(!collapsed)}
              className="w-full flex items-center justify-center gap-2 py-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-900/70 text-xs font-medium transition-colors"
            >
              {collapsed ? (
                <ChevronRight className="w-4 h-4" />
              ) : (
                <>
                  <ChevronLeft className="w-4 h-4" />
                  <span>Collapse Sidebar</span>
                </>
              )}
            </button>
          </div>
        </aside>

        {/* Mobile Navigation Drawer */}
        {mobileOpen && (
          <div className="fixed inset-0 z-50 lg:hidden flex">
            <div
              className="fixed inset-0 bg-black/70 backdrop-blur-sm transition-opacity"
              onClick={() => setMobileOpen(false)}
            />
            <div className="relative w-72 bg-slate-950 border-r border-slate-800 p-4 flex flex-col z-10">
              <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
                <Logo to="/app/overview" />
                <button
                  onClick={() => setMobileOpen(false)}
                  className="p-1.5 text-slate-400 hover:text-slate-200 rounded-lg"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="flex-1 space-y-1">
                {navItems.map((item) => {
                  const Icon = item.icon;
                  return (
                    <NavLink
                      key={item.to}
                      to={item.to}
                      onClick={() => setMobileOpen(false)}
                      className={({ isActive }) =>
                        `flex items-center gap-3 px-3 py-2.5 rounded-lg text-xs font-medium transition-colors ${
                          isActive
                            ? 'bg-blue-600/15 text-blue-400 border border-blue-500/30'
                            : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60'
                        }`
                      }
                    >
                      <Icon className="w-4 h-4" />
                      <span>{item.label}</span>
                    </NavLink>
                  );
                })}
              </div>

              <div className="pt-4 border-t border-slate-800">
                <button
                  onClick={() => {
                    logout();
                    setMobileOpen(false);
                  }}
                  className="flex items-center gap-2 w-full px-3 py-2 text-xs font-medium text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  Sign Out
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Main Workspace Content Area */}
        <main className="flex-1 overflow-y-auto bg-slate-950 min-w-0">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

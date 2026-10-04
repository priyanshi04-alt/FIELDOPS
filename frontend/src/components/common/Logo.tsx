import React from 'react';
import { Link } from 'react-router-dom';

interface LogoProps {
  collapsed?: boolean;
  to?: string;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

export const Logo: React.FC<LogoProps> = ({ collapsed = false, to = '/', className = '', size = 'md' }) => {
  const iconSizeClasses = size === 'sm' ? 'w-7 h-7' : size === 'lg' ? 'w-11 h-11' : 'w-9 h-9';
  const svgSizeClasses = size === 'sm' ? 'w-4 h-4' : size === 'lg' ? 'w-6 h-6' : 'w-5 h-5';
  const textSizeClasses = size === 'sm' ? 'text-sm' : size === 'lg' ? 'text-lg' : 'text-base';
  return (
    <Link to={to} className={`flex items-center gap-3 group transition-opacity hover:opacity-90 ${className}`}>
      <div className={`relative flex items-center justify-center rounded-lg bg-gradient-to-tr from-blue-600 via-blue-500 to-indigo-600 text-white shadow-lg shadow-blue-500/20 ring-1 ring-white/15 shrink-0 ${iconSizeClasses}`}>
        <svg
          className={`${svgSizeClasses} text-white transition-transform group-hover:scale-105`}
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2.2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M12 2L2 7l10 5 10-5-10-5z" />
          <path d="m2 17 10 5 10-5" />
          <path d="m2 12 10 5 10-5" />
        </svg>
        <span className="absolute -top-1 -right-1 flex h-2.5 w-2.5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75"></span>
          <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-blue-400"></span>
        </span>
      </div>

      {!collapsed && (
        <div className="flex flex-col">
          <div className="flex items-center gap-1.5">
            <span className={`font-bold tracking-tight text-slate-100 font-sans ${textSizeClasses}`}>
              FIELD<span className="text-blue-500">OPS</span>
            </span>
            <span className="text-[10px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
              v1.0
            </span>
          </div>
          <span className="text-[11px] font-medium text-slate-400 tracking-tight">
            Field Operations Intelligence
          </span>
        </div>
      )}
    </Link>
  );
};

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Building2,
  Search,
  MapPin,
  Phone,
  Mail,
  Star,
  Clock,
  Briefcase,
  X,
  ExternalLink,
} from 'lucide-react';
import { customersApi, workOrdersApi } from '@/services/api';
import { Customer } from '@/types';
import { Link } from 'react-router-dom';

export const CustomersPage: React.FC = () => {
  const [search, setSearch] = useState('');
  const [cityFilter, setCityFilter] = useState('ALL');
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);

  const { data: customers = [], isLoading } = useQuery({
    queryKey: ['customers'],
    queryFn: () => customersApi.getAll(),
  });

  const { data: allWorkOrders = [] } = useQuery({
    queryKey: ['work-orders'],
    queryFn: () => workOrdersApi.getAll(),
  });

  const filteredCustomers = React.useMemo(() => {
    return customers.filter((c) => {
      if (cityFilter !== 'ALL' && c.city?.toLowerCase() !== cityFilter.toLowerCase()) return false;
      if (search.trim()) {
        const q = search.toLowerCase();
        const matchName = c.name?.toLowerCase().includes(q);
        const matchComp = c.company?.toLowerCase().includes(q);
        const matchCity = c.city?.toLowerCase().includes(q);
        if (!matchName && !matchComp && !matchCity) return false;
      }
      return true;
    });
  }, [customers, cityFilter, search]);

  const customerWorkOrders = React.useMemo(() => {
    if (!selectedCustomer) return [];
    return allWorkOrders.filter((wo) => wo.customer?.id === selectedCustomer.id);
  }, [allWorkOrders, selectedCustomer]);

  return (
    <div className="p-4 lg:p-8 space-y-6 max-w-[1700px] mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h1 className="text-xl lg:text-2xl font-bold tracking-tight text-white font-sans">
            Enterprise Accounts & Facilities
          </h1>
          <p className="text-xs text-slate-400">
            Industrial campuses, hyperscale data centers, and multi-tenant commercial facilities.
          </p>
        </div>

        <span className="text-xs font-mono text-slate-400">
          Total Client Sites: <span className="text-white font-bold">{customers.length}</span>
        </span>
      </div>

      {/* Filter and Search Bar */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-3 bg-slate-900/60 p-3.5 rounded-xl border border-slate-800">
        <div className="md:col-span-8 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by company name, facility, city..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full h-9 pl-9 pr-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-blue-500"
          />
        </div>

        <div className="md:col-span-4">
          <select
            value={cityFilter}
            onChange={(e) => setCityFilter(e.target.value)}
            className="w-full h-9 px-3 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-300 focus:outline-none focus:border-blue-500"
          >
            <option value="ALL">All Hubs & Territories</option>
            <option value="Gurgaon">Gurgaon</option>
            <option value="Delhi">Delhi NCR</option>
            <option value="Noida">Noida</option>
            <option value="Jaipur">Jaipur</option>
            <option value="Chandigarh">Chandigarh</option>
          </select>
        </div>
      </div>

      {/* Customers Table */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 border-b border-slate-800/80 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="py-3 px-4">Facility & Company</th>
                <th className="py-3 px-4">Territory / Node</th>
                <th className="py-3 px-4">Primary Contact</th>
                <th className="py-3 px-4">Open Jobs</th>
                <th className="py-3 px-4">Total Serviced</th>
                <th className="py-3 px-4">Satisfaction</th>
                <th className="py-3 px-4">Last Service</th>
                <th className="py-3 px-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Loading customer directories...
                  </td>
                </tr>
              ) : filteredCustomers.map((c) => (
                <tr
                  key={c.id}
                  onClick={() => setSelectedCustomer(c)}
                  className="hover:bg-slate-800/40 transition-colors cursor-pointer"
                >
                  <td className="py-3.5 px-4">
                    <div className="font-semibold text-slate-200 text-sm">{c.company}</div>
                    <div className="text-[11px] text-slate-500 mt-0.5">{c.name}</div>
                  </td>

                  <td className="py-3.5 px-4">
                    <div className="flex items-center gap-1.5 text-slate-300">
                      <MapPin className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                      <span>{c.city}</span>
                    </div>
                  </td>

                  <td className="py-3.5 px-4 text-slate-400">
                    <div>{c.email}</div>
                    <div className="text-[11px] text-slate-500">{c.phone}</div>
                  </td>

                  <td className="py-3.5 px-4 font-mono font-bold">
                    <span className="text-blue-400 px-2 py-0.5 rounded bg-blue-500/10 border border-blue-500/20">
                      {c.openJobsCount || 1}
                    </span>
                  </td>

                  <td className="py-3.5 px-4 font-mono text-slate-300">
                    {c.completedJobsCount || 18}
                  </td>

                  <td className="py-3.5 px-4 font-mono">
                    <span className="text-amber-400">★ {c.satisfactionScore || 4.9}</span>
                  </td>

                  <td className="py-3.5 px-4 text-slate-400 text-[11px]">
                    {c.lastServiceDate
                      ? new Date(c.lastServiceDate).toLocaleDateString()
                      : 'Recent'}
                  </td>

                  <td className="py-3.5 px-4 text-right">
                    <button className="text-blue-400 hover:text-blue-300 font-medium">
                      Inspect →
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Customer Detail Slide-Over Modal */}
      {selectedCustomer && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-2xl w-full p-6 shadow-2xl relative space-y-5">
            <button
              onClick={() => setSelectedCustomer(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>

            <div>
              <span className="text-[10px] font-mono text-blue-400 uppercase tracking-wider">
                Enterprise Account Profile
              </span>
              <h2 className="text-xl font-bold text-white">{selectedCustomer.company}</h2>
              <p className="text-xs text-slate-400 mt-1">{selectedCustomer.name}</p>
            </div>

            <div className="grid grid-cols-3 gap-3 text-center text-xs font-mono">
              <div className="p-3 rounded-lg bg-slate-950 border border-slate-800">
                <span className="text-slate-500 text-[10px] block">OPEN JOBS</span>
                <span className="text-lg font-bold text-blue-400">
                  {selectedCustomer.openJobsCount}
                </span>
              </div>
              <div className="p-3 rounded-lg bg-slate-950 border border-slate-800">
                <span className="text-slate-500 text-[10px] block">TOTAL COMPLETED</span>
                <span className="text-lg font-bold text-emerald-400">
                  {selectedCustomer.completedJobsCount}
                </span>
              </div>
              <div className="p-3 rounded-lg bg-slate-950 border border-slate-800">
                <span className="text-slate-500 text-[10px] block">SATISFACTION</span>
                <span className="text-lg font-bold text-amber-400">
                  ★ {selectedCustomer.satisfactionScore}
                </span>
              </div>
            </div>

            {/* Address & Facility Notes */}
            <div className="p-4 rounded-lg bg-slate-950/80 border border-slate-800 space-y-2 text-xs">
              <div className="flex items-center gap-2 text-slate-300">
                <MapPin className="w-4 h-4 text-slate-500 shrink-0" />
                <span>
                  {selectedCustomer.address || selectedCustomer.city} ({selectedCustomer.city})
                </span>
              </div>
              <div className="flex items-center gap-2 text-slate-300">
                <Phone className="w-4 h-4 text-slate-500 shrink-0" />
                <span>{selectedCustomer.phone}</span>
              </div>
              <div className="flex items-center gap-2 text-slate-300">
                <Mail className="w-4 h-4 text-slate-500 shrink-0" />
                <span>{selectedCustomer.email}</span>
              </div>

              {selectedCustomer.notes && (
                <div className="pt-2 border-t border-slate-800/80 text-slate-400 italic">
                  Note: "{selectedCustomer.notes}"
                </div>
              )}
            </div>

            {/* Active Tickets for Customer */}
            <div>
              <h3 className="text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Recent Facility Work Orders
              </h3>
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {customerWorkOrders.length === 0 ? (
                  <div className="text-xs text-slate-500 italic p-3 text-center">
                    No active tickets currently logged for this facility.
                  </div>
                ) : (
                  customerWorkOrders.map((wo) => (
                    <Link
                      key={wo.id}
                      to={`/app/work-orders/${wo.id}`}
                      className="block p-3 rounded-lg bg-slate-950 border border-slate-800 hover:border-slate-700 transition-colors"
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-mono text-xs font-bold text-blue-400">
                          {wo.workOrderNumber}
                        </span>
                        <span className="text-[10px] font-semibold text-slate-400">
                          {wo.status}
                        </span>
                      </div>
                      <div className="text-xs text-slate-200 line-clamp-1">{wo.title}</div>
                    </Link>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

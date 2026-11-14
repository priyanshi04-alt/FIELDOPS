import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  Package, AlertTriangle, XCircle, CheckCircle2, 
  Search, Plus, Minus, ArrowUpDown, Filter, RefreshCw,
  Sparkles, TrendingUp, Info, HelpCircle
} from 'lucide-react';
import { inventoryApi, predictiveApi } from '../services/api';
import { InventoryItem, InventoryForecastDTO } from '../types';

export const InventoryPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [stockStatusFilter, setStockStatusFilter] = useState('ALL');
  const [selectedItem, setSelectedItem] = useState<InventoryItem | null>(null);
  const [selectedForecast, setSelectedForecast] = useState<InventoryForecastDTO | null>(null);
  const [adjustAmount, setAdjustAmount] = useState<number>(0);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { data: items = [], isLoading, refetch } = useQuery<InventoryItem[]>({
    queryKey: ['inventory'],
    queryFn: () => inventoryApi.getAll(),
  });

  const { data: predictions = [] } = useQuery<InventoryForecastDTO[]>({
    queryKey: ['inventory-predictions'],
    queryFn: () => predictiveApi.getInventoryPredictions(),
    refetchInterval: 30000,
  });

  const predictionMap = React.useMemo(() => {
    const map = new Map<number, InventoryForecastDTO>();
    predictions.forEach(p => map.set(p.inventoryItemId, p));
    return map;
  }, [predictions]);

  const adjustMutation = useMutation({
    mutationFn: ({ id, amount }: { id: number; amount: number }) => {
      return inventoryApi.adjustStock(id, amount, 'Manual technician depot adjustment');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      setSelectedItem(null);
      setAdjustAmount(0);
      setIsSubmitting(false);
    },
    onError: () => {
      setIsSubmitting(false);
    }
  });

  // Calculate metrics
  const totalItems = items.length;
  const totalUnits = items.reduce((acc: number, item: InventoryItem) => acc + (item.availableQuantity || 0), 0);
  const lowStockItems = items.filter((i: InventoryItem) => (i.availableQuantity || 0) > 0 && (i.availableQuantity || 0) <= i.minThreshold);
  const outOfStockItems = items.filter((i: InventoryItem) => (i.availableQuantity || 0) === 0);
  const totalValuation = items.reduce((acc: number, item: InventoryItem) => acc + ((item.availableQuantity || 0) * (item.unitPrice || 0)), 0);

  // Filter categories
  const categories: string[] = Array.from(new Set(items.map((i: InventoryItem) => i.category))).filter(Boolean);

  const filteredItems = items.filter((item: InventoryItem) => {
    const matchesSearch = item.name.toLowerCase().includes(search.toLowerCase()) || 
                          item.sku.toLowerCase().includes(search.toLowerCase()) ||
                          item.category.toLowerCase().includes(search.toLowerCase());
    const matchesCategory = categoryFilter === 'ALL' || item.category === categoryFilter;
    
    let matchesStock = true;
    const qty = item.availableQuantity || 0;
    if (stockStatusFilter === 'LOW') {
      matchesStock = qty > 0 && qty <= item.minThreshold;
    } else if (stockStatusFilter === 'OUT') {
      matchesStock = qty === 0;
    } else if (stockStatusFilter === 'NORMAL') {
      matchesStock = qty > item.minThreshold;
    }

    return matchesSearch && matchesCategory && matchesStock;
  });

  const handleAdjustStock = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedItem || adjustAmount === 0) return;
    setIsSubmitting(true);
    adjustMutation.mutate({ id: selectedItem.id, amount: adjustAmount });
  };

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-100 tracking-tight">Spare Parts & Inventory</h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time field depot tracking, minimum reorder thresholds, and technician replenishment.
          </p>
        </div>
        <button
          onClick={() => refetch()}
          className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-slate-300 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 transition"
        >
          <RefreshCw className="w-3.5 h-3.5 text-slate-400" />
          Sync Depot
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 relative overflow-hidden backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Catalog SKUs</span>
            <div className="p-2 bg-indigo-500/10 rounded-lg text-indigo-400">
              <Package className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-slate-100">{totalItems}</span>
            <span className="text-xs text-slate-400">({totalUnits.toLocaleString()} units)</span>
          </div>
          <p className="text-xs text-slate-500 mt-2">Active serviceable components</p>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 relative overflow-hidden backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-amber-400 uppercase tracking-wider">Low Stock SKUs</span>
            <div className="p-2 bg-amber-500/10 rounded-lg text-amber-400">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-amber-400">{lowStockItems.length}</span>
            <span className="text-xs text-amber-500/80">Need replenishment</span>
          </div>
          <p className="text-xs text-slate-500 mt-2">Below safety reorder threshold</p>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 relative overflow-hidden backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-rose-400 uppercase tracking-wider">Stock Depleted</span>
            <div className="p-2 bg-rose-500/10 rounded-lg text-rose-400">
              <XCircle className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-rose-400">{outOfStockItems.length}</span>
            <span className="text-xs text-rose-500/80">Blocking dispatch</span>
          </div>
          <p className="text-xs text-slate-500 mt-2">Immediate order placement required</p>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 relative overflow-hidden backdrop-blur-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-emerald-400 uppercase tracking-wider">Depot Valuation</span>
            <div className="p-2 bg-emerald-500/10 rounded-lg text-emerald-400">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-slate-100">
              ₹{(totalValuation / 100000).toFixed(2)}L
            </span>
            <span className="text-xs text-emerald-400">In warehouse</span>
          </div>
          <p className="text-xs text-slate-500 mt-2">Total inventory asset value</p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-4 flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="relative w-full md:w-80">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search part name, SKU, or category..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-950/80 border border-slate-800 rounded-lg text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500/60 transition"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          {/* Category Filter */}
          <div className="flex items-center gap-1.5">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="bg-slate-950/80 border border-slate-800 rounded-lg text-xs text-slate-300 py-2 px-3 focus:outline-none focus:border-indigo-500"
            >
              <option value="ALL">All Categories</option>
              {categories.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>

          {/* Stock Health Filter */}
          <select
            value={stockStatusFilter}
            onChange={(e) => setStockStatusFilter(e.target.value)}
            className="bg-slate-950/80 border border-slate-800 rounded-lg text-xs text-slate-300 py-2 px-3 focus:outline-none focus:border-indigo-500"
          >
            <option value="ALL">All Stock Statuses</option>
            <option value="NORMAL">Healthy Stock</option>
            <option value="LOW">Low Stock Alert</option>
            <option value="OUT">Out of Stock</option>
          </select>
        </div>
      </div>

      {/* Inventory Table */}
      <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl overflow-hidden shadow-sm">
        {isLoading ? (
          <div className="p-12 text-center text-slate-500 text-sm animate-pulse">
            Loading central warehouse depot registry...
          </div>
        ) : filteredItems.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            No parts or inventory records match the selected filters.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/40 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Item Name / SKU</th>
                  <th className="py-3.5 px-4">Category</th>
                  <th className="py-3.5 px-4">Available Units</th>
                  <th className="py-3.5 px-4">Stock Health</th>
                  <th className="py-3.5 px-4">
                    <span className="flex items-center gap-1 text-indigo-400">
                      <Sparkles className="w-3 h-3" />
                      Predicted 7D Demand
                    </span>
                  </th>
                  <th className="py-3.5 px-4">Replenishment Action</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {filteredItems.map((item: InventoryItem) => {
                  const qty = item.availableQuantity || 0;
                  const isOutOfStock = qty === 0;
                  const isLow = qty > 0 && qty <= item.minThreshold;
                  const percentage = Math.min(100, Math.round((qty / (item.minThreshold * 3 || 1)) * 100));

                  return (
                    <tr key={item.id} className="hover:bg-slate-800/30 transition">
                      <td className="py-3.5 px-4">
                        <div className="font-medium text-slate-200">{item.name}</div>
                        <div className="text-xs text-slate-500 font-mono mt-0.5">{item.sku}</div>
                      </td>
                      <td className="py-3.5 px-4 text-slate-400 text-xs">
                        <span className="px-2 py-0.5 bg-slate-800 rounded border border-slate-700/60">
                          {item.category}
                        </span>
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="font-semibold text-slate-100">{qty}</div>
                        <div className="text-xs text-slate-500">Min threshold: {item.minThreshold}</div>
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <div className="w-24 bg-slate-800 rounded-full h-1.5 overflow-hidden">
                            <div
                              className={`h-full rounded-full ${
                                isOutOfStock ? 'bg-rose-500' : isLow ? 'bg-amber-400' : 'bg-emerald-400'
                              }`}
                              style={{ width: `${isOutOfStock ? 5 : percentage}%` }}
                            />
                          </div>
                          <span
                            className={`text-xs px-2 py-0.5 rounded font-medium border ${
                              isOutOfStock
                                ? 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                                : isLow
                                ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                                : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                            }`}
                          >
                            {isOutOfStock ? 'Out of Stock' : isLow ? 'Low Stock' : 'Optimal'}
                          </span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        {predictionMap.has(item.id) ? (
                          <div>
                            <div className="font-mono font-bold text-indigo-300">
                              {predictionMap.get(item.id)?.predicted7DayDemand} units
                            </div>
                            <div className="text-[10px] text-slate-500 font-mono">
                              ~{predictionMap.get(item.id)?.dailyDemandRate}/day
                            </div>
                          </div>
                        ) : (
                          <span className="text-xs text-slate-500 font-mono">Calculating...</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4">
                        {predictionMap.has(item.id) ? (
                          (() => {
                            const pred = predictionMap.get(item.id)!;
                            return (
                              <div className="flex flex-col gap-1">
                                <span
                                  className={`text-[10px] px-2 py-0.5 rounded font-bold border w-fit ${
                                    pred.recommendation === 'REORDER'
                                      ? 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                                      : pred.recommendation === 'MONITOR'
                                      ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                                      : 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                                  }`}
                                >
                                  {pred.recommendation === 'REORDER'
                                    ? `REORDER +${pred.recommendedReorderQuantity}`
                                    : pred.recommendation}
                                </span>
                                <span className="text-[10px] text-slate-400">
                                  {pred.estimatedDaysUntilStockout}d stockout
                                </span>
                              </div>
                            );
                          })()
                        ) : (
                          <span className="text-xs text-slate-500">Evaluating...</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {predictionMap.has(item.id) && (
                            <button
                              onClick={() => setSelectedForecast(predictionMap.get(item.id)!)}
                              className="px-2 py-1 text-xs font-medium text-slate-300 bg-slate-800 hover:bg-slate-700 rounded-lg transition"
                              title="View ML Forecast Details"
                            >
                              Forecast
                            </button>
                          )}
                          <button
                            onClick={() => {
                              setSelectedItem(item);
                              setAdjustAmount(0);
                            }}
                            className="px-2.5 py-1 text-xs font-medium text-indigo-400 bg-indigo-500/10 hover:bg-indigo-500/20 border border-indigo-500/30 rounded-lg transition"
                          >
                            Adjust
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Adjust Stock Modal */}
      {selectedItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-xl w-full max-w-md p-6 shadow-2xl relative">
            <h2 className="text-lg font-semibold text-slate-100">Adjust Inventory Stock</h2>
            <p className="text-xs text-slate-400 mt-1">
              {selectedItem.name} ({selectedItem.sku})
            </p>

            <div className="my-6 p-4 bg-slate-950/60 rounded-lg border border-slate-800/80 space-y-2">
              <div className="flex justify-between text-xs text-slate-400">
                <span>Current Stock:</span>
                <span className="font-semibold text-slate-200">{selectedItem.availableQuantity || 0} units</span>
              </div>
              <div className="flex justify-between text-xs text-slate-400">
                <span>Safety Min Threshold:</span>
                <span className="font-semibold text-slate-200">{selectedItem.minThreshold} units</span>
              </div>
              <div className="flex justify-between text-xs text-slate-400">
                <span>New Resulting Quantity:</span>
                <span className={`font-semibold ${
                  ((selectedItem.availableQuantity || 0) + adjustAmount) <= selectedItem.minThreshold ? 'text-amber-400' : 'text-emerald-400'
                }`}>
                  {Math.max(0, (selectedItem.availableQuantity || 0) + adjustAmount)} units
                </span>
              </div>
            </div>

            <form onSubmit={handleAdjustStock} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">
                  Adjustment Delta (Positive to Restock, Negative to Consume)
                </label>
                <div className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={() => setAdjustAmount(prev => prev - 1)}
                    className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700"
                  >
                    <Minus className="w-4 h-4" />
                  </button>
                  <input
                    type="number"
                    value={adjustAmount}
                    onChange={(e) => setAdjustAmount(parseInt(e.target.value) || 0)}
                    className="w-full text-center py-2 bg-slate-950 border border-slate-800 rounded-lg text-slate-100 font-mono text-sm focus:outline-none focus:border-indigo-500"
                  />
                  <button
                    type="button"
                    onClick={() => setAdjustAmount(prev => prev + 1)}
                    className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setSelectedItem(null)}
                  className="px-4 py-2 text-xs font-medium text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 rounded-lg transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting || adjustAmount === 0}
                  className="px-4 py-2 text-xs font-medium text-white bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 disabled:pointer-events-none rounded-lg shadow-sm transition"
                >
                  {isSubmitting ? 'Updating...' : 'Commit Stock Change'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Predictive ML Forecast Modal */}
      {selectedForecast && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-slate-900 border border-indigo-500/40 rounded-xl w-full max-w-xl p-6 shadow-2xl relative space-y-4">
            <div className="flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                    {selectedForecast.algorithm}
                  </span>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                    selectedForecast.recommendation === 'REORDER'
                      ? 'bg-rose-500/20 text-rose-300 border-rose-500/30'
                      : 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                  }`}>
                    {selectedForecast.recommendation}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-white mt-1">
                  Predictive Replenishment: {selectedForecast.partName}
                </h3>
                <p className="text-xs text-slate-400 font-mono">Category: {selectedForecast.category}</p>
              </div>
              <button
                onClick={() => setSelectedForecast(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
              >
                ✕
              </button>
            </div>

            {/* Metrics Grid */}
            <div className="grid grid-cols-3 gap-3">
              <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
                <span className="text-[10px] text-slate-400 block font-mono">CURRENT STOCK</span>
                <span className="text-base font-bold text-white">{selectedForecast.currentStock} units</span>
              </div>
              <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
                <span className="text-[10px] text-slate-400 block font-mono">7D ML FORECAST</span>
                <span className="text-base font-bold text-indigo-400">{selectedForecast.predicted7DayDemand} units</span>
              </div>
              <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800">
                <span className="text-[10px] text-slate-400 block font-mono">DAYS TO STOCKOUT</span>
                <span className={`text-base font-bold ${selectedForecast.estimatedDaysUntilStockout <= 7 ? 'text-rose-400' : 'text-emerald-400'}`}>
                  {selectedForecast.estimatedDaysUntilStockout} days
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800 space-y-1">
                <div className="flex justify-between text-slate-400">
                  <span>Safety Buffer Stock:</span>
                  <span className="text-slate-200 font-bold">{selectedForecast.safetyStock} units</span>
                </div>
                <div className="flex justify-between text-slate-400">
                  <span>Supplier Lead Time:</span>
                  <span className="text-slate-200 font-bold">{selectedForecast.supplierLeadDays} days</span>
                </div>
                <div className="flex justify-between text-slate-400">
                  <span>Daily Consumption Rate:</span>
                  <span className="text-indigo-300 font-bold font-mono">{selectedForecast.dailyDemandRate}/day</span>
                </div>
              </div>
              <div className="p-3 rounded-lg bg-slate-950/70 border border-slate-800 space-y-1">
                <div className="flex justify-between text-slate-400">
                  <span>Recommended Order:</span>
                  <span className="text-rose-400 font-bold">+{selectedForecast.recommendedReorderQuantity} units</span>
                </div>
                <div className="flex justify-between text-slate-400">
                  <span>Baseline (Moving Avg):</span>
                  <span className="text-slate-300 font-mono">30D Rolling</span>
                </div>
                <div className="flex justify-between text-slate-400">
                  <span>Evaluation Metric:</span>
                  <span className="text-slate-300 font-mono">MAE 3.01 / R² 0.69</span>
                </div>
              </div>
            </div>

            {/* Explanation */}
            <div className="p-3 rounded-lg bg-indigo-500/10 border border-indigo-500/20 text-xs text-indigo-200">
              <p className="font-semibold mb-0.5">Decision Rationale:</p>
              <p>{selectedForecast.explanation}</p>
            </div>

            {/* Synthetic Data Disclaimer */}
            {selectedForecast.syntheticDisclaimer && (
              <div className="text-[10px] text-amber-400/80 bg-amber-500/10 border border-amber-500/20 rounded p-2">
                ⚠️ {selectedForecast.syntheticDisclaimer}
              </div>
            )}

            <div className="flex justify-end pt-2 border-t border-slate-800">
              <button
                onClick={() => setSelectedForecast(null)}
                className="px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg transition"
              >
                Close Telemetry
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

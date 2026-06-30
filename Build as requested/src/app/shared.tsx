// ─── Shared Types ─────────────────────────────────────────────────────────────
export type Role = "mangaka" | "assistant" | "editor" | "board";
export type Page = string;

// ─── Constants ────────────────────────────────────────────────────────────────
export const roleLabels: Record<Role, string> = {
  mangaka: "Mangaka",
  assistant: "Assistant",
  editor: "Tantou Editor",
  board: "Editorial Board",
};

export const roleColors: Record<Role, string> = {
  mangaka: "from-purple-600 to-indigo-600",
  assistant: "from-blue-600 to-cyan-500",
  editor: "from-violet-600 to-purple-500",
  board: "from-indigo-600 to-blue-600",
};

// ─── Sample Data ──────────────────────────────────────────────────────────────
export const rankingData = [
  { week: "W1", rank: 12 }, { week: "W2", rank: 9 }, { week: "W3", rank: 7 },
  { week: "W4", rank: 5 }, { week: "W5", rank: 4 }, { week: "W6", rank: 3 },
  { week: "W7", rank: 2 }, { week: "W8", rank: 2 },
];

export const readerData = [
  { month: "Jan", reads: 42000 }, { month: "Feb", reads: 58000 },
  { month: "Mar", reads: 71000 }, { month: "Apr", reads: 65000 },
  { month: "May", reads: 89000 }, { month: "Jun", reads: 112000 },
];

export const earningsData = [
  { month: "Jan", earned: 85000 }, { month: "Feb", earned: 120000 },
  { month: "Mar", earned: 95000 }, { month: "Apr", earned: 140000 },
  { month: "May", earned: 165000 }, { month: "Jun", earned: 198000 },
];

export const analyticsData = [
  { name: "Action", value: 35, color: "#7c3aed" },
  { name: "Romance", value: 28, color: "#2563eb" },
  { name: "Fantasy", value: 22, color: "#06b6d4" },
  { name: "Slice of Life", value: 15, color: "#10b981" },
];

export const productionData = [
  { month: "Apr", completed: 18, revisions: 4 },
  { month: "May", completed: 22, revisions: 6 },
  { month: "Jun", completed: 25, revisions: 3 },
  { month: "Jul", completed: 19, revisions: 5 },
];

// ─── Badge Component ──────────────────────────────────────────────────────────
const statusColors: Record<string, string> = {
  "Draft": "bg-gray-100 text-gray-600",
  "Pending Review": "bg-amber-50 text-amber-700 border border-amber-200",
  "Needs Revision": "bg-red-50 text-red-700 border border-red-200",
  "Pending Board Approval": "bg-blue-50 text-blue-700 border border-blue-200",
  "Approved": "bg-emerald-50 text-emerald-700 border border-emerald-200",
  "Serialized": "bg-purple-50 text-purple-700 border border-purple-200",
  "Cancelled": "bg-gray-100 text-gray-500",
  "In Progress": "bg-blue-50 text-blue-700 border border-blue-200",
  "Completed": "bg-emerald-50 text-emerald-700 border border-emerald-200",
  "Revision Requested": "bg-red-50 text-red-700 border border-red-200",
  "New": "bg-purple-50 text-purple-700 border border-purple-200",
  "On Hold": "bg-gray-50 text-gray-600 border border-gray-200",
  "High Risk": "bg-red-100 text-red-700 border border-red-300",
};

export function Badge({ status }: { status: string }) {
  const cls = statusColors[status] ?? "bg-gray-100 text-gray-600";
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${cls}`}>
      {status}
    </span>
  );
}

// ─── Stat Card ────────────────────────────────────────────────────────────────
import { ArrowUpRight, ArrowDownRight } from "lucide-react";

export function StatCard({
  label, value, icon: Icon, trend, trendVal, gradient
}: {
  label: string; value: string | number; icon: React.ElementType;
  trend?: "up" | "down"; trendVal?: string; gradient?: string;
}) {
  return (
    <div className={`bg-white rounded-xl p-5 border border-purple-50 shadow-sm hover:shadow-md transition-shadow ${gradient ? "relative overflow-hidden" : ""}`}>
      {gradient && (
        <div className={`absolute inset-0 opacity-5 ${gradient}`} />
      )}
      <div className="relative flex items-start justify-between">
        <div>
          <p className="text-sm text-gray-500 font-medium">{label}</p>
          <p className="text-2xl font-bold text-gray-900 mt-1">{value}</p>
          {trendVal && (
            <div className={`flex items-center gap-1 mt-1.5 text-xs font-medium ${trend === "up" ? "text-emerald-600" : "text-red-500"}`}>
              {trend === "up" ? <ArrowUpRight size={13} /> : <ArrowDownRight size={13} />}
              {trendVal}
            </div>
          )}
        </div>
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-500 to-blue-500 flex items-center justify-center text-white shadow-sm">
          <Icon size={18} />
        </div>
      </div>
    </div>
  );
}

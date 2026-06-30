import { BookOpen, Star, Clock, Award, Users, Activity, AlertTriangle } from "lucide-react";
import { StatCard, Badge } from "../ui-helpers";

const stats = [
  { label: "Active Series", value: "3", icon: BookOpen, trend: "up" as const, trendVal: "+1 this month" },
  { label: "Pending Proposals", value: "2", icon: Clock, trend: "up" as const, trendVal: "1 awaiting editor" },
  { label: "Board Approval", value: "68%", icon: Award, trend: "up" as const, trendVal: "+4% this week" },
  { label: "Readers", value: "112k", icon: Users, trend: "up" as const, trendVal: "+8.2%" },
];

const recentItems = [
  { title: "New draft created", detail: "Ch. 1 proposal submitted for review", status: "Draft" },
  { title: "Editor feedback", detail: "Revision requested on synopsis", status: "Needs Revision" },
  { title: "Board result", detail: "Weekly vote pending", status: "Pending Board Approval" },
];

export default function MangakaDashboard() {
  return (
    <div className="p-6 space-y-6 bg-[#f3f3f3] min-h-full">
      <div className="flex items-end justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Welcome, Mangaka</h1>
          <p className="text-sm text-gray-500 mt-1">Track proposals, production, and publication flow.</p>
        </div>
        <div className="flex items-center gap-2 text-xs text-gray-500"><Activity size={14} /> Live workflow</div>
      </div>

      <div className="grid grid-cols-4 gap-4">
        {stats.map((s) => <StatCard key={s.label} {...s} />)}
      </div>

      <div className="grid grid-cols-3 gap-5">
        <div className="col-span-2 bg-white rounded-2xl border border-purple-50 shadow-sm p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-gray-900">Workflow snapshot</h2>
            <span className="text-xs text-gray-500">Updated now</span>
          </div>
          <div className="space-y-3">
            {recentItems.map((item) => (
              <div key={item.title} className="flex items-center justify-between rounded-xl bg-[#f3f3f3] px-4 py-3">
                <div>
                  <div className="text-sm font-semibold text-gray-800">{item.title}</div>
                  <div className="text-xs text-gray-500 mt-0.5">{item.detail}</div>
                </div>
                <Badge status={item.status} />
              </div>
            ))}
          </div>
        </div>

        <div className="bg-[#a3a3a3] rounded-2xl p-5 shadow-sm text-white">
          <h2 className="font-semibold mb-3">Alerts</h2>
          <div className="space-y-3 text-sm text-white/90">
            <div className="flex gap-2"><AlertTriangle size={16} className="mt-0.5" /> Review proposal before board vote.</div>
            <div className="flex gap-2"><Star size={16} className="mt-0.5" /> Create draft from Proposals page.</div>
          </div>
        </div>
      </div>
    </div>
  );
}

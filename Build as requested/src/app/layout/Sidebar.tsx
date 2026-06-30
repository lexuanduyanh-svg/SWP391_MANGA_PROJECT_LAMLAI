import {
  LayoutDashboard, BookOpen, FileText, CheckSquare, Activity, TrendingUp,
  Bell, User, Package, Send, DollarSign, Edit3, BarChart2, ThumbsUp, Calendar,
  Shield, PenTool, Search
} from "lucide-react";
import { Role, roleLabels, roleColors } from "../shared";

type SidebarItem = { icon: React.ElementType; label: string; page: string };

const sidebarItems: Record<Role, SidebarItem[]> = {
  mangaka: [
    { icon: LayoutDashboard, label: "Dashboard", page: "dashboard" },
    { icon: BookOpen, label: "My Series", page: "series" },
    { icon: FileText, label: "Proposals", page: "proposals" },
    { icon: CheckSquare, label: "Task Dashboard", page: "tasks" },
    { icon: Activity, label: "Production Dashboard", page: "production" },
    { icon: TrendingUp, label: "Rankings", page: "rankings" },
    { icon: Bell, label: "Notifications", page: "notifications" },
    { icon: User, label: "Profile", page: "profile" },
  ],
  assistant: [
    { icon: LayoutDashboard, label: "Dashboard", page: "dashboard" },
    { icon: CheckSquare, label: "Assigned Tasks", page: "tasks" },
    { icon: Package, label: "References", page: "references" },
    { icon: Send, label: "Submissions", page: "submissions" },
    { icon: DollarSign, label: "Earnings", page: "earnings" },
    { icon: Bell, label: "Notifications", page: "notifications" },
    { icon: User, label: "Profile", page: "profile" },
  ],
  editor: [
    { icon: LayoutDashboard, label: "Dashboard", page: "dashboard" },
    { icon: FileText, label: "Proposal Reviews", page: "proposals" },
    { icon: Edit3, label: "Manuscript Review", page: "manuscript" },
    { icon: Activity, label: "Production Monitor", page: "production" },
    { icon: BarChart2, label: "Defense Reports", page: "reports" },
    { icon: Bell, label: "Notifications", page: "notifications" },
    { icon: User, label: "Profile", page: "profile" },
  ],
  board: [
    { icon: LayoutDashboard, label: "Dashboard", page: "dashboard" },
    { icon: ThumbsUp, label: "Proposal Voting", page: "voting" },
    { icon: Calendar, label: "Publishing Schedule", page: "schedule" },
    { icon: BarChart2, label: "Reader Data", page: "reader-data" },
    { icon: TrendingUp, label: "Rankings & Analytics", page: "analytics" },
    { icon: Shield, label: "Series Decisions", page: "decisions" },
    { icon: Bell, label: "Notifications", page: "notifications" },
    { icon: User, label: "Profile", page: "profile" },
  ],
};

export default function Sidebar({ role, currentPage, onNavigate }: {
  role: Role; currentPage: string; onNavigate: (page: string) => void;
}) {
  const items = sidebarItems[role];
  return (
    <div className="w-[280px] min-h-screen bg-[#a3a3a3] flex flex-col shadow-sm">
      <div className="px-5 py-5 border-b border-black/10">
        <div className="flex items-center gap-2.5">
          <div className={`w-8 h-8 rounded-lg bg-gradient-to-br ${roleColors[role]} flex items-center justify-center text-white shadow-sm`}>
            <PenTool size={15} />
          </div>
          <div>
            <div className="text-sm font-bold text-white">MangaFlow</div>
            <div className="text-[10px] text-white/70 font-medium uppercase tracking-wider">{roleLabels[role]}</div>
          </div>
        </div>
      </div>
      <nav className="flex-1 px-4 py-5 space-y-2">
        {items.map(({ icon: Icon, label, page }) => {
          const active = currentPage === page;
          return (
            <button
              key={page}
              onClick={() => onNavigate(page)}
              className={`w-full flex items-center gap-3 px-4 py-[13px] rounded-xl text-sm font-semibold transition-all ${
                active
                  ? "bg-white text-gray-800 shadow-sm underline decoration-purple-500 decoration-2 underline-offset-2"
                  : "bg-[#d4d4d4] text-gray-700 hover:bg-white hover:shadow-sm"
              }`}
            >
              <Icon size={15} className={active ? "text-purple-600" : "text-gray-500"} />
              {label}
            </button>
          );
        })}
      </nav>
      <div className="px-4 py-4 border-t border-black/10">
        <div className="text-xs text-white/50 text-center">MangaFlow v2.4.1</div>
      </div>
    </div>
  );
}

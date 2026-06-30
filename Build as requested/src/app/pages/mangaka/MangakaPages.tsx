import { Award, Users } from "lucide-react";
import { StatCard } from "../ui-helpers";
import DraftForm from "./DraftForm";
import ProposalList from "./ProposalList";
import ProductionDashboard from "./ProductionDashboard";

function MangakaDashboard() {
  return (
    <div className="p-6 space-y-6 bg-[#f3f3f3] min-h-full">
      <h1 className="text-3xl font-bold text-gray-900">Welcome, Mangaka</h1>
      <div className="grid grid-cols-3 gap-4">
        <StatCard label="Current Rank" value="#2" icon={Award} trend="up" trendVal="↑1 from last week" />
        <StatCard label="Total Readers" value="112,400" icon={Users} trend="up" trendVal="+8.2% this week" />
      </div>
    </div>
  );
}

export default function MangakaPages({ page, authorEmail, onNavigate }: { page: string; authorEmail: string; onNavigate?: (page: string) => void }) {
  if (page === "dashboard") return <MangakaDashboard />;
  if (page === "series") return <div className="p-6 bg-[#f3f3f3] min-h-full">My series</div>;
  if (page === "proposals") return <ProposalList authorEmail={authorEmail} onNavigate={onNavigate} />;
  if (page === "create-proposal") return <DraftForm authorEmail={authorEmail} onDone={() => onNavigate?.("proposals")} />;
  if (page === "tasks") return <ProductionDashboard authorEmail={authorEmail} />;
  if (page === "production") return <ProductionDashboard authorEmail={authorEmail} />;
  if (page === "rankings") return <div className="p-6">Rankings</div>;
  return <div className="p-6">Page under construction</div>;
}



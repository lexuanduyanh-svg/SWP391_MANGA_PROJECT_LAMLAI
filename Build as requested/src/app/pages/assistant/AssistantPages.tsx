import { useEffect, useState } from "react";
import { CheckSquare, Activity, AlertTriangle, DollarSign, Clock, Download } from "lucide-react";
import { earningsData } from "../../shared";
import { StatCard } from "../ui-helpers";
import { ResponsiveContainer, BarChart, Bar, CartesianGrid, XAxis, YAxis, Tooltip } from "recharts";
import AssistantTaskBoard from "./AssistantTaskBoard";

function AssistantDashboard() { return <div className="p-6 space-y-6"><h1 className="text-xl font-bold text-gray-900">Good morning, Yamada Kenji</h1></div>; }
function AssistantEarnings() { return <div className="p-6 space-y-5"><div className="flex items-center justify-between"><h1 className="text-xl font-bold text-gray-900">Earnings Dashboard</h1><button className="flex items-center gap-2 px-4 py-2 border text-gray-700 text-sm rounded-lg"><Download size={14} /> Export PDF</button></div><div className="grid grid-cols-3 gap-4"><StatCard label="This Month" value="¥198,000" icon={DollarSign} /></div></div>; }

export default function AssistantPages({ page, assistantEmail }: { page: string; assistantEmail: string }) {
  if (page === "dashboard") return <AssistantDashboard />;
  if (page === "tasks") return <AssistantTaskBoard assistantEmail={assistantEmail} />;
  if (page === "earnings") return <AssistantEarnings />;
  if (page === "references") return <div className="p-6">Reference Materials</div>;
  if (page === "submissions") return <div className="p-6">Submit Work</div>;
  return <div className="p-6">Assistant: {page}</div>;
}

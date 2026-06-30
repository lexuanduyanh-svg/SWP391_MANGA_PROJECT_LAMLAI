import { Role } from "../shared";

const items = [
  { msg: "Chapter 49 submitted for review", time: "10 min ago", type: "info", read: false },
  { msg: "Proposal 'Ryuu no Michi' approved — awaiting board vote", time: "1h ago", type: "success", read: false },
  { msg: "Revision requested: Kage no Ken Ch.48 p.9", time: "3h ago", type: "warning", read: false },
  { msg: "New assistant assignment: Yamada Kenji", time: "5h ago", type: "info", read: true },
  { msg: "Editorial board meeting scheduled: Thursday 14:00", time: "1d ago", type: "info", read: true },
  { msg: "Hoshi no Mori ranking dropped to #5", time: "2d ago", type: "warning", read: true },
  { msg: "Payment ¥48,000 processed for Ch.47 inking tasks", time: "3d ago", type: "success", read: true },
];

export default function Notifications({ role }: { role: Role }) {
  return <div className="p-6 space-y-5"><div className="flex items-center justify-between"><h1 className="text-xl font-bold text-gray-900">Notification Center</h1><button className="text-sm text-purple-600 hover:text-purple-700 font-medium">Mark all as read</button></div><div className="bg-white rounded-xl border border-purple-50 shadow-sm divide-y divide-gray-50">{items.map((n, i) => <div key={i} className={`flex items-start gap-4 px-5 py-4 hover:bg-gray-50 transition-colors ${!n.read ? "bg-purple-50/30" : ""}`}><div className={`w-2.5 h-2.5 rounded-full mt-1.5 flex-shrink-0 ${n.type === "success" ? "bg-emerald-400" : n.type === "warning" ? "bg-amber-400" : "bg-blue-400"}`} /><div className="flex-1"><p className={`text-sm ${!n.read ? "font-medium text-gray-800" : "text-gray-600"}`}>{n.msg}</p><p className="text-xs text-gray-400 mt-0.5">{n.time}</p></div>{!n.read && <div className="w-1.5 h-1.5 rounded-full bg-purple-500 mt-2 flex-shrink-0" />}</div>)}</div></div>;
}

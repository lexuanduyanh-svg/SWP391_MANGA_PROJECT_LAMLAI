import { useEffect, useState } from "react";
import { Plus } from "lucide-react";
import { Badge } from "../ui-helpers";
import { api } from "../../api";

export default function ProductionDashboard({ authorEmail }: { authorEmail: string }) {
  const [proposals, setProposals] = useState<any[]>([]);
  const [selected, setSelected] = useState<string>("");
  const [chapters, setChapters] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [chTitle, setChTitle] = useState("");
  const [chNum, setChNum] = useState("");
  const [msg, setMsg] = useState("");

  useEffect(() => { api.listMangakaProposals(authorEmail).then((ps: any) => { setProposals(ps); if (ps.length && !selected) setSelected(ps[0].id); }).catch(() => {}); }, [authorEmail]);
  useEffect(() => { if (selected) api.listChapters(selected, authorEmail).then(setChapters).catch(() => setChapters([])); }, [selected, authorEmail]);

  const createChapter = async () => { try { await api.createChapter(selected, authorEmail, { title: chTitle, chapterNumber: parseInt(chNum) || 1 }); setMsg("Chapter created!"); setShowForm(false); setChTitle(""); setChNum(""); api.listChapters(selected, authorEmail).then(setChapters).catch(() => {}); } catch (e: any) { setMsg("Error: " + e.message); } };

  return (
    <div className="p-6 space-y-5">
      <div className="flex items-center justify-between"><h1 className="text-xl font-bold text-gray-900">Production Dashboard</h1><button onClick={() => setShowForm(!showForm)} className="w-10 h-10 rounded-xl bg-[#a3a3a3] text-white flex items-center justify-center"><Plus size={18} /></button></div>
      {msg && <div className="text-sm text-purple-600 bg-purple-50 px-4 py-2 rounded-lg">{msg}</div>}

      <div><label className="text-xs font-medium text-gray-500">Select Proposal</label><select className="w-full h-9 px-3 bg-gray-50 border rounded-lg text-sm mt-1" value={selected} onChange={e => setSelected(e.target.value)}>{proposals.map(p => <option key={p.id} value={p.id}>{p.title} — {p.status}</option>)}</select></div>

      {showForm && (
        <div className="bg-white rounded-2xl border border-purple-100 shadow-sm p-6 space-y-4">
          <h3 className="font-semibold text-gray-800">Create Chapter</h3>
          <input className="w-full h-9 px-3 bg-gray-50 border rounded-lg text-sm" placeholder="Chapter Title" value={chTitle} onChange={e => setChTitle(e.target.value)} />
          <input className="w-full h-9 px-3 bg-gray-50 border rounded-lg text-sm" placeholder="Chapter Number" type="number" value={chNum} onChange={e => setChNum(e.target.value)} />
          <button className="px-4 py-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white text-sm font-medium rounded-lg" onClick={createChapter}>Create</button>
        </div>
      )}

      <div className="bg-white rounded-xl border border-purple-50 shadow-sm overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-100"><tr><th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">Chapter</th><th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">Title</th><th className="text-left px-4 py-3 text-xs font-semibold text-gray-500">Status</th></tr></thead>
          <tbody className="divide-y divide-gray-50">{chapters.map(ch => (<tr key={ch.id} className="hover:bg-purple-50/30"><td className="px-4 py-3 text-sm font-mono">Ch.{ch.chapterNumber}</td><td className="px-4 py-3 text-sm font-medium">{ch.title}</td><td className="px-4 py-3"><Badge status={ch.status || "Draft"} /></td></tr>))}</tbody>
        </table>
      </div>
    </div>
  );
}

import { useEffect, useState } from "react";
import { api } from "../../api";
import { Badge } from "../ui-helpers";

export default function ReviewProposal({ editorEmail }: { editorEmail: string }) {
  const [proposals, setProposals] = useState<any[]>([]);
  const [selected, setSelected] = useState(0);
  const [note, setNote] = useState("");
  const [msg, setMsg] = useState("");

  const load = () => api.listEditorProposals(editorEmail).then(setProposals).catch(() => setProposals([]));
  useEffect(() => { load(); }, [editorEmail]);

  const p = proposals[selected];
  if (!p) return <div className="p-6"><h2 className="text-xl font-bold">Proposal Review Queue</h2><p className="text-sm text-gray-500 mt-2">No proposals to review.</p></div>;

  const act = async (fn: () => Promise<any>) => { try { await fn(); setMsg("Done!"); setNote(""); load(); } catch (e: any) { setMsg("Error: " + e.message); } };

  return (
    <div className="p-6 space-y-5">
      <h1 className="text-xl font-bold text-gray-900">Proposal Review Queue</h1>
      {msg && <div className="text-sm text-purple-600 bg-purple-50 px-4 py-2 rounded-lg">{msg}</div>}
      <div className="grid grid-cols-3 gap-5">
        <div className="space-y-2">
          {proposals.map((pr, i) => (
            <button key={pr.id} onClick={() => setSelected(i)} className={`w-full text-left p-3.5 rounded-xl border transition-all ${selected === i ? "border-purple-300 bg-purple-50" : "border-gray-100 bg-white hover:border-purple-200"}`}>
              <div className="text-sm font-semibold text-gray-800">{pr.title}</div>
              <div className="text-xs text-gray-500 mt-0.5">{pr.genre} · {pr.targetAudience}</div>
              <Badge status={pr.status} />
            </button>
          ))}
        </div>
        <div className="col-span-2 bg-white rounded-xl border border-purple-50 shadow-sm p-6 space-y-4">
          <div><h2 className="text-lg font-bold text-gray-900">{p.title}</h2><p className="text-sm text-gray-500">{p.genre} · Target: {p.targetAudience}</p></div>
          <div className="text-sm text-gray-700 leading-relaxed bg-gray-50 rounded-lg p-3">{p.synopsis || "No synopsis"}</div>
          <div><label className="block text-xs font-semibold text-gray-500 mb-2">Editor Notes</label><textarea className="w-full h-20 px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm resize-none" value={note} onChange={e => setNote(e.target.value)} placeholder="Add editorial notes..." /></div>
          <div className="flex gap-3">
            <button className="flex-1 py-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white text-sm font-semibold rounded-lg" onClick={() => act(() => api.forwardToBoard(p.id, editorEmail, note))}>Approve — Forward to Board</button>
            <button className="px-4 py-2 border border-amber-200 bg-amber-50 text-amber-700 text-sm font-medium rounded-lg" onClick={() => act(() => api.requestRevision(p.id, editorEmail, note))}>Return for Revision</button>
            <button className="px-4 py-2 border border-red-200 bg-red-50 text-red-700 text-sm font-medium rounded-lg" onClick={() => act(() => api.rejectProposal(p.id, editorEmail, note))}>Reject</button>
          </div>
        </div>
      </div>
    </div>
  );
}

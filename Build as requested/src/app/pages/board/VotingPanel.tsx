import { useEffect, useState } from "react";
import { api } from "../../api";
import { Badge } from "../ui-helpers";

export default function VotingPanel({ memberEmail }: { memberEmail: string }) {
  const [proposals, setProposals] = useState<any[]>([]);
  const [votes, setVotes] = useState<Record<string, string>>({});
  const [msg, setMsg] = useState("");

  const load = () => api.listBoardProposals(memberEmail).then(setProposals).catch(() => setProposals([]));
  useEffect(() => { load(); }, [memberEmail]);

  const vote = async (id: string, action: "approve" | "reject") => {
    try {
      if (action === "approve") await api.boardApprove(id, memberEmail, "");
      else await api.boardReject(id, memberEmail, "");
      setVotes(v => ({ ...v, [id]: action }));
      setMsg(`Voted ${action}!`);
      load();
    } catch (e: any) { setMsg("Error: " + e.message); }
  };

  if (!proposals.length) return <div className="p-6"><h2 className="text-xl font-bold">Proposal Voting</h2><p className="text-sm text-gray-500 mt-2">No proposals pending vote.</p></div>;

  return (
    <div className="p-6 space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-900">Proposal Voting</h1>
      </div>
      {msg && <div className="text-sm text-purple-600 bg-purple-50 px-4 py-2 rounded-lg">{msg}</div>}
      <div className="space-y-4">
        {proposals.map(p => (
          <div key={p.id} className="bg-white rounded-xl border border-purple-50 shadow-sm p-5">
            <div className="grid grid-cols-3 gap-5">
              <div className="col-span-2">
                <div className="flex items-start justify-between mb-2">
                  <div><h3 className="font-semibold text-gray-900">{p.title}</h3><p className="text-xs text-gray-500">{p.genre} · Target: {p.targetAudience}</p></div>
                  {votes[p.id] && <Badge status={votes[p.id] === "approve" ? "Approved" : "Cancelled"} />}
                </div>
                <p className="text-sm text-gray-600 leading-relaxed">{p.synopsis || "No synopsis"}</p>
                <div className="flex gap-2 mt-3 text-xs text-gray-500"><span>Approve: {p.boardApproveVotes ?? 0}</span><span>Reject: {p.boardRejectVotes ?? 0}</span><span>Pending: {p.boardPendingVotes ?? 0}</span></div>
              </div>
              <div className="flex flex-col gap-2 justify-center">
                <button onClick={() => vote(p.id, "approve")} className={`py-2 rounded-lg text-sm font-semibold transition-all ${votes[p.id] === "approve" ? "bg-emerald-500 text-white" : "bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100"}`}>Approve</button>
                <button onClick={() => vote(p.id, "reject")} className={`py-2 rounded-lg text-sm font-semibold transition-all ${votes[p.id] === "reject" ? "bg-red-500 text-white" : "bg-red-50 text-red-700 border border-red-200 hover:bg-red-100"}`}>Reject</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

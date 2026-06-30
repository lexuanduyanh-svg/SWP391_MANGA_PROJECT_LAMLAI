import { useEffect, useState } from "react";
import { api } from "../../api";
import { Badge } from "../ui-helpers";

export default function ProposalList({ authorEmail, onNavigate }: { authorEmail: string; onNavigate?: (page: string) => void }) {
  const [items, setItems] = useState<any[]>([]);
  useEffect(() => { api.listMangakaProposals(authorEmail).then(setItems).catch(() => setItems([])); }, [authorEmail]);

  return (
    <div className="p-6 bg-[#f3f3f3] min-h-full space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">My series</h1>
        <button
          onClick={() => onNavigate?.("create-proposal")}
          className="h-10 px-4 rounded-xl bg-[#d4d4d4] hover:bg-white border border-transparent hover:border-purple-200 text-sm font-semibold text-gray-700 shadow-sm transition-colors"
        >
          Create Draft
        </button>
      </div>
      <div className="grid grid-cols-3 gap-5">
        {items.map((i) => (
          <div key={i.id} className="rounded-2xl overflow-hidden shadow-sm bg-[#a3a3a3] min-h-56 flex flex-col">
            <div className="flex-1 p-5 flex items-end">
              <div className="w-full h-28 rounded-2xl bg-[#d4d4d4] flex items-center justify-center text-gray-600 font-medium">{i.title}</div>
            </div>
            <div className="bg-[#d4d4d4] px-4 py-3.5 space-y-1">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-bold text-gray-800">{i.title}</h4>
                <Badge status={i.status} />
              </div>
              <p className="text-xs text-gray-500">{i.genre} · {i.targetAudience}</p>
              <div className="flex items-center justify-between pt-1">
                <span className="text-xs text-gray-500">{i.manuscriptFileName || "No file"}</span>
                <div className="flex gap-3 text-xs"><button className="text-purple-600 font-medium">Edit</button><button className="text-gray-500 font-medium">View</button></div>
              </div>
            </div>
          </div>
        ))}
      </div>
      {items.length === 0 && <div className="text-sm text-gray-500">No proposals yet. Create one from proposal page.</div>}
    </div>
  );
}


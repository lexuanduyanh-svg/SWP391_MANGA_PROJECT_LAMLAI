import { useState } from "react";
import { Upload, FileText } from "lucide-react";
import { Badge } from "../ui-helpers";
import { api } from "../../api";

export default function DraftForm({ authorEmail, onDone }: { authorEmail: string; onDone?: () => void }) {
  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [targetAudience, setTargetAudience] = useState("Shounen");
  const [synopsis, setSynopsis] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<any>(null);
  const [msg, setMsg] = useState("");
  const [busy, setBusy] = useState(false);

  const onFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0] ?? null;
    setFile(f);
    if (!f) return;
    try { setPreview(await api.previewUpload(f)); } catch { setPreview(null); }
  };

  const submit = async () => {
    if (!file) return;
    setBusy(true); setMsg("");
    try {
      const created = await api.createProposal({
        authorEmail,
        title,
        genre,
        targetAudience,
        synopsis,
        manuscriptTitle: file.name,
        manuscriptSummary: preview?.summary || "",
        manuscriptFileName: file.name,
      });
      await api.uploadManuscript(file, created.id, authorEmail);
      await api.submitProposal(created.id, authorEmail);
      setMsg("Proposal submitted.");
      setTitle(""); setGenre(""); setSynopsis(""); setFile(null); setPreview(null);
      onDone?.();
    } catch (e: any) {
      setMsg(`Error: ${e.message}`);
    } finally { setBusy(false); }
  };

  return (
    <div className="p-6 bg-[#f3f3f3] min-h-full space-y-5">
      <h1 className="text-2xl font-semibold text-gray-900">Proposal Dashboard</h1>
      {msg && <div className="text-sm text-purple-700 bg-purple-50 border border-purple-100 rounded-xl px-4 py-2">{msg}</div>}
      <div className="bg-[#a3a3a3] rounded-2xl p-5 shadow-sm space-y-4">
        <div className="inline-flex bg-[#d4d4d4] rounded-xl px-4 py-2 text-sm font-medium text-gray-800">Create Draft</div>
        <div className="grid grid-cols-2 gap-3">
          <input className="h-10 px-4 rounded-xl bg-[#d4d4d4] border-0 placeholder:text-gray-500" placeholder="Series title" value={title} onChange={e => setTitle(e.target.value)} />
          <input className="h-10 px-4 rounded-xl bg-[#d4d4d4] border-0 placeholder:text-gray-500" placeholder="Genre" value={genre} onChange={e => setGenre(e.target.value)} />
          <select className="h-10 px-4 rounded-xl bg-[#d4d4d4] border-0 text-sm" value={targetAudience} onChange={e => setTargetAudience(e.target.value)}>
            <option>Shounen</option><option>Shoujo</option><option>Seinen</option><option>Josei</option>
          </select>
          <div className="h-10 px-4 rounded-xl bg-[#d4d4d4] flex items-center text-sm text-gray-500">Draft status: <Badge status="Draft" /></div>
          <textarea className="col-span-2 h-24 px-4 py-3 rounded-xl bg-[#d4d4d4] border-0 placeholder:text-gray-500" placeholder="Synopsis" value={synopsis} onChange={e => setSynopsis(e.target.value)} />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-5">
        <div className="bg-[#a3a3a3] rounded-2xl p-5 shadow-sm space-y-4">
          <div className="inline-flex bg-[#d4d4d4] rounded-xl px-4 py-2 text-sm font-medium text-gray-800">Upload Manuscript</div>
          <label className="h-36 flex flex-col items-center justify-center border-0 rounded-2xl bg-[#d4d4d4] text-gray-600 cursor-pointer">
            <Upload size={24} />
            <span className="text-sm mt-2">Choose PDF / TXT</span>
            <input type="file" className="hidden" accept=".pdf,.txt" onChange={onFileChange} />
          </label>
        </div>

        <div className="bg-[#a3a3a3] rounded-2xl p-5 shadow-sm space-y-4">
          <div className="inline-flex bg-[#d4d4d4] rounded-xl px-4 py-2 text-sm font-medium text-gray-800">Preview</div>
          <div className="h-36 rounded-2xl bg-[#d4d4d4] p-4 flex flex-col justify-between">
            <div className="flex items-center gap-2"><FileText size={14} /><span className="text-sm font-medium">{file?.name || "No file selected"}</span></div>
            <div className="text-xs text-gray-600">{preview?.summary || "Upload a manuscript to preview summary."}</div>
          </div>
          <button disabled={!file || busy} onClick={submit} className="w-full h-11 rounded-xl bg-[#d4d4d4] hover:bg-white disabled:opacity-60 text-gray-800 font-medium transition-colors">{busy ? "Submitting..." : "Submit Proposal"}</button>
        </div>
      </div>
    </div>
  );
}

import { useEffect, useState } from "react";
import { Clock } from "lucide-react";
import { Badge } from "../ui-helpers";
import { api } from "../../api";

export default function AssistantTaskBoard({ assistantEmail }: { assistantEmail: string }) {
  const [tasks, setTasks] = useState<any[]>([]);
  const [msg, setMsg] = useState("");

  const load = () => api.listAssistantTasks(assistantEmail).then(setTasks).catch(() => setTasks([]));
  useEffect(() => { load(); }, [assistantEmail]);

  const start = async (id: string) => { try { await api.startAssistantTask(id, assistantEmail); setMsg("Task started!"); load(); } catch (e: any) { setMsg("Error: " + e.message); } };
  const submit = async (id: string) => { try { await api.submitAssistantTask(id, assistantEmail, "submitted-file.pdf", "Done"); setMsg("Task submitted!"); load(); } catch (e: any) { setMsg("Error: " + e.message); } };

  const columns = [
    { title: "New", color: "bg-purple-50 border-purple-200", dot: "bg-purple-400", filter: (t: any) => t.status === "NEW" || t.status === "PENDING" },
    { title: "In Progress", color: "bg-blue-50 border-blue-200", dot: "bg-blue-400", filter: (t: any) => t.status === "IN_PROGRESS" || t.status === "STARTED" },
    { title: "Revision Requested", color: "bg-red-50 border-red-200", dot: "bg-red-400", filter: (t: any) => t.status === "REVISION_REQUESTED" },
    { title: "Completed", color: "bg-emerald-50 border-emerald-200", dot: "bg-emerald-400", filter: (t: any) => t.status === "COMPLETED" || t.status === "APPROVED" },
  ];

  return (
    <div className="p-6 space-y-5">
      <h1 className="text-xl font-bold text-gray-900">Assigned Tasks</h1>
      {msg && <div className="text-sm text-purple-600 bg-purple-50 px-4 py-2 rounded-lg">{msg}</div>}
      <div className="grid grid-cols-4 gap-4 min-h-[500px]">
        {columns.map(col => {
          const colTasks = tasks.filter(col.filter);
          return (
            <div key={col.title} className={`rounded-xl border ${col.color} p-3`}>
              <div className="flex items-center gap-2 mb-3"><div className={`w-2 h-2 rounded-full ${col.dot}`} /><span className="text-xs font-semibold text-gray-600 uppercase tracking-wide">{col.title}</span><span className="ml-auto bg-white text-xs font-bold text-gray-500 w-5 h-5 rounded-full flex items-center justify-center border">{colTasks.length}</span></div>
              <div className="space-y-2">{colTasks.map((t) => (
                <div key={t.id} className="bg-white rounded-lg p-3 shadow-sm border hover:shadow-md transition-shadow">
                  <p className="text-sm font-semibold text-gray-800">{t.taskType || t.title || "Task"}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{t.instructions || ""}</p>
                  <Badge status={t.status} />
                  <div className="flex gap-2 mt-2">
                    {(t.status === "NEW" || t.status === "PENDING") && <button onClick={() => start(t.id)} className="text-xs text-blue-600 font-medium">Start</button>}
                    {(t.status === "IN_PROGRESS" || t.status === "STARTED") && <button onClick={() => submit(t.id)} className="text-xs text-emerald-600 font-medium">Submit</button>}
                  </div>
                </div>
              ))}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

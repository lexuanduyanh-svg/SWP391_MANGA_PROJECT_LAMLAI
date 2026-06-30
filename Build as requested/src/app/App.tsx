import { useState } from "react";
import Sidebar from "./layout/Sidebar";
import TopNav from "./layout/TopNav";
import LoginPage from "./pages/LoginPage";
import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";
import MangakaPages from "./pages/mangaka/MangakaPages";
import AssistantPages from "./pages/assistant/AssistantPages";
import EditorPages from "./pages/editor/EditorPages";
import BoardPages from "./pages/board/BoardPages";
import { Page, Role } from "./shared";

function PageContent({ role, page, email, onNavigate }: { role: Role; page: string; email: string; onNavigate: (page: string) => void }) {
  if (page === "notifications") return <Notifications role={role} />;
  if (page === "profile") return <Profile role={role} />;
  if (role === "mangaka") return <MangakaPages page={page} authorEmail={email} onNavigate={onNavigate} />;
  if (role === "assistant") return <AssistantPages page={page} assistantEmail={email} />;
  if (role === "editor") return <EditorPages page={page} editorEmail={email} />;
  if (role === "board") return <BoardPages page={page} memberEmail={email} />;
  return null;
}

export default function App() {
  const [role, setRole] = useState<Role | null>(null);
  const [email, setEmail] = useState("mangaka@manga.local");
  const [page, setPage] = useState<Page>("dashboard");
  if (!role) return <LoginPage onLogin={(r, e) => { setRole(r); setEmail(e); setPage("dashboard"); }} />;
  return (
    <div className="flex h-screen overflow-hidden bg-[#f3f3f3]" style={{ fontFamily: "'Inter', 'Noto Sans JP', sans-serif" }}>
      <Sidebar role={role} currentPage={page} onNavigate={setPage} />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <TopNav role={role} page={page} onLogout={() => { setRole(null); setPage("dashboard"); }} />
        <main className="flex-1 overflow-y-auto scrollbar-thin scrollbar-track-transparent scrollbar-thumb-purple-100">
          <PageContent role={role} page={page} email={email} onNavigate={setPage} />
        </main>
      </div>
    </div>
  );
}

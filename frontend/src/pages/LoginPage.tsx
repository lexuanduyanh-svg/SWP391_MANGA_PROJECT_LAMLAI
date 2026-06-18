import { useEffect, useState } from "react";
import { AdminAccountDashboard } from "../components/AdminAccountDashboard";
import { EditorialBoardDashboard } from "../components/EditorialBoardDashboard";
import { AssistantDashboard } from "../components/AssistantDashboard";
import { MangakaDashboard } from "../components/MangakaDashboard";
import { TantouEditorDashboard } from "../components/TantouEditorDashboard";
import { LoginForm } from "../components/LoginForm";
import { getStoredAuth, logout } from "../services/authService";
import type { LoginResponse } from "../types/auth";

export function LoginPage() {
  const [session, setSession] = useState<LoginResponse | null>(null);

  useEffect(() => {
    setSession(getStoredAuth());
  }, []);

  useEffect(() => {
    if (!session) {
      document.title = "Manga Workflow";
      return;
    }

    const roleTitle = session.user.role.replace(/([a-z])([A-Z])/g, "$1 $2");
    document.title = `${roleTitle} Workspace`;
  }, [session]);

  const isAdmin = session?.user.role === "Admin";
  const isAssistant = session?.user.role === "Assistant";
  const isMangaka = session?.user.role === "Mangaka";
  const isTantouEditor = session?.user.role === "TantouEditor";
  const isEditorialBoardMember = session?.user.role === "EditorialBoardMember";

  function handleLogout() {
    logout();
    setSession(null);
  }

  if (session && isAdmin) {
    return (
      <main className="board-page" id="dashboard">
        <AdminAccountDashboard session={session} onLogout={handleLogout} />
      </main>
    );
  }

  if (session && isAssistant) {
    return <AssistantDashboard session={session} onLogout={handleLogout} />;
  }

  if (session && isMangaka) {
    return <MangakaDashboard session={session} onLogout={handleLogout} />;
  }

  if (session && isTantouEditor) {
    return <TantouEditorDashboard session={session} onLogout={handleLogout} />;
  }

  if (session && isEditorialBoardMember) {
    return (
      <EditorialBoardDashboard session={session} onLogout={handleLogout} />
    );
  }

  if (session) {
    return (
      <main
        className="board-page board-page--signed-in"
        id="signed-in-placeholder"
      >
        <section className="signed-in-placeholder glass-card">
          <span className="eyebrow">Signed in</span>
          <h1>Welcome, {session.user.fullName}</h1>
          <p>
            You are signed in as <strong>{session.user.role}</strong>. This
            secure workspace keeps your session active and routes you to the
            best available project tools for your role.
          </p>

          <div className="signed-in-placeholder__meta">
            <div>
              <span>Email</span>
              <strong>{session.user.email}</strong>
            </div>
            <div>
              <span>Role</span>
              <strong>{session.user.role}</strong>
            </div>
          </div>

          <button
            className="button button-secondary"
            type="button"
            onClick={handleLogout}
          >
            Log out
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="login-page">
      <div className="login-bg login-bg--one" aria-hidden="true" />
      <div className="login-bg login-bg--two" aria-hidden="true" />
      <div className="login-bg login-bg--three" aria-hidden="true" />
      <div
        className="login-pattern login-pattern--halftone"
        aria-hidden="true"
      />
      <div className="login-pattern login-pattern--grid" aria-hidden="true" />

      <section className="login-stage" aria-label="Authentication screen">
        <div className="login-brand">
          <span className="login-brand__mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" role="img" aria-hidden="true">
              <defs>
                <linearGradient
                  id="loginMarkGradient"
                  x1="0%"
                  y1="0%"
                  x2="100%"
                  y2="100%"
                >
                  <stop offset="0%" stopColor="#7c3aed" />
                  <stop offset="100%" stopColor="#38bdf8" />
                </linearGradient>
              </defs>
              <path
                d="M6 7.25C6 6.01 7.01 5 8.25 5h7.5C16.99 5 18 6.01 18 7.25v9.5A2.25 2.25 0 0 1 15.75 19h-7.5A2.25 2.25 0 0 1 6 16.75v-9.5Zm3 .5v8.5h6v-8.5H9Zm1.25 11.75h3.5"
                fill="none"
                stroke="url(#loginMarkGradient)"
                strokeWidth="1.8"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
              <path
                d="M10.5 9.5h3M10.5 12h5.5"
                fill="none"
                stroke="url(#loginMarkGradient)"
                strokeWidth="1.8"
                strokeLinecap="round"
              />
            </svg>
          </span>
          <div>
            <p className="login-brand__kicker">SWP391 Manga Workflow</p>
            <strong>Secure access for your project team</strong>
          </div>
        </div>

        <aside
          className="auth-panel glass-card"
          aria-label="Authentication panel"
        >
          <LoginForm onLoginSuccess={setSession} />
        </aside>
      </section>
    </main>
  );
}

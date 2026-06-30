const BASE = "/api";

async function request(path: string, init?: RequestInit) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
    ...init,
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export const api = {
  // ── Auth ────────────────────────────────────────────────────────────────────
  login: (email: string, password: string) =>
    request(`/auth/login`, { method: "POST", body: JSON.stringify({ email, password }) }),

  // ── Mangaka Proposals (Flow 1) ─────────────────────────────────────────────
  listMangakaProposals: (authorEmail: string) =>
    request(`/mangaka/proposals?authorEmail=${encodeURIComponent(authorEmail)}`),
  createProposal: (body: any) =>
    request(`/mangaka/proposals`, { method: "POST", body: JSON.stringify(body) }),
  updateProposal: (id: string, authorEmail: string, body: any) =>
    request(`/mangaka/proposals/${id}?authorEmail=${encodeURIComponent(authorEmail)}`, { method: "PUT", body: JSON.stringify(body) }),
  submitProposal: (id: string, authorEmail: string) =>
    request(`/mangaka/proposals/${id}/submit`, { method: "PUT", body: JSON.stringify({ authorEmail }) }),
  previewUpload: async (file: File) => {
    const fd = new FormData(); fd.append("file", file);
    const res = await fetch(`${BASE}/mangaka/proposals/preview-upload`, { method: "POST", body: fd });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },
  uploadManuscript: async (file: File, proposalId?: string, authorEmail?: string) => {
    const fd = new FormData(); fd.append("file", file);
    if (proposalId) fd.append("proposalId", proposalId);
    if (authorEmail) fd.append("authorEmail", authorEmail);
    const res = await fetch(`${BASE}/mangaka/proposals/upload`, { method: "POST", body: fd });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  // ── Editor Review (Flow 1) ─────────────────────────────────────────────────
  listEditorProposals: (editorEmail: string) =>
    request(`/editor/proposals?editorEmail=${encodeURIComponent(editorEmail)}`),
  forwardToBoard: (id: string, editorEmail: string, note: string) =>
    request(`/editor/proposals/${id}/forward-board`, { method: "PUT", body: JSON.stringify({ editorEmail, note }) }),
  requestRevision: (id: string, editorEmail: string, note: string) =>
    request(`/editor/proposals/${id}/request-revision`, { method: "PUT", body: JSON.stringify({ editorEmail, note }) }),
  rejectProposal: (id: string, editorEmail: string, note: string) =>
    request(`/editor/proposals/${id}/reject`, { method: "PUT", body: JSON.stringify({ editorEmail, note }) }),

  // ── Board Voting (Flow 1) ──────────────────────────────────────────────────
  listBoardProposals: (memberEmail: string) =>
    request(`/board/proposals?memberEmail=${encodeURIComponent(memberEmail)}`),
  boardApprove: (id: string, memberEmail: string, note: string) =>
    request(`/board/proposals/${id}/approve`, { method: "PUT", body: JSON.stringify({ memberEmail, note }) }),
  boardReject: (id: string, memberEmail: string, note: string) =>
    request(`/board/proposals/${id}/reject`, { method: "PUT", body: JSON.stringify({ memberEmail, note }) }),

  // ── Mangaka Production (Flow 2) ────────────────────────────────────────────
  listChapters: (proposalId: string, authorEmail: string) =>
    request(`/mangaka/proposals/${proposalId}/chapters?authorEmail=${encodeURIComponent(authorEmail)}`),
  createChapter: (proposalId: string, authorEmail: string, body: any) =>
    request(`/mangaka/proposals/${proposalId}/chapters?authorEmail=${encodeURIComponent(authorEmail)}`, { method: "POST", body: JSON.stringify(body) }),
  createPage: (proposalId: string, chapterId: string, authorEmail: string, body: any) =>
    request(`/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages?authorEmail=${encodeURIComponent(authorEmail)}`, { method: "POST", body: JSON.stringify(body) }),
  createRegion: (proposalId: string, chapterId: string, pageId: string, authorEmail: string, body: any) =>
    request(`/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions?authorEmail=${encodeURIComponent(authorEmail)}`, { method: "POST", body: JSON.stringify(body) }),
  createTask: (proposalId: string, chapterId: string, pageId: string, regionId: string, authorEmail: string, body: any) =>
    request(`/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}/tasks?authorEmail=${encodeURIComponent(authorEmail)}`, { method: "POST", body: JSON.stringify(body) }),
  approveTask: (proposalId: string, chapterId: string, pageId: string, regionId: string, taskId: string, authorEmail: string) =>
    request(`/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}/tasks/${taskId}/approve?authorEmail=${encodeURIComponent(authorEmail)}`, { method: "PUT" }),
  redoTask: (proposalId: string, chapterId: string, pageId: string, regionId: string, taskId: string, authorEmail: string) =>
    request(`/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}/tasks/${taskId}/redo?authorEmail=${encodeURIComponent(authorEmail)}`, { method: "PUT" }),

  // ── Assistant Tasks (Flow 2) ───────────────────────────────────────────────
  listAssistantTasks: (assistantEmail: string) =>
    request(`/assistant/tasks?assistantEmail=${encodeURIComponent(assistantEmail)}`),
  startAssistantTask: (taskId: string, assistantEmail: string) =>
    request(`/assistant/tasks/${taskId}/start`, { method: "PUT", body: JSON.stringify({ assistantEmail }) }),
  submitAssistantTask: (taskId: string, assistantEmail: string, submittedFileName: string, submissionNote: string) =>
    request(`/assistant/tasks/${taskId}/submit`, { method: "PUT", body: JSON.stringify({ assistantEmail, submittedFileName, submissionNote }) }),
};

import type { MangaProposal } from "../types/mangaka";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function parseError(response: Response): Promise<string> {
  const payload = await response.json().catch(() => null);

  if (typeof payload === "string") {
    return payload;
  }

  return (
    payload?.message ??
    payload?.error ??
    "Không thể thực hiện thao tác. Vui lòng thử lại sau."
  );
}

async function readJson<T>(response: Response): Promise<T | null> {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    return null;
  }
}

async function requestProposalAction(
  proposalId: string,
  action: "forward-board" | "request-revision" | "reject",
  payload: { editorEmail: string; note: string },
): Promise<MangaProposal | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/editor/proposals/${proposalId}/${action}`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return readJson<MangaProposal>(response);
}

export async function listTantouEditorProposals(
  editorEmail: string,
): Promise<MangaProposal[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/editor/proposals?editorEmail=${encodeURIComponent(editorEmail)}`,
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await readJson<MangaProposal[]>(response)) ?? [];
}

export async function forwardProposalToBoard(
  proposalId: string,
  payload: { editorEmail: string; note: string },
): Promise<MangaProposal | null> {
  return requestProposalAction(proposalId, "forward-board", payload);
}

export async function requestProposalRevision(
  proposalId: string,
  payload: { editorEmail: string; note: string },
): Promise<MangaProposal | null> {
  return requestProposalAction(proposalId, "request-revision", payload);
}

export async function rejectProposalByEditor(
  proposalId: string,
  payload: { editorEmail: string; note: string },
): Promise<MangaProposal | null> {
  return requestProposalAction(proposalId, "reject", payload);
}

export function getManuscriptDownloadUrl(fileName: string): string {
  return `${API_BASE_URL}/api/mangaka/proposals/files/${encodeURIComponent(fileName)}`;
}

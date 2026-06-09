import type {
  MangaProposal,
  MangaProposalCreateRequest,
  MangaProposalSubmitRequest,
  MangaProposalUpdateRequest,
} from "../types/mangaka";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function parseError(response: Response): Promise<string> {
  const error = await response.json().catch(() => null);
  return (
    error?.message ??
    error?.error ??
    "Không thể thực hiện thao tác. Vui lòng thử lại sau."
  );
}

export async function listMangakaProposals(
  authorEmail: string,
): Promise<MangaProposal[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals?authorEmail=${encodeURIComponent(authorEmail)}`,
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProposal[];
}

function toBackendProposalPayload(
  payload: MangaProposalCreateRequest | MangaProposalUpdateRequest,
) {
  return {
    ...payload,
    targetAudience: payload.targetAudience?.trim() || "General",
    manuscriptTitle: payload.title,
    manuscriptSummary: payload.synopsis,
  };
}

export async function createMangakaProposal(
  payload: MangaProposalCreateRequest,
): Promise<MangaProposal> {
  const response = await fetch(`${API_BASE_URL}/api/mangaka/proposals`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(toBackendProposalPayload(payload)),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProposal;
}

export async function updateMangakaProposal(
  id: string,
  authorEmail: string,
  payload: MangaProposalUpdateRequest,
): Promise<MangaProposal> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${id}?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(toBackendProposalPayload(payload)),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProposal;
}

export async function uploadMangakaManuscript(
  file: File,
): Promise<{ fileName: string; originalFileName: string; path: string }> {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${API_BASE_URL}/api/mangaka/proposals/upload`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as {
    fileName: string;
    originalFileName: string;
    path: string;
  };
}

export async function deleteMangakaProposal(
  id: string,
  authorEmail: string,
): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${id}?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "DELETE",
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
}

export async function submitMangakaProposal(
  id: string,
  payload: MangaProposalSubmitRequest,
): Promise<MangaProposal> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${id}/submit`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProposal;
}

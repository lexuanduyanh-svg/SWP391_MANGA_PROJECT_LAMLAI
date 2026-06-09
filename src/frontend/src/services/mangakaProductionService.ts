import type {
  MangaProductionChapter,
  MangaProductionChapterCreateRequest,
  MangaProductionPage,
  MangaProductionPageCreateRequest,
  MangaProductionRegion,
  MangaProductionRegionCreateRequest,
  MangaProductionTask,
  MangaProductionTaskCreateRequest,
} from "../types/mangaka";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function parseError(response: Response): Promise<string> {
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

export async function listMangakaChapters(
  proposalId: string,
  authorEmail: string,
): Promise<MangaProductionChapter[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${proposalId}/chapters?authorEmail=${encodeURIComponent(authorEmail)}`,
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProductionChapter[];
}

export async function createMangakaChapter(
  proposalId: string,
  authorEmail: string,
  payload: MangaProductionChapterCreateRequest,
): Promise<MangaProductionChapter> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${proposalId}/chapters?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProductionChapter;
}

export async function createMangakaPage(
  proposalId: string,
  chapterId: string,
  authorEmail: string,
  payload: MangaProductionPageCreateRequest,
): Promise<MangaProductionPage> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProductionPage;
}

export async function createMangakaRegion(
  proposalId: string,
  chapterId: string,
  pageId: string,
  authorEmail: string,
  payload: MangaProductionRegionCreateRequest,
): Promise<MangaProductionRegion> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProductionRegion;
}

export async function createMangakaTask(
  proposalId: string,
  chapterId: string,
  pageId: string,
  regionId: string,
  authorEmail: string,
  payload: MangaProductionTaskCreateRequest,
): Promise<MangaProductionTask> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}/tasks?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as MangaProductionTask;
}

export async function approveMangakaTask(
  proposalId: string,
  chapterId: string,
  pageId: string,
  regionId: string,
  taskId: string,
  authorEmail: string,
): Promise<MangaProductionTask> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}/tasks/${taskId}/approve?authorEmail=${encodeURIComponent(authorEmail)}`,
    { method: "PUT" },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionTask;
}

export async function redoMangakaTask(
  proposalId: string,
  chapterId: string,
  pageId: string,
  regionId: string,
  taskId: string,
  authorEmail: string,
): Promise<MangaProductionTask> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/proposals/${proposalId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}/tasks/${taskId}/redo?authorEmail=${encodeURIComponent(authorEmail)}`,
    { method: "PUT" },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionTask;
}

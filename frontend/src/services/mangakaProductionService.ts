import type {
  MangaProductionChapter,
  MangaProductionChapterCreateRequest,
  MangaProductionPage,
  MangaProductionPageCreateRequest,
  MangaProductionTask,
  MangaProductionTaskCreateRequest,
  MangaProductionRegion,
} from "../types/mangaka";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function parseError(response: Response): Promise<string> {
  const payload = await response.json().catch(() => null);
  if (typeof payload === "string") return payload;
  return (
    payload?.message ??
    payload?.error ??
    "Không thể thực hiện thao tác. Vui lòng thử lại sau."
  );
}

export async function listMangakaChapters(
  seriesId: string,
  authorEmail: string,
): Promise<MangaProductionChapter[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters?authorEmail=${encodeURIComponent(authorEmail)}`,
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionChapter[];
}

export async function createMangakaChapter(
  seriesId: string,
  authorEmail: string,
  payload: MangaProductionChapterCreateRequest,
): Promise<MangaProductionChapter> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionChapter;
}

export async function createMangakaPage(
  seriesId: string,
  chapterId: string,
  authorEmail: string,
  payload: MangaProductionPageCreateRequest,
): Promise<MangaProductionPage> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionPage;
}

// --- Region API (restored to full scope) ---

export async function createMangakaRegion(
  seriesId: string,
  chapterId: string,
  pageId: string,
  authorEmail: string,
  payload: {
    regionType: string;
    x: number;
    y: number;
    widthPct: number;
    heightPct: number;
    note?: string;
  },
): Promise<MangaProductionRegion> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages/${pageId}/regions?authorEmail=${encodeURIComponent(authorEmail)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionRegion;
}

export async function listMangakaRegions(
  seriesId: string,
  chapterId: string,
  pageId: string,
  authorEmail: string,
): Promise<MangaProductionRegion[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages/${pageId}/regions?authorEmail=${encodeURIComponent(authorEmail)}`,
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionRegion[];
}

export async function deleteMangakaRegion(
  seriesId: string,
  chapterId: string,
  pageId: string,
  regionId: string,
  authorEmail: string,
): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}?authorEmail=${encodeURIComponent(authorEmail)}`,
    { method: "DELETE" },
  );
  if (!response.ok) throw new Error(await parseError(response));
}

// --- Task API ---

export async function createMangakaTask(
  seriesId: string,
  chapterId: string,
  pageId: string,
  authorEmail: string,
  payload: MangaProductionTaskCreateRequest,
  regionId?: string,
): Promise<MangaProductionTask> {
  let url: string;
  if (regionId) {
    url = `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages/${pageId}/regions/${regionId}/tasks?authorEmail=${encodeURIComponent(authorEmail)}`;
  } else {
    url = `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages/${pageId}/tasks?authorEmail=${encodeURIComponent(authorEmail)}`;
  }
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionTask;
}

export async function approveMangakaTask(
  seriesId: string,
  chapterId: string,
  pageId: string,
  taskId: string,
  authorEmail: string,
): Promise<MangaProductionTask> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages/${pageId}/tasks/${taskId}/approve?authorEmail=${encodeURIComponent(authorEmail)}`,
    { method: "PUT" },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionTask;
}

export async function completeMangakaChapter(
  seriesId: string,
  chapterId: string,
  authorEmail: string,
): Promise<MangaProductionChapter> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/complete?authorEmail=${encodeURIComponent(authorEmail)}`,
    { method: "PUT" },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionChapter;
}

export async function redoMangakaTask(
  seriesId: string,
  chapterId: string,
  pageId: string,
  taskId: string,
  authorEmail: string,
): Promise<MangaProductionTask> {
  const response = await fetch(
    `${API_BASE_URL}/api/mangaka/series/${seriesId}/chapters/${chapterId}/pages/${pageId}/tasks/${taskId}/redo?authorEmail=${encodeURIComponent(authorEmail)}`,
    { method: "PUT" },
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await response.json()) as MangaProductionTask;
}

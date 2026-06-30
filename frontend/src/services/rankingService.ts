import type {
  ReaderMetric,
  ReaderMetricCreateRequest,
  SeriesRanking,
} from "../types/ranking";

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

async function readJson<T>(response: Response): Promise<T | null> {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text) as T;
  } catch {
    return null;
  }
}

export async function listMetrics(
  seriesId: string
): Promise<ReaderMetric[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/series/${seriesId}/metrics`
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await readJson<ReaderMetric[]>(response)) ?? [];
}

export async function createMetric(
  seriesId: string,
  request: ReaderMetricCreateRequest
): Promise<ReaderMetric | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/series/${seriesId}/metrics`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    }
  );
  if (!response.ok) throw new Error(await parseError(response));
  return readJson<ReaderMetric>(response);
}

export async function getSeriesRanking(
  seriesId: string
): Promise<SeriesRanking | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/series/${seriesId}/rankings`
  );
  if (!response.ok) throw new Error(await parseError(response));
  return readJson<SeriesRanking>(response);
}

export async function getAllRankings(): Promise<SeriesRanking[]> {
  const response = await fetch(`${API_BASE_URL}/api/rankings`);
  if (!response.ok) throw new Error(await parseError(response));
  return (await readJson<SeriesRanking[]>(response)) ?? [];
}

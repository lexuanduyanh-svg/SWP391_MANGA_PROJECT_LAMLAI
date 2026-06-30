import type {
  SeriesDecision,
  SeriesDecisionRequest,
} from "../types/seriesDecision";

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

export async function listDecisions(
  seriesId: string
): Promise<SeriesDecision[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/series/${seriesId}/decisions`
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await readJson<SeriesDecision[]>(response)) ?? [];
}

export async function makeDecision(
  seriesId: string,
  request: SeriesDecisionRequest
): Promise<SeriesDecision | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/series/${seriesId}/decisions`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    }
  );
  if (!response.ok) throw new Error(await parseError(response));
  return readJson<SeriesDecision>(response);
}

export async function listAllDecisions(): Promise<SeriesDecision[]> {
  const response = await fetch(`${API_BASE_URL}/api/decisions`);
  if (!response.ok) throw new Error(await parseError(response));
  return (await readJson<SeriesDecision[]>(response)) ?? [];
}

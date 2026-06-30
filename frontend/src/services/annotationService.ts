import type { Annotation, AnnotationCreateRequest } from "../types/annotation";

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

export async function listAnnotations(
  pageId: string
): Promise<Annotation[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/pages/${pageId}/annotations`
  );
  if (!response.ok) throw new Error(await parseError(response));
  return (await readJson<Annotation[]>(response)) ?? [];
}

export async function createAnnotation(
  pageId: string,
  request: AnnotationCreateRequest
): Promise<Annotation | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/pages/${pageId}/annotations`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    }
  );
  if (!response.ok) throw new Error(await parseError(response));
  return readJson<Annotation>(response);
}

export async function resolveAnnotation(
  pageId: string,
  annotationId: string
): Promise<Annotation | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/pages/${pageId}/annotations/${annotationId}/resolve`,
    { method: "PUT" }
  );
  if (!response.ok) throw new Error(await parseError(response));
  return readJson<Annotation>(response);
}

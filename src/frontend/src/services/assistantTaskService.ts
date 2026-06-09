import type {
  AssistantTask,
  AssistantTaskStartRequest,
  AssistantTaskSubmitRequest,
} from "../types/assistant";

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

export async function listAssistantTasks(
  assistantEmail: string,
): Promise<AssistantTask[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/assistant/tasks?assistantEmail=${encodeURIComponent(assistantEmail)}`,
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await readJson<AssistantTask[]>(response)) ?? [];
}

export async function startAssistantTask(
  taskId: string,
  payload: AssistantTaskStartRequest,
): Promise<AssistantTask | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/assistant/tasks/${taskId}/start`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return readJson<AssistantTask>(response);
}

export async function submitAssistantTask(
  taskId: string,
  payload: AssistantTaskSubmitRequest,
): Promise<AssistantTask | null> {
  const response = await fetch(
    `${API_BASE_URL}/api/assistant/tasks/${taskId}/submit`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return readJson<AssistantTask>(response);
}

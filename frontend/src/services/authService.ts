import type { LoginRequest, LoginResponse } from "../types/auth";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const AUTH_STORAGE_KEY = "mangaWorkflow.auth";

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(
      error?.message ?? "Không thể đăng nhập. Vui lòng thử lại sau.",
    );
  }

  const data = (await response.json()) as LoginResponse;
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data));
  return data;
}

export function getStoredAuth(): LoginResponse | null {
  const stored = localStorage.getItem(AUTH_STORAGE_KEY);
  return stored ? (JSON.parse(stored) as LoginResponse) : null;
}

export function logout(): void {
  localStorage.removeItem(AUTH_STORAGE_KEY);
}

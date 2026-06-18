import type {
  SkillCategory,
  SkillCategoryCreateRequest,
  SkillCategoryUpdateRequest,
} from "../types/admin";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function parseError(response: Response): Promise<string> {
  const error = await response.json().catch(() => null);
  return (
    error?.message ?? "Không thể thực hiện thao tác. Vui lòng thử lại sau."
  );
}

export async function listSkills(): Promise<SkillCategory[]> {
  const response = await fetch(`${API_BASE_URL}/api/admin/skills`);
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as SkillCategory[];
}

export async function createSkill(
  payload: SkillCategoryCreateRequest,
): Promise<SkillCategory> {
  const response = await fetch(`${API_BASE_URL}/api/admin/skills`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as SkillCategory;
}

export async function updateSkill(
  id: string,
  payload: SkillCategoryUpdateRequest,
): Promise<SkillCategory> {
  const response = await fetch(`${API_BASE_URL}/api/admin/skills/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as SkillCategory;
}

export async function toggleSkillStatus(
  id: string,
  active: boolean,
): Promise<SkillCategory> {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/skills/${id}/status`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ active }),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as SkillCategory;
}

export async function deleteSkill(id: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/admin/skills/${id}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
}

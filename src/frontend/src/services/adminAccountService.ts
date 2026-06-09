import type {
  AccountStatus,
  AdminAccount,
  AdminAccountCreateRequest,
  AdminAccountUpdateRequest,
} from "../types/admin";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function parseError(response: Response): Promise<string> {
  const error = await response.json().catch(() => null);
  return (
    error?.message ?? "Không thể thực hiện thao tác. Vui lòng thử lại sau."
  );
}

export async function listAdminAccounts(): Promise<AdminAccount[]> {
  const response = await fetch(`${API_BASE_URL}/api/admin/accounts`);
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as AdminAccount[];
}

export async function createAdminAccount(
  payload: AdminAccountCreateRequest,
): Promise<AdminAccount> {
  const response = await fetch(`${API_BASE_URL}/api/admin/accounts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as AdminAccount;
}

export async function updateAdminAccount(
  id: string,
  payload: AdminAccountUpdateRequest,
): Promise<AdminAccount> {
  const response = await fetch(`${API_BASE_URL}/api/admin/accounts/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as AdminAccount;
}

export async function changeAdminAccountStatus(
  id: string,
  status: AccountStatus,
): Promise<AdminAccount> {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/accounts/${id}/status`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status }),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return (await response.json()) as AdminAccount;
}

export async function updateAdminAccountSkills(
  id: string,
  skillIds: string[],
): Promise<AdminAccount> {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/accounts/${id}/skills`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ skillIds }),
    },
  );

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as AdminAccount;
}

export async function deleteAdminAccount(id: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/admin/accounts/${id}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
}

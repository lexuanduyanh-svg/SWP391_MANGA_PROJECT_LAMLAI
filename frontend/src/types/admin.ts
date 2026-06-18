import type { UserRole } from "./auth";

export type AccountStatus = "Active" | "Inactive" | "Suspended";

export interface AdminAccount {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
  status: AccountStatus;
  skills: SkillCategory[];
}

export interface AdminAccountCreateRequest {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface AdminAccountUpdateRequest {
  fullName: string;
  email: string;
  role: UserRole;
  status: AccountStatus;
}

export interface SkillCategory {
  id: string;
  name: string;
  description: string | null;
  active: boolean;
}

export interface SkillCategoryCreateRequest {
  name: string;
  description?: string;
}

export interface SkillCategoryUpdateRequest {
  name: string;
  description?: string;
}

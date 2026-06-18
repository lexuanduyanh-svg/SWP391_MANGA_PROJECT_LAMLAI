export type UserRole =
  | "Admin"
  | "Mangaka"
  | "Assistant"
  | "TantouEditor"
  | "EditorialBoardMember";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthenticatedUser {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
}

export interface LoginResponse {
  accessToken: string;
  user: AuthenticatedUser;
  dashboardPath: string;
}

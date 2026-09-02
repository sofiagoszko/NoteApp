export interface User {
  id: number;
  nickname: string;
  email: string;
  role: "USER" | "ADMIN";
}

export interface AuthResponse {
  token: string;
  user: User;
}

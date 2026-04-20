export interface User {
  id: number;
  nickname: string;
  email: string;
  role: "USER" | "ADMIN"; 
}

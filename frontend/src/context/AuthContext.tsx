import { createContext, useContext, useState, type ReactNode } from "react"
import type { User } from "../types/User"

interface AuthContextType {
  user: User | null;
  loginUser: (user: User) => void;
  logoutUser: () => void;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth debe usarse dentro de un AuthProvider");
  }
  return context;
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  const loginUser = (userData: User) => {
    localStorage.setItem("user", JSON.stringify(userData));
    setUser(userData);
  };

  const logoutUser = () => {
    localStorage.removeItem("user");
    setUser(null);
  };

  const isAdmin = user?.role === "ADMIN";

  return (
    <AuthContext.Provider value={{ user, loginUser, logoutUser, isAdmin }}>
      {children}
    </AuthContext.Provider>
  )
}


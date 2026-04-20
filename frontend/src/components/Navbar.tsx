import { Link, useNavigate } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import { LogOut, User } from "lucide-react"
import toast from "react-hot-toast"

export default function Navbar() {
  const { user, logoutUser, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutUser();
    toast.success("¡Hasta luego!");
    navigate("/");
  };

  return (
    <nav className="sticky top-0 z-40 flex items-center justify-between px-6 py-4 bg-white border-b-2 border-[#f3f4f6]">
      <Link to="/notes" className="flex items-center gap-2">
        <span className="font-black text-xl text-[#1a1a2e]">
          NoteApp
        </span>
      </Link>

      <div className="flex items-center gap-3">
        {isAdmin && (
          <span className="text-xs font-bold px-3 py-1 rounded-full bg-[#A855F7] text-white">
            ADMIN
          </span>
        )}
        <div className="flex items-center gap-2 px-3 py-2 rounded-xl bg-[#f3f4f6]">
          <User size={16} color="#6b7280" />
          <span className="text-sm font-bold text-[#1a1a2e]">{user?.nickname}</span>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 px-4 py-2 rounded-xl font-bold text-sm transition-all hover:scale-105 bg-[#1a1a2e] text-[#FFE66D]"
        >
          <LogOut size={16} />
          Salir
        </button>
      </div>
    </nav>
  )
}

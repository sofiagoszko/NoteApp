import { Link } from "react-router-dom";

export default function GuestNavbar() {
  return (
    <nav className="relative z-10 flex items-center justify-between px-8 py-5">
        <div className="flex items-center gap-2">
            <span className="font-bold text-xl text-[#1a1a2e]">
                NoteApp
            </span>
        </div>
        <div className="flex gap-3">
            <Link
                to="/login"
                className="px-5 py-2 rounded-full font-bold text-sm transition-all hover:scale-105 border-2 border-[#1a1a2e] text-[#1a1a2e] bg-transparent"
            >
                Iniciar sesión
            </Link>
            <Link
                to="/register"
                className="px-5 py-2 rounded-full font-bold text-sm transition-all hover:scale-105 text-white bg-[#1a1a2e]"
            >
                Registrarse
            </Link>
        </div>
    </nav>
  );
}
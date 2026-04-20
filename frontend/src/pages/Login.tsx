import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import type { User } from "../types/User"
import toast from "react-hot-toast"

export default function LoginPage() {
    const { loginUser } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const BASE_URL = import.meta.env.VITE_BASE_URL;

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setLoading(true);

        try {
            const res = await fetch(`${BASE_URL}/users/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            const data = await res.json();

            if (!res.ok) {
                toast.error(typeof data === "string" ? data : "Credenciales inválidas");
                return;
            }

            loginUser(data as User);
            toast.success(`¡Bienvenido, ${data.nickname}!`);
            navigate("/notes");
        } catch {
            toast.error("Error de conexión");
        } finally {
            setLoading(false);
        }
    };

    const inputClass = "w-full px-4 py-3 rounded-xl text-sm outline-none bg-white border-2 border-[#e5e7eb] transition-colors duration-200 hover:border-gray-400";
    const borderFocus = (e: React.FocusEvent<HTMLInputElement>) => (e.target.style.borderColor = "#FF6B6B");
    const borderBlur = (e: React.FocusEvent<HTMLInputElement>) => (e.target.style.borderColor = "#e5e7eb");

    return (
        <div className="min-h-screen flex bg-[#FAFAF8]">
            <div className="hidden lg:flex flex-col justify-between p-12 w-1/2 bg-[#1a1a2e]">
                <Link to="/" className="flex items-center gap-2">
                    <span className="font-black text-2xl text-[#FFE66D]">NoteApp</span>
                </Link>

                <div>
                    <p className="text-4xl font-extrabold text-white leading-tight">
                        Tus ideas te <span className="text-[#4ECDC4]">esperan.</span>
                    </p>
                    <p className="mt-4 text-lx text-[#9ca3af]">
                        Iniciá sesión para ver tus notas y seguir donde lo dejaste.
                    </p>
                </div>
            </div>

            {/* Formulario */}
            <div className="flex-1 flex flex-col justify-center items-center px-8">
                <div className="w-full max-w-md">
                    <div className="lg:hidden flex items-center gap-2 mb-8">
                        <span className="font-black text-2xl text-[#1a1a2e]">NoteApp</span>
                    </div>

                    <h2 className="text-3xl text-[#1a1a2e] font-black mb-8">
                        Iniciar sesión
                    </h2>
                    <p className="text-lx mb-8 text-[#6b7280]" >
                        ¿No tenés cuenta?{" "}
                        <Link to="/register" className="text-[#FF6B6B] font-bold text-lx">Registrate</Link>
                    </p>

                    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                        <div>
                            <label className="block font-bold mb-1 text-[#1a1a2e]">Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="tu@email.com"
                                required
                                className={inputClass}
                                onFocus={borderFocus}
                                onBlur={borderBlur}
                            />
                        </div>

                        <div>
                            <label className="block font-bold mb-1 text-[#1a1a2e]">Contraseña</label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                required
                                className={inputClass}
                                onBlur={borderBlur}
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full py-3 rounded-xl font-bold text-base transition-all hover:scale-[1.02] hover:shadow-lg disabled:opacity-60 mt-2 text-white bg-[#FF6B6B] hover:bg-[#FF5252]"
                            >
                            {loading ? "Ingresando..." : "Iniciar sesión"}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    )
}

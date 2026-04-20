import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import toast from "react-hot-toast"

interface FormState {
  nickname: string;
  email: string;
  password: string;
  passwordConfirm: string;
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<FormState>({
    nickname: "",
    email: "",
    password: "",
    passwordConfirm: "",
  })
  const [loading, setLoading] = useState(false);
  const BASE_URL = import.meta.env.VITE_BASE_URL;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (form.password !== form.passwordConfirm) {
      toast.error("Las contraseñas no coinciden")
      return
    }
    setLoading(true)
    try {
      const res = await fetch(`${BASE_URL}/users/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      })
      const data = await res.json()
      if (!res.ok) {
        toast.error(data?.message ?? data ?? "Error al registrarse")
        return
      }
      toast.success("Cuenta creada. Bienvenido")
      navigate("/")
    } catch {
      toast.error("Error de conexión")
    } finally {
      setLoading(false)
    }
  }

  const inputClass = "w-full px-4 py-3 rounded-xl text-sm outline-none bg-white border-2 border-[#e5e7eb] transition-colors duration-200 hover:border-gray-400"
  const borderFocus = (e: React.FocusEvent<HTMLInputElement>) => (e.target.style.borderColor = "#FF6B6B");
  const borderBlur = (e: React.FocusEvent<HTMLInputElement>) => (e.target.style.borderColor = "#e5e7eb");
  return (
    <div className="min-h-screen flex bg-bg">

      {/* Formulario */}
      <div className="flex-1 flex flex-col justify-center items-center px-8">
        <div className="w-full max-w-md">

          <Link to="/" className="block mb-8 font-[family-name:var(--font-display)] font-black text-2xl text-dark">
            NoteApp
          </Link>

          <h2 className="font-[family-name:var(--font-display)] font-black text-4xl text-dark mb-2">
            Creá una cuenta
          </h2>
          <p className="text-lx mb-8 text-[#6b7280]">
            Ya tenés una cuenta?{" "}
            <Link to="/login" className="text-[#FF6B6B] font-bold text-lx">Iniciá sesión</Link>
          </p>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label className="block font-bold mb-1 text-dark">Nombre de usuario</label>
              <input
                type="text"
                name="nickname"
                value={form.nickname}
                onChange={handleChange}
                placeholder="juanperez"
                required
                className={inputClass}
                onFocus={borderFocus}
                onBlur={borderBlur}
              />
            </div>

            <div>
              <label className="block font-bold mb-1 text-dark">Email</label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="tu@email.com"
                required
                className={inputClass}
                onFocus={borderFocus}
                onBlur={borderBlur}
              />
            </div>

            <div>
              <label className="block font-bold mb-1 text-dark">Contraseña</label>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Ingresa tu contraseña"
                required
                className={inputClass}
                onFocus={borderFocus}
                onBlur={borderBlur}
              />
            </div>

            <div>
              <label className="block font-bold mb-1 text-dark">Confirma tu contraseña</label>
              <input
                type="password"
                name="passwordConfirm"
                value={form.passwordConfirm}
                onChange={handleChange}
                placeholder="Repite tu contraseña"
                required
                className={inputClass}
                onFocus={borderFocus}
                onBlur={borderBlur}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-xl font-bold text-base transition-all hover:scale-[1.02] hover:shadow-lg disabled:opacity-60 mt-2 text-white bg-[#FF6B6B] hover:bg-[#FF5252]"
            >
              {loading ? "Creando cuenta..." : "Crear cuenta"}
            </button>

          </form>
        </div>
      </div>

      {/* Panel derecho */}
      <div className="hidden lg:flex flex-col justify-between p-12 w-1/2 bg-[#1a1a2e]">
        <Link to="/" className="flex items-center gap-2">
            <span className="font-black text-2xl text-[#FFE66D] text-right">NoteApp</span>
        </Link>

        <div>
            <p className="text-4xl font-extrabold text-white leading-tight text-right">
                Tus ideas te <span className="text-[#4ECDC4]">esperan.</span>
            </p>
            <p className="mt-4 text-lx text-[#9ca3af] text-right">
                Crea tu cuenta y empieza a anotar todo lo que importa en segundos.
            </p>
        </div>
      </div>
    </div>
  )
}

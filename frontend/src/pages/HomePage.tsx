import { Link } from "react-router-dom"
import type { User } from "../types/User"
import Navbar from "../components/Navbar"
import GuestNavbar from "../components/GuestNavbar"

const FloatingBlob = ({ style }: { style: React.CSSProperties }) => (
    <div className="absolute rounded-full blur-3xl opacity-20 pointer-events-none" style={style} />
)

type Card = {
    title: string
    desc: string
    color: string
}

export default function HomePage() {
    const cards: Card[] = [
        { title: "Creá notas", desc: "Escribí y editá tus ideas en segundos", color: "#FF6B6B" },
        { title: "Archivá", desc: "Guardá las notas que ya no necesitás ver", color: "#4ECDC4" },
        { title: "Filtrá", desc: "Encontrá lo que buscás por categoría", color: "#A855F7" },
    ]
    const storedUser = localStorage.getItem("user");
    const user: User = storedUser ? JSON.parse(storedUser) : null;

    return (
    <div className="min-h-screen relative overflow-hidden flex flex-col bg-[#FAFAF8]">

        <FloatingBlob style={{ width: 400, height: 400, background: "#FF6B6B", top: -100, right: -100 }} />
        <FloatingBlob style={{ width: 300, height: 300, background: "#4ECDC4", bottom: 100, left: -80 }} />
        <FloatingBlob style={{ width: 200, height: 200, background: "#A855F7", top: "40%", left: "50%" }} />

        {/* Navbar */}
        {user ? <Navbar /> : <GuestNavbar />}

        {/* Hero */}
        <main className="relative z-10 flex-1 flex flex-col items-center justify-center text-center px-6 py-16">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-bold mb-8 text-[#1a1a2e] bg-[#FFE66D]">
                Tus ideas, organizadas
            </div>

            <h1 className="mb-6 leading-tight font-bold text-[#1a1a2e] max-w-[700px] text-[clamp(2.5rem,7vw,5rem)]">
                Anotá todo lo que{" "}
                <span className="text-[#FF6B6B] underline decoration-wavy underline-offset-[6px]">
                    importa
                </span>
            </h1>

            <p className="text-lg mb-12 max-w-lg text-[#6b7280] leading-[1.7]">
                Creá notas, organizalas con categorías y encontralas cuando las necesitás. Simple, rápido y sin distracciones.
            </p>

            <div className="flex flex-wrap gap-4 justify-center">
                <Link
                    to="/register"
                    className="px-8 py-4 rounded-2xl font-bold text-lg transition-all hover:scale-105 hover:shadow-xl bg-[#FF6B6B] text-white"
                >
                    Empezar gratis
                </Link>
                <Link
                    to="/login"
                    className="px-8 py-4 rounded-2xl font-bold text-lg transition-all hover:scale-105 bg-white text-[#1a1a2e] border-2 border-[#e5e7eb] shadow-md"
                >
                    Ya tengo cuenta
                </Link>
            </div>

        {/* Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mt-20 max-w-3xl w-full">
            {cards.map((card) => (
                <div
                    key={card.title}
                    className="rounded-2xl p-6 text-left transition-all hover:-translate-y-1 hover:shadow-lg bg-white border-2 border-[#f3f4f6] shadow-md"
                >
                    <h3 className="font-bold text-lg mb-1 text-[#1a1a2e]">
                        {card.title}
                    </h3>
                    <p className="text-sm text-[#6b7280]">{card.desc}</p>
                </div>
            ))}
        </div>
        </main>

        <footer className="relative z-10 text-center py-6 text-sm text-[#1a1a2e]">
            Hecho por Sofia Lara Goszko · NoteApp 2026
        </footer>
    </div>
    )
}
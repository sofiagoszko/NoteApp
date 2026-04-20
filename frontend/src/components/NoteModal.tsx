import { useState } from "react"
import { X } from "lucide-react"
import type { Note } from "../types/Note"

interface Props {
  isOpen: boolean;
  onClose: () => void;
  onSave: (title: string, content: string) => Promise<void>;
  noteToEdit: Note | null;
}

export default function NoteModal({ isOpen, onClose, onSave, noteToEdit }: Props) {
  const [title, setTitle] = useState(noteToEdit?.title ?? "");
  const [content, setContent] = useState(noteToEdit?.content ?? "");
  const [loading, setLoading] = useState(false);


  if (!isOpen) return null;

  const handleSubmit = async (e:  React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;
    setLoading(true);
    await onSave(title.trim(), content.trim()); 
    setLoading(false);
  }

  const inputStyle: React.CSSProperties = {
    border: "2px solid #e5e7eb",
    background: "white",
    width: "100%",
    padding: "12px 16px",
    borderRadius: 12,
    fontSize: 14,
    outline: "none",
    transition: "border-color 0.2s",
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4, bg-[rgba(26,26,46,0.6)]"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="w-full max-w-lg rounded-3xl p-8 shadow-2xl bg-white">
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-black text-[#1a1a2e] text-2xl">
            {noteToEdit ? "Editar nota" : "Nueva nota"}
          </h2>
          <button onClick={onClose} className="p-2 rounded-xl transition-all hover:scale-110 bg-[#f3f4f6]">
            <X size={18} color="#6b7280" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block text-sm font-bold mb-1 text-[#1a1a2e]">Título</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Dale un nombre a tu nota..."
              required
              maxLength={100}
              style={inputStyle}
              onFocus={(e) => (e.target.style.borderColor = "#FF6B6B")}
              onBlur={(e) => (e.target.style.borderColor = "#e5e7eb")}
            />
          </div>

          <div>
            <label className="block text-sm font-bold mb-1 text-[#1a1a2e]">Contenido</label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Escribí tu nota acá..."
              required
              maxLength={500}
              rows={5}
              style={{ ...inputStyle, resize: "none" }}
              onFocus={(e) => (e.target.style.borderColor = "#FF6B6B")}
              onBlur={(e) => (e.target.style.borderColor = "#e5e7eb")}
            />
            <p className="text-xs mt-1 text-right text-[#9ca3af]">{content.length}/500</p>
          </div>

          <div className="flex gap-3 mt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-3 rounded-xl font-bold text-sm transition-all hover:scale-[1.02] bg-[#f3f4f6] text-[#6b7280]"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-3 rounded-xl font-bold text-sm transition-all hover:scale-[1.02] hover:shadow-lg disabled:opacity-60 bg-[#FF6B6B] text-white"
            >
              {loading ? "Guardando..." : noteToEdit ? "Guardar cambios" : "Crear nota →"}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

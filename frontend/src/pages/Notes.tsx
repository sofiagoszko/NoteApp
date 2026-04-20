import { useState, useEffect, useMemo, useCallback} from "react"
import { Plus } from "lucide-react"
import toast from "react-hot-toast"
import { useAuth } from "../context/AuthContext"
import type { Note } from "../types/Note"
import Navbar from "../components/Navbar"
import NoteCard from "../components/NoteCard"
import NoteModal from "../components/NoteModal"
import ConfirmModal from "../components/ConfirmModal"


export default function NotesPage() {
  const { user } = useAuth();
  const [notes, setNotes] = useState<Note[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState(true);
  const [noteModal, setNoteModal] = useState<{ open: boolean; noteToEdit: Note | null }>({ open: false, noteToEdit: null });
  const [confirmModal, setConfirmModal] = useState<{ open: boolean; note: Note | null }>({ open: false, note: null });
  const BASE_URL = import.meta.env.VITE_BASE_URL;

  const headers: HeadersInit = useMemo(() => ({
    "Content-Type": "application/json",
    "X-User-Id": String(user!.id),
  }), [user]);

  const fetchNotes = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(
        `${BASE_URL}/notes/users/${user!.id}/active?active=${activeTab}`,
        { method: "GET", headers }
      );
      if (!res.ok) throw new Error();
      const data: Note[] = await res.json();
      setNotes(data);
    } catch {
      toast.error("Error al cargar las notas");
    } finally {
      setLoading(false);
    }
  }, [user, activeTab, BASE_URL, headers]);

  useEffect(() => {
    fetchNotes();
  }, [fetchNotes]);

  const handleCreate = async (title: string, content: string) => {
    try {
      const res = await fetch(`${BASE_URL}/notes/users/${user!.id}/notes`, {
        method: "POST",
        headers,
        body: JSON.stringify({ title, content }),
      })
      if (!res.ok) throw new Error()
      toast.success("Nota creada");
      setNoteModal({ open: false, noteToEdit: null });
      fetchNotes();
    } catch {
      toast.error("Error al crear la nota");
    }
  }

  const handleEdit = async (title: string, content: string) => {
    const noteId = noteModal.noteToEdit!.id;
    try {
      const res = await fetch(`${BASE_URL}/notes/users/${user!.id}/notes/${noteId}`, {
        method: "PUT",
        headers,
        body: JSON.stringify({ title, content }),
      })
      if (!res.ok) throw new Error();
      toast.success("Nota actualizada");
      setNoteModal({ open: false, noteToEdit: null });
      fetchNotes();
    } catch {
      toast.error("Error al actualizar la nota");
    }
  }

  const handleToggle = async (note: Note) => {
    try {
      const res = await fetch(
        `${BASE_URL}/notes/users/${user!.id}/notes/${note.id}/toggle-active`, { 
          method: "PATCH", 
          headers,
        }
      )
      if (!res.ok) throw new Error();
      toast.success(note.active ? "Nota archivada" : "Nota restaurada");
      fetchNotes();
    } catch {
      toast.error("Error al cambiar el estado");
    }
  }

  const handleDelete = async () => {
    const note = confirmModal.note!;
    try {
      const res = await fetch(`${BASE_URL}/notes/users/${user!.id}/notes/${note.id}`, {
        method: "DELETE",
        headers,
      })
      if (!res.ok) throw new Error();
      toast.success("Nota eliminada");
      setConfirmModal({ open: false, note: null });
      fetchNotes();
    } catch {
      toast.error("Error al eliminar la nota");
    }
  }

  const handleSave = (title: string, content: string) =>
    noteModal.noteToEdit ? handleEdit(title, content) : handleCreate(title, content)

  return (
    <div className="min-h-screen bg-[#FAFAF8]">
      <Navbar />

      <main className="max-w-6xl mx-auto px-6 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="font-black text-[#1a1a2e] text-4xl">
              Mis notas
            </h1>
            <p className="text-sm mt-1 text-[#6b7280]">
              {notes.length} {activeTab ? "nota activa" : "nota archivada"}{notes.length !== 1 ? "s" : ""}
            </p>
          </div>

          <button
            onClick={() => setNoteModal({ open: true, noteToEdit: null })}
            className="flex items-center gap-2 px-5 py-3 rounded-2xl font-bold text-sm transition-all hover:scale-105 hover:shadow-lg bg-[#FF6B6B] text-white"
          >
            <Plus size={18} />
            Nueva nota
          </button>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 mb-8">
          {[{ label: "Activas", value: true }, { label: "Archivadas", value: false }].map((tab) => (
            <button
              key={String(tab.value)}
              onClick={() => setActiveTab(tab.value)}
              className="px-5 py-2 rounded-full font-bold text-sm transition-all hover:scale-105"
              style={{
                background: activeTab === tab.value ? "#1a1a2e" : "white",
                color: activeTab === tab.value ? "#FFE66D" : "#6b7280",
                border: "2px solid",
                borderColor: activeTab === tab.value ? "#1a1a2e" : "#e5e7eb",
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Grid */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="rounded-2xl animate-pulse bg-[#f3f4f6] h-[180px]" />
            ))}
          </div>
        ) : notes.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <h3  className="font-black text-[#1a1a2e] text-2xl mb-8">
              {activeTab ? "No tenés notas activas" : "No tenés notas archivadas"}
            </h3>
            <p className="text-sm mb-6 text-[#6b7280]">
              {activeTab ? "Creá tu primera nota para empezar." : "Las notas que archives van a aparecer acá."}
            </p>
            {activeTab && (
              <button
                onClick={() => setNoteModal({ open: true, noteToEdit: null })}
                className="px-6 py-3 rounded-2xl font-bold text-sm transition-all hover:scale-105 bg-[#FF6B6B] text-white"
              >
                + Crear primera nota
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {notes.map((note) => (
              <NoteCard
                key={note.id}
                note={note}
                onEdit={(note) => setNoteModal({ open: true, noteToEdit: note })}
                onToggle={handleToggle}
                onDelete={(note) => setConfirmModal({ open: true, note: note })}
              />
            ))}
          </div>
        )}
      </main>

      <NoteModal
        key={noteModal.noteToEdit?.id ?? "new"}
        isOpen={noteModal.open}
        onClose={() => setNoteModal({ open: false, noteToEdit: null })}
        onSave={handleSave}
        noteToEdit={noteModal.noteToEdit}
      />

      <ConfirmModal
        isOpen={confirmModal.open}
        onClose={() => setConfirmModal({ open: false, note: null })}
        onConfirm={handleDelete}
        title="¿Eliminar nota?"
        message={`"${confirmModal.note?.title}" se va a eliminar para siempre.`}
      />
    </div>
  )
}

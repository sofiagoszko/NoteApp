import { Archive, ArchiveRestore, Pencil, Trash2 } from "lucide-react"
import type { Note } from "../types/Note"

const CARD_COLORS = ["#FFF3E0", "#E8F5E9", "#E3F2FD", "#FCE4EC", "#F3E5F5", "#FFFDE7"];

interface Props {
  note: Note;
  onEdit: (note: Note) => void;
  onToggle: (note: Note) => void;
  onDelete: (note: Note) => void;
};

export default function NoteCard({ note, onEdit, onToggle, onDelete }: Props) {
  const bg = CARD_COLORS[note.id % CARD_COLORS.length];
  const date = note.updatedAt ?? note.createdAt;
  const dateStr = date
    ? new Date(date).toLocaleDateString("es-AR", { day: "2-digit", month: "short", year: "numeric" })
    : "";

  return (
    <div
      className="rounded-2xl p-5 flex flex-col gap-3 transition-all hover:-translate-y-1 hover:shadow-lg border-2 border-black/[0.05] min-h-[160px]"
      style={{ background: bg }}
    >
      <div className="flex items-start justify-between gap-2 text-lx">
        <h3 className="font-bold text-base leading-tight flex-1 text-[#1a1a2e]">
          {note.title}
        </h3>
        {!note.active && (
          <span className="font-bold px-2 py-1 rounded-full flex-shrink-0 bg-[#1a1a2e] text-[#FFE66D]">
            Archivada
          </span>
        )}
      </div>

      <p className="flex-1 line-clamp-4 text-[#374151]">
        {note.content}
      </p>

      <div className="flex items-center justify-between mt-auto pt-2 border-t border-black/[0.08]">
        <span className="text-xs text-[#9ca3af]">{dateStr}</span>

        <div className="flex items-center gap-1">
          <button
            onClick={() => onEdit(note)}
            className="p-2 rounded-lg transition-all hover:scale-110 bg-[rgba(0,0,0,0.06)]"
            title="Editar"
          >
            <Pencil size={14} color="#374151" />
          </button>
          <button
            onClick={() => onToggle(note)}
            className="p-2 rounded-lg transition-all hover:scale-110 bg-[rgba(0,0,0,0.06)]"
            title={note.active ? "Archivar" : "Desarchivar"}
          >
            {note.active ? <Archive size={14} color="#374151" /> : <ArchiveRestore size={14} color="#374151" />}
          </button>
          <button
            onClick={() => onDelete(note)}
            className="p-2 rounded-lg transition-all hover:scale-110 bg-[rgba(255,107,107,0.15)]"
            title="Eliminar"
          >
            <Trash2 size={14} color="#FF6B6B" />
          </button>
        </div>
      </div>
    </div>
  )
}

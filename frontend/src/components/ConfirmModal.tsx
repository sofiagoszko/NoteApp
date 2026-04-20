import { Trash2 } from "lucide-react"

interface Props {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title?: string;
  message?: string;
}

export default function ConfirmModal({ isOpen, onClose, onConfirm, title, message }: Props) {
  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[rgba(26,26,46,0.6)]"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="w-full max-w-sm rounded-3xl p-8 shadow-2xl text-center bg-white">
        <div className="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 bg-[#FFE4E4]">
          <Trash2 size={28} color="#FF6B6B" />
        </div>
        <h3  className="font-black text-[1a1a2e] text-xl mb-8">
          {title ?? "¿Eliminar nota?"}
        </h3>
        <p className="text-sm mb-6 text-[#6b7280]">
          {message ?? "Esta acción no se puede deshacer."}
        </p>
        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 py-3 rounded-xl font-bold text-sm transition-all hover:scale-[1.02] bg-[#f3f4f6] text-[#6b7280]"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            className="flex-1 py-3 rounded-xl font-bold text-sm transition-all hover:scale-[1.02] bg-[#FF6B6B] text-white"
          >
            Eliminar
          </button>
        </div>
      </div>
    </div>
  )
}

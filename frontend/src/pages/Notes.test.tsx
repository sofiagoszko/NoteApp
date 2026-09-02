import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import toast from 'react-hot-toast'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, test, vi } from 'vitest'

import { AuthProvider } from '../context/AuthContext'
import NotesPage from './Notes'

vi.mock('react-hot-toast', () => ({
  default: {
    error: vi.fn(),
    success: vi.fn(),
  },
}))

const fetchMock = vi.fn()
const currentUser = {
  id: 1,
  nickname: 'natalia',
  email: 'natalia@mail.com',
  role: 'USER',
}

function renderNotes() {
  localStorage.setItem('user', JSON.stringify(currentUser))
  localStorage.setItem('token', 'fake-token')

  return render(
    <AuthProvider>
      <MemoryRouter>
        <NotesPage />
      </MemoryRouter>
    </AuthProvider>,
  )
}

const titleInput = () => screen.getByPlaceholderText('Dale un nombre a tu nota...')
const contentInput = () => screen.getByPlaceholderText('Escribí tu nota acá...')

describe('NotesPage', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.stubEnv('VITE_BASE_URL', 'http://api.test')
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockReset()
  })

  test('loads and renders active notes with user header', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        {
          id: 10,
          title: 'Comprar pan',
          content: 'Pasar por la panadería',
          active: true,
          createdAt: '2026-09-01T10:00:00',
          updatedAt: null,
        },
      ],
    })

    renderNotes()

    expect(await screen.findByText('Comprar pan')).toBeInTheDocument()
    expect(screen.getByText('Pasar por la panadería')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledWith('http://api.test/notes/users/1/active?active=true', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer fake-token',
      },
    })
  })

  test('renders empty active state when there are no notes', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    })

    renderNotes()

    expect(await screen.findByText('No tenés notas activas')).toBeInTheDocument()
    expect(screen.getByText('Creá tu primera nota para empezar.')).toBeInTheDocument()
  })

  test('shows error when notes cannot be loaded', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: false,
      json: async () => [],
    })

    renderNotes()

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith('Error al cargar las notas'))
  })

  test('creates a note and reloads the list', async () => {
    const user = userEvent.setup()
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          {
            id: 11,
            title: 'Nueva idea',
            content: 'Contenido de prueba',
            active: true,
            createdAt: '2026-09-02T10:00:00',
            updatedAt: null,
          },
        ],
      })

    renderNotes()

    await screen.findByText('No tenés notas activas')
    await user.click(screen.getByRole('button', { name: /nueva nota/i }))
    await user.type(titleInput(), 'Nueva idea')
    await user.type(contentInput(), 'Contenido de prueba')
    await user.click(screen.getByRole('button', { name: /crear nota/i }))

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3))
    expect(fetchMock).toHaveBeenNthCalledWith(2, 'http://api.test/notes/users/1/notes', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer fake-token',
      },
      body: JSON.stringify({ title: 'Nueva idea', content: 'Contenido de prueba' }),
    })
    expect(await screen.findByText('Nueva idea')).toBeInTheDocument()
    expect(toast.success).toHaveBeenCalledWith('Nota creada')
  })

  test('archives a note and reloads the list', async () => {
    const user = userEvent.setup()
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          {
            id: 10,
            title: 'Comprar pan',
            content: 'Pasar por la panadería',
            active: true,
            createdAt: '2026-09-01T10:00:00',
            updatedAt: null,
          },
        ],
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      })

    renderNotes()

    await screen.findByText('Comprar pan')
    await user.click(screen.getByTitle('Archivar'))

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3))
    expect(fetchMock).toHaveBeenNthCalledWith(2, 'http://api.test/notes/users/1/notes/10/toggle-active', {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer fake-token',
      },
    })
    expect(toast.success).toHaveBeenCalledWith('Nota archivada')
  })

  test('edits a note and reloads the list', async () => {
    const user = userEvent.setup()
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          {
            id: 10,
            title: 'Comprar pan',
            content: 'Pasar por la panadería',
            active: true,
            createdAt: '2026-09-01T10:00:00',
            updatedAt: null,
          },
        ],
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      })

    renderNotes()

    await screen.findByText('Comprar pan')
    await user.click(screen.getByTitle('Editar'))
    await user.clear(titleInput())
    await user.type(titleInput(), 'Comprar leche')
    await user.clear(contentInput())
    await user.type(contentInput(), 'Pasar por el mercado')
    await user.click(screen.getByRole('button', { name: /guardar cambios/i }))

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3))
    expect(fetchMock).toHaveBeenNthCalledWith(2, 'http://api.test/notes/users/1/notes/10', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer fake-token',
      },
      body: JSON.stringify({ title: 'Comprar leche', content: 'Pasar por el mercado' }),
    })
    expect(toast.success).toHaveBeenCalledWith('Nota actualizada')
  })

  test('deletes a note after confirmation and reloads the list', async () => {
    const user = userEvent.setup()
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [
          {
            id: 10,
            title: 'Comprar pan',
            content: 'Pasar por la panadería',
            active: true,
            createdAt: '2026-09-01T10:00:00',
            updatedAt: null,
          },
        ],
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      })

    renderNotes()

    await screen.findByText('Comprar pan')
    await user.click(screen.getByTitle('Eliminar'))
    const dialog = screen.getByText('¿Eliminar nota?').closest('div')!
    await user.click(within(dialog).getByRole('button', { name: 'Eliminar' }))

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3))
    expect(fetchMock).toHaveBeenNthCalledWith(2, 'http://api.test/notes/users/1/notes/10', {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer fake-token',
      },
    })
    expect(toast.success).toHaveBeenCalledWith('Nota eliminada')
  })
})

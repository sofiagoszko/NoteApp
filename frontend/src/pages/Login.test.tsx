import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import toast from 'react-hot-toast'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, test, vi } from 'vitest'

import { AuthProvider } from '../context/AuthContext'
import LoginPage from './Login'

vi.mock('react-hot-toast', () => ({
  default: {
    error: vi.fn(),
    success: vi.fn(),
  },
}))

const fetchMock = vi.fn()

function renderLogin() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/notes" element={<h1>Notas</h1>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

const emailInput = () => screen.getByPlaceholderText('tu@email.com')
const passwordInput = () => screen.getByPlaceholderText('••••••••')

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.stubEnv('VITE_BASE_URL', 'http://api.test')
    vi.stubGlobal('fetch', fetchMock)
    fetchMock.mockReset()
  })

  test('renders login form', () => {
    renderLogin()

    expect(screen.getByRole('heading', { name: /iniciar sesión/i })).toBeInTheDocument()
    expect(emailInput()).toBeInTheDocument()
    expect(passwordInput()).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /iniciar sesión/i })).toBeInTheDocument()
  })

  test('submits credentials and redirects after successful login', async () => {
    const user = userEvent.setup()
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        token: 'fake-token',
        user: {
          id: 1,
          nickname: 'natalia',
          email: 'natalia@mail.com',
          role: 'USER',
        },
      }),
    })

    renderLogin()

    await user.type(emailInput(), 'natalia@mail.com')
    await user.type(passwordInput(), 'secret')
    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    await waitFor(() => expect(screen.getByRole('heading', { name: 'Notas' })).toBeInTheDocument())
    expect(fetchMock).toHaveBeenCalledWith('http://api.test/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'natalia@mail.com', password: 'secret' }),
    })
    expect(localStorage.getItem('user')).toContain('natalia@mail.com')
    expect(localStorage.getItem('token')).toBe('fake-token')
    expect(toast.success).toHaveBeenCalledWith('¡Bienvenido, natalia!')
  })

  test('shows error when credentials are invalid', async () => {
    const user = userEvent.setup()
    fetchMock.mockResolvedValueOnce({
      ok: false,
      json: async () => ({ success: false, message: 'Credenciales inválidas' }),
    })

    renderLogin()

    await user.type(emailInput(), 'natalia@mail.com')
    await user.type(passwordInput(), 'wrong')
    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith('Credenciales inválidas'))
    expect(screen.queryByRole('heading', { name: 'Notas' })).not.toBeInTheDocument()
  })

  test('requires email and password before submitting', async () => {
    const user = userEvent.setup()

    renderLogin()

    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    expect(fetchMock).not.toHaveBeenCalled()
  })
})

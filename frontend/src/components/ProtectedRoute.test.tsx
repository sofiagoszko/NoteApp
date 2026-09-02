import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, test } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

import ProtectedRoute from './ProtectedRoute'
import { AuthProvider } from '../context/AuthContext'

function renderProtectedRoute(initialPath = '/notes') {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[initialPath]}>
        <Routes>
          <Route
            path="/notes"
            element={
              <ProtectedRoute>
                <h1>Protected content</h1>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<h1>Login page</h1>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  test('renders children when user is authenticated', () => {
    localStorage.setItem(
      'user',
      JSON.stringify({
        id: 1,
        nickname: 'natalia',
        email: 'natalia@mail.com',
        role: 'USER',
      }),
    )

    renderProtectedRoute()

    expect(screen.getByRole('heading', { name: 'Protected content' })).toBeInTheDocument()
  })

  test('redirects unauthenticated users to login', () => {
    renderProtectedRoute()

    expect(screen.getByRole('heading', { name: 'Login page' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Protected content' })).not.toBeInTheDocument()
  })
})

import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, test } from 'vitest'

import { AuthProvider, useAuth } from './AuthContext'
import type { User } from '../types/User'

const regularUser: User = {
  id: 1,
  nickname: 'natalia',
  email: 'natalia@mail.com',
  role: 'USER',
}

const adminUser: User = {
  id: 2,
  nickname: 'admin',
  email: 'admin@noteapp.com',
  role: 'ADMIN',
}

function AuthConsumer() {
  const { user, loginUser, logoutUser, isAdmin } = useAuth()

  return (
    <div>
      <span>User: {user?.nickname ?? 'none'}</span>
      <span>Admin: {isAdmin ? 'yes' : 'no'}</span>
      <button onClick={() => loginUser(regularUser, 'fake-token')}>Login user</button>
      <button onClick={() => loginUser(adminUser, 'fake-token')}>Login admin</button>
      <button onClick={logoutUser}>Logout</button>
    </div>
  )
}

function renderAuthContext() {
  return render(
    <AuthProvider>
      <AuthConsumer />
    </AuthProvider>,
  )
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  test('starts without user when localStorage is empty', () => {
    renderAuthContext()

    expect(screen.getByText('User: none')).toBeInTheDocument()
    expect(screen.getByText('Admin: no')).toBeInTheDocument()
  })

  test('loads stored user from localStorage', () => {
    localStorage.setItem('user', JSON.stringify(adminUser))

    renderAuthContext()

    expect(screen.getByText('User: admin')).toBeInTheDocument()
    expect(screen.getByText('Admin: yes')).toBeInTheDocument()
  })

  test('login stores user and token and updates context state', async () => {
    const user = userEvent.setup()

    renderAuthContext()

    await user.click(screen.getByRole('button', { name: 'Login user' }))

    expect(screen.getByText('User: natalia')).toBeInTheDocument()
    expect(screen.getByText('Admin: no')).toBeInTheDocument()
    expect(localStorage.getItem('user')).toBe(JSON.stringify(regularUser))
    expect(localStorage.getItem('token')).toBe('fake-token')
  })

  test('login exposes admin derived state for admin users', async () => {
    const user = userEvent.setup()

    renderAuthContext()

    await user.click(screen.getByRole('button', { name: 'Login admin' }))

    expect(screen.getByText('User: admin')).toBeInTheDocument()
    expect(screen.getByText('Admin: yes')).toBeInTheDocument()
    expect(localStorage.getItem('user')).toBe(JSON.stringify(adminUser))
  })

  test('logout removes stored user and token and clears context state', async () => {
    const user = userEvent.setup()
    localStorage.setItem('user', JSON.stringify(regularUser))
    localStorage.setItem('token', 'fake-token')

    renderAuthContext()

    await user.click(screen.getByRole('button', { name: 'Logout' }))

    expect(screen.getByText('User: none')).toBeInTheDocument()
    expect(screen.getByText('Admin: no')).toBeInTheDocument()
    expect(localStorage.getItem('user')).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })
})

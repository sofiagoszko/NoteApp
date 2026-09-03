import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, test, vi } from 'vitest'

import ConfirmModal from './ConfirmModal'

describe('ConfirmModal', () => {
  test('does not render when closed', () => {
    render(<ConfirmModal isOpen={false} onClose={vi.fn()} onConfirm={vi.fn()} />)

    expect(screen.queryByText('¿Eliminar nota?')).not.toBeInTheDocument()
  })

  test('calls the right handlers from buttons and backdrop', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    const onConfirm = vi.fn()

    const { container } = render(
      <ConfirmModal
        isOpen
        onClose={onClose}
        onConfirm={onConfirm}
        title="Eliminar nota importante"
        message="Esta nota se quitará de la lista."
      />,
    )

    expect(screen.getByText('Eliminar nota importante')).toBeInTheDocument()
    expect(screen.getByText('Esta nota se quitará de la lista.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(onClose).toHaveBeenCalledTimes(1)

    await user.click(screen.getByRole('button', { name: 'Eliminar' }))
    expect(onConfirm).toHaveBeenCalledTimes(1)

    await user.click(container.firstElementChild as HTMLElement)
    expect(onClose).toHaveBeenCalledTimes(2)
  })
})

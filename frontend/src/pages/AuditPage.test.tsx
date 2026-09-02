import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { api } from '../api/client'
import type { WorkspaceOutletContext } from '../components/AppShell'
import { AuditPage } from './AuditPage'

vi.mock('../api/client', () => ({
  api: {
    audit: vi.fn(),
  },
}))

describe('AuditPage', () => {
  it('renders an event without a correlation id instead of crashing the route', async () => {
    vi.mocked(api.audit).mockResolvedValue([
      {
        id: 'event-1',
        workspaceId: 'workspace-1',
        actorUserId: 'actor-user-1',
        action: 'TICKET_CREATED',
        targetType: 'TICKET',
        targetId: 'ticket-target-1',
        outcome: 'SUCCEEDED',
        correlationId: null,
        occurredAt: '2026-09-02T12:31:34Z',
      },
    ])
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const context = {
      user: { id: 'actor-user-1', email: 'admin@test.invalid', displayName: 'Admin', enabled: true },
      workspace: { id: 'workspace-1', slug: 'test', name: 'Test', role: 'ADMIN' },
      canManage: true,
      isAdmin: true,
    } satisfies WorkspaceOutletContext

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <Routes>
            <Route element={<Outlet context={context} />}>
              <Route index element={<AuditPage />} />
            </Route>
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    )

    expect(await screen.findByText('无关联 ID')).toBeInTheDocument()
    expect(screen.getByText('ticket created')).toBeInTheDocument()
  })
})

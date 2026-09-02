import { QueryClient } from '@tanstack/react-query'
import { describe, expect, it } from 'vitest'
import { clearAuthenticatedSession } from './session'

describe('clearAuthenticatedSession', () => {
  it('notifies the active session query and removes tenant-scoped data', () => {
    const queryClient = new QueryClient()
    queryClient.setQueryData(['me'], { id: 'user-1' })
    queryClient.setQueryData(['tickets', 'workspace-1'], [{ id: 'ticket-1' }])

    clearAuthenticatedSession(queryClient)

    expect(queryClient.getQueryData(['me'])).toBeNull()
    expect(queryClient.getQueryData(['tickets', 'workspace-1'])).toBeUndefined()
  })
})

import type { QueryClient } from '@tanstack/react-query'
import type { CurrentUser } from '../api/types'

export function clearAuthenticatedSession(queryClient: QueryClient) {
  queryClient.setQueryData<CurrentUser | null>(['me'], null)
  queryClient.removeQueries({
    predicate: (query) => query.queryKey[0] !== 'me',
  })
}

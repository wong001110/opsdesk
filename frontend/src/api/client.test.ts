import { afterEach, describe, expect, it, vi } from 'vitest'
import { api, apiRequest } from './client'

describe('apiRequest', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    document.cookie = 'XSRF-TOKEN=; Max-Age=0'
  })

  it('sends the decoded CSRF cookie and includes the server session on mutations', async () => {
    document.cookie = 'XSRF-TOKEN=a%2Bb%2Fc'
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ ok: true }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    await apiRequest<{ ok: boolean }>('/api/v1/example', { method: 'POST', body: '{}' })
    const [, init] = fetchMock.mock.calls[0]
    const headers = new Headers(init?.headers)
    expect(init?.credentials).toBe('include')
    expect(headers.get('X-XSRF-TOKEN')).toBe('a+b/c')
  })

  it('obtains a CSRF token before the first mutation', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(new Response(JSON.stringify({ token: 'fresh-token' }), { status: 200 })).mockResolvedValueOnce(new Response(JSON.stringify({ ok: true }), { status: 200 }))
    await apiRequest<{ ok: boolean }>('/api/v1/example', { method: 'POST', body: '{}' })
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/v1/auth/csrf', { credentials: 'include' })
    const [, mutationInit] = fetchMock.mock.calls[1]
    expect(new Headers(mutationInit?.headers).get('X-XSRF-TOKEN')).toBe('fresh-token')
  })

  it('imports a controlled provider using only its server option id', async () => {
    document.cookie = 'XSRF-TOKEN=secure-token'
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ id: 'profile-1' }), { status: 200 }),
    )

    await api.importProvider('workspace-1', '生产辅助', 'deepseek-chat')

    const [path, init] = fetchMock.mock.calls[0]
    expect(path).toBe('/api/v1/workspaces/workspace-1/provider-profiles/import')
    expect(JSON.parse(String(init?.body))).toEqual({
      name: '生产辅助',
      importOptionId: 'deepseek-chat',
    })
    expect(String(init?.body)).not.toMatch(/key|credential|secret/i)
  })
})

import type {
  AuditEvent,
  ClassificationResult,
  CurrentUser,
  Membership,
  ProviderImportOptions,
  ProviderProfile,
  SummaryResult,
  Ticket,
  TicketStatus,
  Workspace,
  WorkspaceRole,
} from './types'

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

function readCookie(name: string): string | undefined {
  const prefix = `${name}=`
  const cookie = document.cookie
    .split(';')
    .map((entry) => entry.trim())
    .find((entry) => entry.startsWith(prefix))
  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : undefined
}

async function ensureCsrfToken(): Promise<string> {
  const existing = readCookie('XSRF-TOKEN')
  if (existing) return existing

  const response = await fetch('/api/v1/auth/csrf', { credentials: 'include' })
  if (!response.ok) throw new ApiError('无法初始化安全会话', response.status)
  const body = (await response.json()) as { token?: string }
  const token = body.token ?? readCookie('XSRF-TOKEN')
  if (!token) throw new ApiError('安全令牌缺失，请刷新页面', 403)
  return token
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const method = (options.method ?? 'GET').toUpperCase()
  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    headers.set('X-XSRF-TOKEN', await ensureCsrfToken())
  }

  const response = await fetch(path, { ...options, method, headers, credentials: 'include' })
  if (!response.ok) {
    let message = response.status === 401 ? '登录状态已失效' : `请求失败 (${response.status})`
    try {
      const body = (await response.json()) as { message?: string }
      if (body.message) message = body.message
    } catch {
      // Non-JSON proxy and infrastructure errors keep the safe fallback above.
    }
    throw new ApiError(message, response.status)
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

const json = (body: unknown): string => JSON.stringify(body)
const workspacePath = (workspaceId: string): string => `/api/v1/workspaces/${workspaceId}`

export const api = {
  me: () => apiRequest<CurrentUser>('/api/v1/auth/me'),
  login: (email: string, password: string) =>
    apiRequest<CurrentUser>('/api/v1/auth/login', {
      method: 'POST',
      body: json({ email, password }),
    }),
  logout: () => apiRequest<void>('/api/v1/auth/logout', { method: 'POST' }),
  workspaces: () => apiRequest<Workspace[]>('/api/v1/workspaces'),
  createWorkspace: (name: string, slug: string) =>
    apiRequest<Workspace>('/api/v1/workspaces', { method: 'POST', body: json({ name, slug }) }),
  memberships: (workspaceId: string) =>
    apiRequest<Membership[]>(`${workspacePath(workspaceId)}/memberships`),
  addMembership: (workspaceId: string, email: string, role: WorkspaceRole) =>
    apiRequest<Membership>(`${workspacePath(workspaceId)}/memberships`, {
      method: 'POST',
      body: json({ email, role }),
    }),
  changeMembershipRole: (workspaceId: string, userId: string, role: WorkspaceRole) =>
    apiRequest<Membership>(`${workspacePath(workspaceId)}/memberships/${userId}`, {
      method: 'PATCH',
      body: json({ role }),
    }),
  removeMembership: (workspaceId: string, userId: string) =>
    apiRequest<void>(`${workspacePath(workspaceId)}/memberships/${userId}`, { method: 'DELETE' }),
  tickets: (workspaceId: string) => apiRequest<Ticket[]>(`${workspacePath(workspaceId)}/tickets`),
  ticket: (workspaceId: string, ticketId: string) =>
    apiRequest<Ticket>(`${workspacePath(workspaceId)}/tickets/${ticketId}`),
  createTicket: (workspaceId: string, title: string, description: string) =>
    apiRequest<Ticket>(`${workspacePath(workspaceId)}/tickets`, {
      method: 'POST',
      body: json({ title, description }),
    }),
  changeTicketStatus: (workspaceId: string, ticketId: string, status: TicketStatus) =>
    apiRequest<Ticket>(`${workspacePath(workspaceId)}/tickets/${ticketId}/status`, {
      method: 'PATCH',
      body: json({ status }),
    }),
  providers: (workspaceId: string) =>
    apiRequest<ProviderProfile[]>(`${workspacePath(workspaceId)}/provider-profiles`),
  createProvider: (
    workspaceId: string,
    input: { name: string; trustedOrigin: string; credentialReference: string },
  ) =>
    apiRequest<ProviderProfile>(`${workspacePath(workspaceId)}/provider-profiles`, {
      method: 'POST',
      body: json({ ...input, providerType: 'MOCK' }),
    }),
  providerImportOptions: (workspaceId: string) =>
    apiRequest<ProviderImportOptions>(`${workspacePath(workspaceId)}/provider-profiles/import-options`),
  importProvider: (workspaceId: string, name: string, importOptionId: string) =>
    apiRequest<ProviderProfile>(`${workspacePath(workspaceId)}/provider-profiles/import`, {
      method: 'POST',
      body: json({ name, importOptionId }),
    }),
  classifyTicket: (workspaceId: string, ticketId: string, providerProfileId: string) =>
    apiRequest<ClassificationResult>(`${workspacePath(workspaceId)}/tickets/${ticketId}/ai/classify`, {
      method: 'POST',
      body: json({ providerProfileId }),
    }),
  summarizeTicket: (workspaceId: string, ticketId: string, providerProfileId: string) =>
    apiRequest<SummaryResult>(`${workspacePath(workspaceId)}/tickets/${ticketId}/ai/summarize`, {
      method: 'POST',
      body: json({ providerProfileId }),
    }),
  audit: (workspaceId: string) =>
    apiRequest<AuditEvent[]>(`${workspacePath(workspaceId)}/audit-events?limit=100`),
}

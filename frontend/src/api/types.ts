export type WorkspaceRole = 'ADMIN' | 'MANAGER' | 'MEMBER'
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'DONE'
export type ProviderType = 'MOCK' | 'DEEPSEEK'

export interface CurrentUser {
  id: string
  email: string
  displayName: string
  enabled: boolean
}

export interface Workspace {
  id: string
  slug: string
  name: string
  role: WorkspaceRole
}

export interface Membership {
  userId: string
  email: string
  displayName: string
  role: WorkspaceRole
  active: boolean
}

export interface Ticket {
  id: string
  workspaceId: string
  title: string
  description: string | null
  status: TicketStatus
  createdByUserId: string
  assignedToUserId: string | null
  createdAt: string
  updatedAt: string
}

export interface ProviderProfile {
  id: string
  name: string
  providerType: ProviderType
  model: string
  trustedOrigin: string
  credentialConfigured: boolean
  enabled: boolean
}

export interface ProviderImportOption {
  id: string
  label: string
  providerType: 'DEEPSEEK'
  model: string
  trustedOrigin: string
}

export interface ProviderImportOptions {
  options: ProviderImportOption[]
}

export interface ClassificationResult {
  ticketId: string
  providerProfileId: string
  mode: string
  classification: string
}

export interface SummaryResult {
  ticketId: string
  providerProfileId: string
  mode: string
  summary: string
}

export interface AuditEvent {
  id: string
  workspaceId: string
  actorUserId: string
  action: string
  targetType: string
  targetId: string
  outcome: 'SUCCEEDED' | 'FAILED'
  correlationId: string | null
  occurredAt: string
}

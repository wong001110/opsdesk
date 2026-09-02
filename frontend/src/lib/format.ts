import type { TicketStatus, WorkspaceRole } from '../api/types'

export const roleLabel: Record<WorkspaceRole, string> = {
  ADMIN: '管理员',
  MANAGER: '负责人',
  MEMBER: '成员',
}

export const statusLabel: Record<TicketStatus, string> = {
  OPEN: '待处理',
  IN_PROGRESS: '处理中',
  DONE: '已完成',
}

export function nextTicketStatus(status: TicketStatus): TicketStatus | null {
  if (status === 'OPEN') return 'IN_PROGRESS'
  if (status === 'IN_PROGRESS') return 'DONE'
  return null
}

export function formatDate(value: string): string {
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export function initials(value: string): string {
  return value.trim().slice(0, 2).toUpperCase()
}

export function humanizeAction(value: string): string {
  return value.toLowerCase().split('_').join(' ')
}

import type { TicketStatus } from '../api/types'
import { statusLabel } from '../lib/format'

export function StatusPill({ status }: { status: TicketStatus }) {
  return <span className={`status-pill status-${status.toLowerCase()}`}>{statusLabel[status]}</span>
}

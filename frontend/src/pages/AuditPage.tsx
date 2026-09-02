import { useQuery } from '@tanstack/react-query'
import { CheckCircle2, Fingerprint } from 'lucide-react'
import { Navigate, useOutletContext } from 'react-router-dom'
import { api } from '../api/client'
import type { WorkspaceOutletContext } from '../components/AppShell'
import { EmptyState, ErrorBlock, LoadingBlock } from '../components/Feedback'
import { PageHeader } from '../components/PageHeader'
import { formatDate, humanizeAction } from '../lib/format'

export function AuditPage() {
  const { workspace, canManage } = useOutletContext<WorkspaceOutletContext>()
  const audit = useQuery({ queryKey: ['audit', workspace.id], queryFn: () => api.audit(workspace.id), enabled: canManage })
  if (!canManage) return <Navigate to="../overview" replace />

  return (
    <div className="page-stack">
      <PageHeader eyebrow="结构化证据" title="审计记录" description="只呈现固定元数据，不记录请求正文、工单内容或凭据引用。" />
      {audit.isPending ? <LoadingBlock /> : null}
      {audit.isError ? <ErrorBlock message={audit.error.message} /> : null}
      {audit.data?.length === 0 ? <EmptyState title="暂无审计事件" detail="工作区操作发生后，会在这里形成结构化记录。" /> : null}
      {audit.data?.length ? (
        <div className="audit-timeline">
          {audit.data.map((event) => (
            <article className="audit-event" key={event.id}>
              <span className="timeline-mark"><CheckCircle2 size={18} /></span>
              <div className="audit-copy"><div><strong>{humanizeAction(event.action)}</strong><span className={`outcome ${event.outcome.toLowerCase()}`}>{event.outcome}</span></div><p>{event.targetType.toLowerCase()} · {event.targetId.slice(0, 8)}</p><small>{formatDate(event.occurredAt)} · 操作者 {event.actorUserId.slice(0, 8)}</small></div>
              <span className="correlation">
                <Fingerprint size={15} />
                {event.correlationId ? event.correlationId.slice(0, 8) : '无关联 ID'}
              </span>
            </article>
          ))}
        </div>
      ) : null}
    </div>
  )
}

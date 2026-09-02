import { useQuery } from '@tanstack/react-query'
import { Activity, ArrowRight, Bot, CircleDot, Clock3, TicketCheck } from 'lucide-react'
import { Link, useOutletContext } from 'react-router-dom'
import { api } from '../api/client'
import type { WorkspaceOutletContext } from '../components/AppShell'
import { ErrorBlock, LoadingBlock } from '../components/Feedback'
import { PageHeader } from '../components/PageHeader'
import { StatusPill } from '../components/StatusPill'
import { formatDate } from '../lib/format'

export function OverviewPage() {
  const { workspace, canManage } = useOutletContext<WorkspaceOutletContext>()
  const tickets = useQuery({ queryKey: ['tickets', workspace.id], queryFn: () => api.tickets(workspace.id) })
  const providers = useQuery({
    queryKey: ['providers', workspace.id],
    queryFn: () => api.providers(workspace.id),
    enabled: canManage,
  })
  const audit = useQuery({
    queryKey: ['audit', workspace.id],
    queryFn: () => api.audit(workspace.id),
    enabled: canManage,
  })

  if (tickets.isPending) return <LoadingBlock label="正在整理运营概况" />
  if (tickets.isError) return <ErrorBlock message={tickets.error.message} />

  const openCount = tickets.data.filter((ticket) => ticket.status === 'OPEN').length
  const activeCount = tickets.data.filter((ticket) => ticket.status === 'IN_PROGRESS').length
  const doneCount = tickets.data.filter((ticket) => ticket.status === 'DONE').length
  const recentTickets = tickets.data.slice(0, 5)

  return (
    <div className="page-stack">
      <PageHeader eyebrow={workspace.slug} title="运营总览" description="把队列状态、团队动作和自动化入口放在同一张图上。" actions={<Link className="button primary" to="../tickets">进入工单队列<ArrowRight size={17} /></Link>} />

      <section className="metric-grid" aria-label="工单指标">
        <MetricCard label="待处理" value={openCount} detail="等待第一次响应" icon={<CircleDot size={19} />} tone="orange" />
        <MetricCard label="处理中" value={activeCount} detail="团队正在推进" icon={<Clock3 size={19} />} tone="blue" />
        <MetricCard label="已完成" value={doneCount} detail="本工作区累计" icon={<TicketCheck size={19} />} tone="green" />
        <MetricCard label="模型配置" value={canManage ? (providers.data?.length ?? '—') : '受限'} detail={canManage ? '可用于工单辅助' : '由负责人管理'} icon={<Bot size={19} />} tone="ink" />
      </section>

      <section className="split-grid">
        <div className="panel">
          <div className="panel-heading"><div><p className="eyebrow">实时队列</p><h2>最近工单</h2></div><Link to="../tickets">查看全部</Link></div>
          <div className="compact-list">
            {recentTickets.length === 0 ? <p className="muted-copy">还没有工单，创建一条来启动工作流。</p> : recentTickets.map((ticket) => (
              <Link className="compact-ticket" key={ticket.id} to={`../tickets/${ticket.id}`}>
                <span><strong>{ticket.title}</strong><small>{formatDate(ticket.updatedAt)}</small></span>
                <StatusPill status={ticket.status} />
              </Link>
            ))}
          </div>
        </div>
        <div className="panel signal-panel">
          <div className="panel-heading"><div><p className="eyebrow">系统信号</p><h2>可信活动</h2></div><Activity size={20} /></div>
          {!canManage ? <p className="muted-copy">负责人和管理员可以查看结构化审计记录。</p> : audit.isPending ? <LoadingBlock /> : audit.isError ? <ErrorBlock message={audit.error.message} /> : (
            <div className="signal-list">
              {audit.data.slice(0, 5).map((event) => (
                <div key={event.id}><span className="signal-dot" /><span><strong>{event.action.toLowerCase().replaceAll('_', ' ')}</strong><small>{formatDate(event.occurredAt)} · {event.outcome}</small></span></div>
              ))}
              {audit.data.length === 0 ? <p className="muted-copy">暂无审计事件。</p> : null}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

function MetricCard({ label, value, detail, icon, tone }: { label: string; value: number | string; detail: string; icon: React.ReactNode; tone: string }) {
  return <article className="metric-card"><div className={`metric-icon ${tone}`}>{icon}</div><p>{label}</p><strong>{value}</strong><small>{detail}</small></article>
}

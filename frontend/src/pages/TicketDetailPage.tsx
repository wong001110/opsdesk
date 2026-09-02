import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, ArrowRight, Bot, Calendar, Server, Sparkles, UserRound } from 'lucide-react'
import { useState } from 'react'
import { Link, useOutletContext, useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { ClassificationResult, SummaryResult } from '../api/types'
import type { WorkspaceOutletContext } from '../components/AppShell'
import { ErrorBlock, LoadingBlock } from '../components/Feedback'
import { StatusPill } from '../components/StatusPill'
import { formatDate, nextTicketStatus, statusLabel } from '../lib/format'

export function TicketDetailPage() {
  const { ticketId = '' } = useParams()
  const { workspace, canManage } = useOutletContext<WorkspaceOutletContext>()
  const queryClient = useQueryClient()
  const [profileId, setProfileId] = useState('')
  const [aiResult, setAiResult] = useState<ClassificationResult | SummaryResult | null>(null)
  const ticket = useQuery({ queryKey: ['ticket', workspace.id, ticketId], queryFn: () => api.ticket(workspace.id, ticketId) })
  const providers = useQuery({
    queryKey: ['providers', workspace.id],
    queryFn: () => api.providers(workspace.id),
    enabled: canManage,
  })
  const transition = useMutation({
    mutationFn: (status: NonNullable<ReturnType<typeof nextTicketStatus>>) => api.changeTicketStatus(workspace.id, ticketId, status),
    onSuccess: (updated) => {
      queryClient.setQueryData(['ticket', workspace.id, ticketId], updated)
      void queryClient.invalidateQueries({ queryKey: ['tickets', workspace.id] })
    },
  })
  const classify = useMutation({
    mutationFn: () => api.classifyTicket(workspace.id, ticketId, profileId),
    onSuccess: setAiResult,
  })
  const summarize = useMutation({
    mutationFn: () => api.summarizeTicket(workspace.id, ticketId, profileId),
    onSuccess: setAiResult,
  })

  if (ticket.isPending) return <LoadingBlock label="正在打开工单" />
  if (ticket.isError) return <ErrorBlock message={ticket.error.message} />
  const nextStatus = nextTicketStatus(ticket.data.status)
  const aiError = classify.error ?? summarize.error
  const selectedProvider = providers.data?.find((provider) => provider.id === profileId)

  return (
    <div className="page-stack">
      <Link className="back-link" to="../tickets"><ArrowLeft size={17} />返回工单列表</Link>
      <section className="ticket-detail-head">
        <div><div className="ticket-meta"><StatusPill status={ticket.data.status} /><span>#{ticket.data.id.slice(0, 8)}</span></div><h1>{ticket.data.title}</h1></div>
        {canManage && nextStatus ? <button className="button primary" disabled={transition.isPending} onClick={() => transition.mutate(nextStatus)}>{transition.isPending ? '更新中…' : `推进至${statusLabel[nextStatus]}`}<ArrowRight size={17} /></button> : null}
      </section>
      {transition.isError ? <ErrorBlock message={transition.error.message} /> : null}
      <div className="ticket-layout">
        <article className="panel ticket-body">
          <p className="eyebrow">问题描述</p>
          <div className="ticket-description">{ticket.data.description || '没有补充描述。'}</div>
          <dl className="detail-grid">
            <div><dt><Calendar size={16} />创建时间</dt><dd>{formatDate(ticket.data.createdAt)}</dd></div>
            <div><dt><Calendar size={16} />最后更新</dt><dd>{formatDate(ticket.data.updatedAt)}</dd></div>
            <div><dt><UserRound size={16} />创建者</dt><dd>{ticket.data.createdByUserId.slice(0, 8)}</dd></div>
            <div><dt><UserRound size={16} />负责人</dt><dd>{ticket.data.assignedToUserId?.slice(0, 8) ?? '未分配'}</dd></div>
          </dl>
        </article>
        <aside className="panel ai-panel">
          <div className="panel-heading"><div><p className="eyebrow">受限自动化</p><h2>AI 辅助判断</h2></div><span className="feature-icon"><Bot size={19} /></span></div>
          <p className="muted-copy">无论使用本地 MOCK 还是真实模型，都只会读取当前工单并返回分析结果，不具备修改工单、成员、角色或模型配置的权限。</p>
          {!canManage ? <div className="notice neutral-notice">模型配置目前仅对负责人可见，因此成员暂不能从界面发起分析。</div> : providers.isPending ? <LoadingBlock /> : providers.isError ? <ErrorBlock message={providers.error.message} /> : providers.data.length === 0 ? <div className="notice neutral-notice">先在“模型配置”中创建或导入一个配置。</div> : (
            <>
              <label className="field"><span>执行配置</span><select value={profileId} onChange={(event) => setProfileId(event.target.value)}><option value="">选择模型配置</option>{providers.data.map((profile) => <option key={profile.id} value={profile.id}>{profile.name} · {profile.providerType} / {profile.model}</option>)}</select></label>
              {selectedProvider?.providerType === 'DEEPSEEK' ? <div className="notice provider-execution-notice"><Server size={17} /><span>将通过服务器调用 {selectedProvider.model}。模型只返回分析文本，产品写操作仍由 OpsDesk 权限系统控制。</span></div> : null}
              {aiError ? <ErrorBlock message={aiError.message} /> : null}
              <div className="ai-actions"><button className="button secondary" disabled={!profileId || classify.isPending || summarize.isPending} onClick={() => classify.mutate()}><Sparkles size={17} />智能分类</button><button className="button secondary" disabled={!profileId || classify.isPending || summarize.isPending} onClick={() => summarize.mutate()}><Sparkles size={17} />生成摘要</button></div>
            </>
          )}
          {aiResult ? <div className="ai-result"><span>{'classification' in aiResult ? '分类结果' : '内容摘要'} · {aiResult.mode}</span><strong>{'classification' in aiResult ? aiResult.classification : aiResult.summary}</strong></div> : null}
        </aside>
      </div>
    </div>
  )
}

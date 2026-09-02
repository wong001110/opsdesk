import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ListFilter, Plus, Search, TicketCheck } from 'lucide-react'
import { useDeferredValue, useState } from 'react'
import { Link, useOutletContext } from 'react-router-dom'
import { api } from '../api/client'
import type { TicketStatus } from '../api/types'
import type { WorkspaceOutletContext } from '../components/AppShell'
import { EmptyState, ErrorBlock, LoadingBlock } from '../components/Feedback'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { StatusPill } from '../components/StatusPill'
import { formatDate } from '../lib/format'

export function TicketsPage() {
  const { workspace } = useOutletContext<WorkspaceOutletContext>()
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<'ALL' | TicketStatus>('ALL')
  const [creating, setCreating] = useState(false)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const deferredSearch = useDeferredValue(search.trim().toLowerCase())
  const tickets = useQuery({ queryKey: ['tickets', workspace.id], queryFn: () => api.tickets(workspace.id) })
  const create = useMutation({
    mutationFn: () => api.createTicket(workspace.id, title, description),
    onSuccess: () => {
      setCreating(false); setTitle(''); setDescription('')
      void queryClient.invalidateQueries({ queryKey: ['tickets', workspace.id] })
    },
  })

  const filtered = tickets.data?.filter((ticket) => {
    const matchesStatus = status === 'ALL' || ticket.status === status
    const matchesSearch = !deferredSearch || `${ticket.title} ${ticket.description ?? ''}`.toLowerCase().includes(deferredSearch)
    return matchesStatus && matchesSearch
  })

  return (
    <div className="page-stack">
      <PageHeader eyebrow="响应队列" title="工单" description="捕获问题、推进状态，并保留完整的操作轨迹。" actions={<button className="button primary" onClick={() => setCreating(true)}><Plus size={18} />创建工单</button>} />
      <div className="toolbar">
        <label className="search-field"><Search size={18} /><span className="sr-only">搜索工单</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="搜索标题或描述" /></label>
        <label className="select-field"><ListFilter size={17} /><span className="sr-only">按状态筛选</span><select value={status} onChange={(event) => setStatus(event.target.value as 'ALL' | TicketStatus)}><option value="ALL">全部状态</option><option value="OPEN">待处理</option><option value="IN_PROGRESS">处理中</option><option value="DONE">已完成</option></select></label>
      </div>
      {tickets.isPending ? <LoadingBlock /> : null}
      {tickets.isError ? <ErrorBlock message={tickets.error.message} /> : null}
      {filtered?.length === 0 ? <EmptyState title="没有符合条件的工单" detail="调整筛选条件，或者创建一条新的工单。" /> : null}
      {filtered?.length ? (
        <div className="table-wrap">
          <table>
            <thead><tr><th>工单</th><th>状态</th><th>更新时间</th><th><span className="sr-only">打开</span></th></tr></thead>
            <tbody>{filtered.map((ticket) => (
              <tr key={ticket.id}>
                <td><Link className="ticket-title" to={ticket.id}><span className="ticket-glyph"><TicketCheck size={17} /></span><span><strong>{ticket.title}</strong><small>{ticket.description || '无描述'}</small></span></Link></td>
                <td><StatusPill status={ticket.status} /></td><td>{formatDate(ticket.updatedAt)}</td><td><Link className="row-link" to={ticket.id}>查看</Link></td>
              </tr>
            ))}</tbody>
          </table>
        </div>
      ) : null}

      {creating ? (
        <Modal title="创建工单" description="描述清楚现象和影响，团队会更快开始处理。" onClose={() => setCreating(false)}>
          <form className="stack-form" onSubmit={(event) => { event.preventDefault(); create.mutate() }}>
            {create.isError ? <ErrorBlock message={create.error.message} /> : null}
            <label className="field"><span>标题</span><input autoFocus required maxLength={200} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="一句话说明发生了什么" /></label>
            <label className="field"><span>描述</span><textarea rows={6} maxLength={8000} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="补充影响范围、复现方式或期望结果" /></label>
            <div className="form-actions"><button type="button" className="button ghost" onClick={() => setCreating(false)}>取消</button><button className="button primary" disabled={create.isPending}>{create.isPending ? '创建中…' : '创建工单'}</button></div>
          </form>
        </Modal>
      ) : null}
    </div>
  )
}

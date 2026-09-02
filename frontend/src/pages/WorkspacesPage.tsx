import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowRight, Building2, LogOut, Plus } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { CurrentUser } from '../api/types'
import { EmptyState, ErrorBlock, LoadingBlock } from '../components/Feedback'
import { Modal } from '../components/Modal'
import { initials, roleLabel } from '../lib/format'
import { clearAuthenticatedSession } from '../lib/session'

export function WorkspacesPage({ user }: { user: CurrentUser }) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [name, setName] = useState('')
  const [slug, setSlug] = useState('')
  const workspaces = useQuery({ queryKey: ['workspaces'], queryFn: api.workspaces })
  const create = useMutation({
    mutationFn: () => api.createWorkspace(name, slug),
    onSuccess: (workspace) => {
      queryClient.setQueryData(['workspaces'], [...(workspaces.data ?? []), workspace])
      navigate(`/w/${workspace.id}/overview`)
    },
  })

  function updateName(nextName: string) {
    setName(nextName)
    setSlug(nextName.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, ''))
  }

  async function logout() {
    await api.logout()
    clearAuthenticatedSession(queryClient)
    navigate('/login', { replace: true })
  }

  return (
    <main className="workspace-page">
      <header className="workspace-topbar">
        <div className="login-brand dark"><span className="brand-mark">O</span><strong>OpsDesk</strong></div>
        <div className="workspace-account">
          <span className="avatar small">{initials(user.displayName)}</span>
          <span>{user.displayName}</span>
          <button className="icon-button" onClick={() => void logout()} aria-label="退出"><LogOut size={18} /></button>
        </div>
      </header>
      <section className="workspace-content">
        <div className="workspace-hero">
          <div><p className="eyebrow">工作区</p><h1>今天从哪里开始？</h1><p>选择团队空间，查看正在推进的响应工作。</p></div>
          <button className="button primary" onClick={() => setCreating(true)}><Plus size={18} />新建工作区</button>
        </div>
        {workspaces.isPending ? <LoadingBlock /> : null}
        {workspaces.isError ? <ErrorBlock message={workspaces.error.message} /> : null}
        {workspaces.data?.length === 0 ? <EmptyState title="还没有工作区" detail="创建第一个空间，你会自动成为管理员。" /> : null}
        <div className="workspace-grid">
          {workspaces.data?.map((workspace) => (
            <button className="workspace-card" key={workspace.id} onClick={() => navigate(`/w/${workspace.id}/overview`)}>
              <span className="workspace-symbol"><Building2 size={22} /></span>
              <span className="workspace-card-copy"><strong>{workspace.name}</strong><small>{workspace.slug} · {roleLabel[workspace.role]}</small></span>
              <ArrowRight size={20} />
            </button>
          ))}
        </div>
      </section>

      {creating ? (
        <Modal title="创建工作区" description="建立一个独立的团队和数据边界。" onClose={() => setCreating(false)}>
          <form className="stack-form" onSubmit={(event) => { event.preventDefault(); create.mutate() }}>
            {create.isError ? <ErrorBlock message={create.error.message} /> : null}
            <label className="field"><span>名称</span><input autoFocus required maxLength={160} value={name} onChange={(event) => updateName(event.target.value)} placeholder="例如：客户成功团队" /></label>
            <label className="field"><span>标识</span><input required maxLength={80} pattern="[a-z0-9-]+" value={slug} onChange={(event) => setSlug(event.target.value.toLowerCase())} placeholder="customer-success" /><small>仅小写英文、数字和连字符</small></label>
            <div className="form-actions"><button type="button" className="button ghost" onClick={() => setCreating(false)}>取消</button><button className="button primary" disabled={create.isPending}>{create.isPending ? '创建中…' : '创建并进入'}</button></div>
          </form>
        </Modal>
      ) : null}
    </main>
  )
}

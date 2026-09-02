import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { MoreHorizontal, Plus, ShieldCheck, UserRound } from 'lucide-react'
import { useState } from 'react'
import { Navigate, useOutletContext } from 'react-router-dom'
import { api } from '../api/client'
import type { Membership, WorkspaceRole } from '../api/types'
import type { WorkspaceOutletContext } from '../components/AppShell'
import { EmptyState, ErrorBlock, LoadingBlock } from '../components/Feedback'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { initials, roleLabel } from '../lib/format'

export function MembersPage() {
  const { workspace, canManage, isAdmin, user } = useOutletContext<WorkspaceOutletContext>()
  const queryClient = useQueryClient()
  const [adding, setAdding] = useState(false)
  const [email, setEmail] = useState('')
  const [role, setRole] = useState<WorkspaceRole>('MEMBER')
  const members = useQuery({ queryKey: ['memberships', workspace.id], queryFn: () => api.memberships(workspace.id), enabled: canManage })
  const add = useMutation({
    mutationFn: () => api.addMembership(workspace.id, email, role),
    onSuccess: () => { setAdding(false); setEmail(''); setRole('MEMBER'); void queryClient.invalidateQueries({ queryKey: ['memberships', workspace.id] }) },
  })
  const update = useMutation({
    mutationFn: ({ member, role: nextRole }: { member: Membership; role: WorkspaceRole }) => api.changeMembershipRole(workspace.id, member.userId, nextRole),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['memberships', workspace.id] }),
  })
  const remove = useMutation({
    mutationFn: (member: Membership) => api.removeMembership(workspace.id, member.userId),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['memberships', workspace.id] }),
  })

  if (!canManage) return <Navigate to="../overview" replace />

  return (
    <div className="page-stack">
      <PageHeader eyebrow="团队与权限" title="成员" description="角色只在当前工作区生效，身份与权限保持分离。" actions={isAdmin ? <button className="button primary" onClick={() => setAdding(true)}><Plus size={18} />添加成员</button> : undefined} />
      <div className="notice neutral-notice"><ShieldCheck size={18} /><span>管理员可维护成员；负责人拥有只读可见性。系统会阻止移除最后一位管理员。</span></div>
      {members.isPending ? <LoadingBlock /> : null}
      {members.isError ? <ErrorBlock message={members.error.message} /> : null}
      {update.isError ? <ErrorBlock message={update.error.message} /> : null}
      {remove.isError ? <ErrorBlock message={remove.error.message} /> : null}
      {members.data?.length === 0 ? <EmptyState title="暂无成员" detail="工作区至少应保留一位管理员。" /> : null}
      <div className="member-grid">
        {members.data?.map((member) => (
          <article className="member-card" key={member.userId}>
            <span className="avatar large-avatar">{initials(member.displayName)}</span>
            <div className="member-info"><strong>{member.displayName}{member.userId === user.id ? <em>你</em> : null}</strong><span>{member.email}</span></div>
            {isAdmin ? (
              <div className="member-actions">
                <label><span className="sr-only">更改 {member.displayName} 的角色</span><select value={member.role} disabled={update.isPending} onChange={(event) => update.mutate({ member, role: event.target.value as WorkspaceRole })}>{(['ADMIN', 'MANAGER', 'MEMBER'] as WorkspaceRole[]).map((item) => <option value={item} key={item}>{roleLabel[item]}</option>)}</select></label>
                <button className="icon-button danger" disabled={remove.isPending} onClick={() => { if (window.confirm(`确定移除 ${member.displayName}？`)) remove.mutate(member) }} aria-label={`移除 ${member.displayName}`}><MoreHorizontal size={18} /></button>
              </div>
            ) : <span className="role-chip"><UserRound size={15} />{roleLabel[member.role]}</span>}
          </article>
        ))}
      </div>

      {adding ? (
        <Modal title="添加成员" description="用户必须已经拥有一个有效的 OpsDesk 账户。" onClose={() => setAdding(false)}>
          <form className="stack-form" onSubmit={(event) => { event.preventDefault(); add.mutate() }}>
            {add.isError ? <ErrorBlock message={add.error.message} /> : null}
            <label className="field"><span>用户邮箱</span><input autoFocus required type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="colleague@company.com" /></label>
            <label className="field"><span>工作区角色</span><select value={role} onChange={(event) => setRole(event.target.value as WorkspaceRole)}><option value="MEMBER">成员</option><option value="MANAGER">负责人</option><option value="ADMIN">管理员</option></select></label>
            <div className="form-actions"><button type="button" className="button ghost" onClick={() => setAdding(false)}>取消</button><button className="button primary" disabled={add.isPending}>{add.isPending ? '添加中…' : '添加成员'}</button></div>
          </form>
        </Modal>
      ) : null}
    </div>
  )
}

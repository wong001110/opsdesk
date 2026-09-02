import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Activity,
  Bot,
  Building2,
  ChevronDown,
  LayoutDashboard,
  LogOut,
  Menu,
  TicketCheck,
  Users,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { CurrentUser, Workspace } from '../api/types'
import { initials, roleLabel } from '../lib/format'
import { clearAuthenticatedSession } from '../lib/session'
import { ErrorBlock, LoadingBlock } from './Feedback'

export interface WorkspaceOutletContext {
  user: CurrentUser
  workspace: Workspace
  canManage: boolean
  isAdmin: boolean
}

const navItems = [
  { to: 'overview', label: '总览', icon: LayoutDashboard, restricted: false },
  { to: 'tickets', label: '工单', icon: TicketCheck, restricted: false },
  { to: 'members', label: '成员', icon: Users, restricted: true },
  { to: 'providers', label: '模型配置', icon: Bot, restricted: true },
  { to: 'audit', label: '审计记录', icon: Activity, restricted: true },
] as const

export function AppShell({ user }: { user: CurrentUser }) {
  const { workspaceId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()
  const [mobileOpen, setMobileOpen] = useState(false)
  const workspaces = useQuery({ queryKey: ['workspaces'], queryFn: api.workspaces })
  const workspace = workspaces.data?.find((candidate) => candidate.id === workspaceId)

  async function logout() {
    await api.logout()
    clearAuthenticatedSession(queryClient)
    navigate('/login', { replace: true })
  }

  if (workspaces.isPending) return <LoadingBlock label="正在打开工作区" />
  if (workspaces.isError) return <ErrorBlock message={workspaces.error.message} />
  if (!workspace) return <MissingWorkspace />

  const canManage = workspace.role === 'ADMIN' || workspace.role === 'MANAGER'
  const section = navItems.find((item) => location.pathname.endsWith(item.to))?.label ?? 'OpsDesk'

  return (
    <div className="app-frame">
      <header className="mobile-bar">
        <button className="icon-button light" onClick={() => setMobileOpen(true)} aria-label="打开导航">
          <Menu size={20} />
        </button>
        <span>{section}</span>
        <span className="avatar small">{initials(user.displayName)}</span>
      </header>

      <aside className={`sidebar ${mobileOpen ? 'mobile-open' : ''}`} aria-label="主导航">
        <div className="brand-row">
          <div className="brand-mark">O</div>
          <div><strong>OpsDesk</strong><span>Operations hub</span></div>
          <button className="icon-button sidebar-close" onClick={() => setMobileOpen(false)} aria-label="关闭导航">
            <X size={19} />
          </button>
        </div>

        <label className="workspace-picker">
          <span>当前工作区</span>
          <div>
            <Building2 size={16} aria-hidden="true" />
            <select
              value={workspace.id}
              onChange={(event) => navigate(`/w/${event.target.value}/overview`)}
              aria-label="切换工作区"
            >
              {workspaces.data.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
            </select>
            <ChevronDown size={15} aria-hidden="true" />
          </div>
        </label>

        <nav className="nav-list">
          {navItems
            .filter((item) => !item.restricted || canManage)
            .map(({ to, label, icon: Icon }) => (
              <NavLink key={to} to={to} onClick={() => setMobileOpen(false)}>
                <Icon size={18} aria-hidden="true" />
                <span>{label}</span>
              </NavLink>
            ))}
        </nav>

        <div className="sidebar-foot">
          <div className="user-card">
            <span className="avatar">{initials(user.displayName)}</span>
            <div><strong>{user.displayName}</strong><span>{roleLabel[workspace.role]}</span></div>
          </div>
          <button className="logout-button" type="button" onClick={() => void logout()}>
            <LogOut size={17} aria-hidden="true" />退出
          </button>
        </div>
      </aside>
      {mobileOpen ? <button className="sidebar-scrim" aria-label="关闭导航" onClick={() => setMobileOpen(false)} /> : null}

      <main className="app-main">
        <Outlet context={{ user, workspace, canManage, isAdmin: workspace.role === 'ADMIN' } satisfies WorkspaceOutletContext} />
      </main>
    </div>
  )
}

function MissingWorkspace() {
  const navigate = useNavigate()
  return (
    <main className="centered-state">
      <Building2 size={36} aria-hidden="true" />
      <h1>找不到这个工作区</h1>
      <p>它可能已不可用，或你不再是其中的成员。</p>
      <button className="button primary" onClick={() => navigate('/workspaces')}>返回工作区</button>
    </main>
  )
}

import { useQuery } from '@tanstack/react-query'
import { Navigate, Route, Routes } from 'react-router-dom'
import { api, ApiError } from './api/client'
import { AppShell } from './components/AppShell'
import { LoadingBlock } from './components/Feedback'
import { AuditPage } from './pages/AuditPage'
import { LoginPage } from './pages/LoginPage'
import { MembersPage } from './pages/MembersPage'
import { OverviewPage } from './pages/OverviewPage'
import { ProvidersPage } from './pages/ProvidersPage'
import { TicketDetailPage } from './pages/TicketDetailPage'
import { TicketsPage } from './pages/TicketsPage'
import { WorkspacesPage } from './pages/WorkspacesPage'

export function App() {
  const session = useQuery({ queryKey: ['me'], queryFn: api.me, retry: false })

  if (session.isPending) {
    return <div className="app-splash"><div className="brand-mark large">O</div><LoadingBlock label="正在恢复工作台" /></div>
  }

  const signedOut = session.error instanceof ApiError && session.error.status === 401
  if (session.isError && !signedOut) {
    return (
      <main className="centered-state">
        <h1>暂时无法连接 OpsDesk</h1>
        <p>{session.error.message}</p>
        <button className="button primary" onClick={() => void session.refetch()}>重新连接</button>
      </main>
    )
  }

  if (!session.data) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    )
  }

  return (
    <Routes>
      <Route path="/login" element={<Navigate to="/workspaces" replace />} />
      <Route path="/workspaces" element={<WorkspacesPage user={session.data} />} />
      <Route path="/w/:workspaceId" element={<AppShell user={session.data} />}>
        <Route index element={<Navigate to="overview" replace />} />
        <Route path="overview" element={<OverviewPage />} />
        <Route path="tickets" element={<TicketsPage />} />
        <Route path="tickets/:ticketId" element={<TicketDetailPage />} />
        <Route path="members" element={<MembersPage />} />
        <Route path="providers" element={<ProvidersPage />} />
        <Route path="audit" element={<AuditPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/workspaces" replace />} />
    </Routes>
  )
}

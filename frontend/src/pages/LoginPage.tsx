import { useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowRight, Check, ShieldCheck, Sparkles } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { ErrorBlock } from '../components/Feedback'

const proofPoints = [
  '工作区级权限与租户隔离',
  '工单流转与结构化审计',
  '受限、可追踪的 AI 辅助',
]

export function LoginPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const login = useMutation({
    mutationFn: () => api.login(email, password),
    onSuccess: (user) => {
      queryClient.setQueryData(['me'], user)
      navigate('/workspaces', { replace: true })
    },
  })

  return (
    <main className="login-page">
      <section className="login-story" aria-label="OpsDesk 产品介绍">
        <div className="login-brand"><span className="brand-mark">O</span><strong>OpsDesk</strong></div>
        <div className="story-copy">
          <p className="eyebrow inverse">团队运营工作台</p>
          <h1>让每一次响应<br />都有清晰的下一步。</h1>
          <p>从问题进入队列，到成员协同和 AI 辅助判断，关键信息始终停留在正确的权限边界内。</p>
          <ul>
            {proofPoints.map((point) => <li key={point}><Check size={17} />{point}</li>)}
          </ul>
        </div>
        <div className="trust-note"><ShieldCheck size={18} /><span>凭据只用于建立受保护会话，不会保存在浏览器存储中。</span></div>
      </section>

      <section className="login-panel">
        <form
          className="login-form"
          onSubmit={(event) => {
            event.preventDefault()
            login.mutate()
          }}
        >
          <div className="form-heading">
            <span className="feature-icon"><Sparkles size={19} /></span>
            <p className="eyebrow">欢迎回来</p>
            <h2>进入你的运营空间</h2>
            <p>使用管理员提供的 OpsDesk 账户登录。</p>
          </div>
          {login.isError ? <ErrorBlock message={login.error.message} /> : null}
          <label className="field">
            <span>邮箱</span>
            <input
              type="email"
              autoComplete="username"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@company.com"
            />
          </label>
          <label className="field">
            <span>密码</span>
            <input
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="输入密码"
            />
          </label>
          <button className="button primary wide" type="submit" disabled={login.isPending}>
            {login.isPending ? '正在验证…' : '登录工作台'}<ArrowRight size={18} />
          </button>
          <p className="form-footnote">本地 MVP · 会话将在服务端管理</p>
        </form>
      </section>
    </main>
  )
}

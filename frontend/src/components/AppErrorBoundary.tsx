import { Component, type ErrorInfo, type ReactNode } from 'react'

interface AppErrorBoundaryProps {
  children: ReactNode
}

interface AppErrorBoundaryState {
  failed: boolean
}

export class AppErrorBoundary extends Component<AppErrorBoundaryProps, AppErrorBoundaryState> {
  state: AppErrorBoundaryState = { failed: false }

  static getDerivedStateFromError(): AppErrorBoundaryState {
    return { failed: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('OpsDesk UI render failed', error, info.componentStack)
  }

  render() {
    if (this.state.failed) {
      return (
        <main className="centered-state">
          <h1>这个页面暂时无法显示</h1>
          <p>你的数据没有被修改。请刷新页面；如果问题持续出现，请查看服务日志。</p>
          <button className="button primary" type="button" onClick={() => window.location.reload()}>
            刷新页面
          </button>
        </main>
      )
    }

    return this.props.children
  }
}

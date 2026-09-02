import { AlertCircle, Inbox } from 'lucide-react'

export function LoadingBlock({ label = '正在加载' }: { label?: string }) {
  return (
    <div className="loading-block" role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}

export function ErrorBlock({ message }: { message: string }) {
  return (
    <div className="notice error-notice" role="alert">
      <AlertCircle size={18} aria-hidden="true" />
      <span>{message}</span>
    </div>
  )
}

export function EmptyState({ title, detail }: { title: string; detail: string }) {
  return (
    <div className="empty-state">
      <Inbox size={28} aria-hidden="true" />
      <strong>{title}</strong>
      <p>{detail}</p>
    </div>
  )
}

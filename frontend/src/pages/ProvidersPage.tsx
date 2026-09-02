import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bot, CheckCircle2, CloudCog, FlaskConical, LockKeyhole, Plus, Server } from 'lucide-react'
import { useState } from 'react'
import { Navigate, useOutletContext } from 'react-router-dom'
import { api } from '../api/client'
import type { ProviderType } from '../api/types'
import type { WorkspaceOutletContext } from '../components/AppShell'
import { EmptyState, ErrorBlock, LoadingBlock } from '../components/Feedback'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'

export function ProvidersPage() {
  const { workspace, canManage, isAdmin } = useOutletContext<WorkspaceOutletContext>()
  const queryClient = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [providerType, setProviderType] = useState<ProviderType>('MOCK')
  const [name, setName] = useState('')
  const [importOptionId, setImportOptionId] = useState('')
  const [trustedOrigin, setTrustedOrigin] = useState('https://mock.invalid')
  const [credentialReference, setCredentialReference] = useState('env:OPSDESK_MOCK_TOKEN')
  const providers = useQuery({ queryKey: ['providers', workspace.id], queryFn: () => api.providers(workspace.id), enabled: canManage })
  const importOptions = useQuery({
    queryKey: ['provider-import-options', workspace.id],
    queryFn: () => api.providerImportOptions(workspace.id),
    enabled: isAdmin && creating && providerType === 'DEEPSEEK',
  })
  const createMock = useMutation({
    mutationFn: () => api.createProvider(workspace.id, { name, trustedOrigin, credentialReference }),
    onSuccess: finishCreate,
  })
  const importProvider = useMutation({
    mutationFn: () => api.importProvider(workspace.id, name, importOptionId),
    onSuccess: finishCreate,
  })

  function finishCreate() {
    setCreating(false)
    setName('')
    setImportOptionId('')
    setProviderType('MOCK')
    void queryClient.invalidateQueries({ queryKey: ['providers', workspace.id] })
  }

  const createError = createMock.error ?? importProvider.error
  const isCreating = createMock.isPending || importProvider.isPending

  if (!canManage) return <Navigate to="../overview" replace />

  return (
    <div className="page-stack">
      <PageHeader eyebrow="自动化边界" title="模型配置" description="把模型、可信来源与服务器凭据显式绑定；浏览器永远不会接收真实密钥。" actions={isAdmin ? <button className="button primary" onClick={() => setCreating(true)}><Plus size={18} />新建配置</button> : undefined} />
      <div className="notice neutral-notice"><LockKeyhole size={18} /><span>真实模型只能从服务器已经批准的选项导入；前端不提供 Key 输入框，也不会接收凭据引用。</span></div>
      {providers.isPending ? <LoadingBlock /> : null}
      {providers.isError ? <ErrorBlock message={providers.error.message} /> : null}
      {providers.data?.length === 0 ? <EmptyState title="还没有模型配置" detail={isAdmin ? '创建 MOCK 配置，或从服务器导入一个已批准的 DeepSeek 模型。' : '请联系工作区管理员完成配置。'} /> : null}
      <div className="provider-grid">
        {providers.data?.map((provider) => (
          <article className="provider-card" key={provider.id}>
            <div className="provider-top"><span className={`provider-icon ${provider.providerType === 'DEEPSEEK' ? 'remote' : ''}`}>{provider.providerType === 'DEEPSEEK' ? <CloudCog size={22} /> : <Bot size={22} />}</span><span className="health-label"><span />可用</span></div>
            <h2>{provider.name}</h2>
            <p>{provider.trustedOrigin}</p>
            <dl><div><dt>提供方</dt><dd>{provider.providerType}</dd></div><div><dt>模型</dt><dd>{provider.model}</dd></div><div><dt>凭据状态</dt><dd>{provider.credentialConfigured ? <><CheckCircle2 size={15} />服务器已配置</> : '未配置'}</dd></div><div><dt>执行权限</dt><dd>只读分析</dd></div></dl>
          </article>
        ))}
      </div>

      {creating ? (
        <Modal title="新建模型配置" description="先选择执行方式。真实模型只允许导入服务器提供的受控选项。" onClose={() => setCreating(false)}>
          <form className="stack-form" onSubmit={(event) => {
            event.preventDefault()
            if (providerType === 'MOCK') createMock.mutate()
            else importProvider.mutate()
          }}>
            <fieldset className="provider-mode-fieldset">
              <legend>执行方式</legend>
              <label className={providerType === 'MOCK' ? 'selected' : ''}><input type="radio" name="providerType" value="MOCK" checked={providerType === 'MOCK'} onChange={() => setProviderType('MOCK')} /><FlaskConical size={18} /><span><strong>本地 MOCK</strong><small>确定性结果，不访问网络</small></span></label>
              <label className={providerType === 'DEEPSEEK' ? 'selected' : ''}><input type="radio" name="providerType" value="DEEPSEEK" checked={providerType === 'DEEPSEEK'} onChange={() => setProviderType('DEEPSEEK')} /><Server size={18} /><span><strong>受控 DeepSeek</strong><small>使用服务器已配置的凭据</small></span></label>
            </fieldset>
            {createError ? <ErrorBlock message={createError.message} /> : null}
            <label className="field"><span>配置名称</span><input autoFocus required maxLength={120} value={name} onChange={(event) => setName(event.target.value)} placeholder="本地工单助手" /></label>
            {providerType === 'MOCK' ? <><label className="field"><span>可信来源</span><input required value={trustedOrigin} onChange={(event) => setTrustedOrigin(event.target.value)} /></label><label className="field"><span>测试凭据引用</span><input required value={credentialReference} onChange={(event) => setCredentialReference(event.target.value)} aria-describedby="credential-hint" /><small id="credential-hint">仅 MOCK 接受 env:NAME 或 secret://path；不要粘贴真实密钥。</small></label></> : importOptions.isPending ? <LoadingBlock label="正在读取服务器可用模型" /> : importOptions.isError ? <ErrorBlock message={importOptions.error.message} /> : importOptions.data.options.length === 0 ? <div className="notice neutral-notice">服务器尚未配置可导入的 DeepSeek 模型。</div> : <label className="field"><span>服务器模型</span><select required value={importOptionId} onChange={(event) => setImportOptionId(event.target.value)}><option value="">选择已批准的模型</option>{importOptions.data.options.map((option) => <option key={option.id} value={option.id}>{option.label} · {option.model}</option>)}</select><small>来源和凭据由服务器控制，前端只提交选项 ID。</small></label>}
            <div className="form-actions"><button type="button" className="button ghost" onClick={() => setCreating(false)}>取消</button><button className="button primary" disabled={isCreating || (providerType === 'DEEPSEEK' && !importOptionId)}>{isCreating ? '创建中…' : providerType === 'DEEPSEEK' ? '导入配置' : '创建配置'}</button></div>
          </form>
        </Modal>
      ) : null}
    </div>
  )
}

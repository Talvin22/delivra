import { useState, useEffect, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { X, Send, Paperclip, FileText, Download, Check, CheckCheck } from 'lucide-react'
import { chatApi } from '@/api/chat'
import { useWsStore } from '@/store/wsStore'
import { useAuthStore } from '@/store/authStore'
import { formatTime } from '@/lib/formatters'
import { cn } from '@/lib/utils'
import type { ChatMessageDTO } from '@/types/api'

interface Props {
  taskId: number
  onClose: () => void
  overlay?: boolean
}

const IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp']

function isImage(fileName: string | null): boolean {
  if (!fileName) return false
  const ext = fileName.split('.').pop()?.toLowerCase() ?? ''
  return IMAGE_EXTENSIONS.includes(ext)
}

function FileAttachment({ fileUrl, fileName }: { fileUrl: string; fileName: string }) {
  if (isImage(fileName)) {
    return (
      <a href={fileUrl} target="_blank" rel="noopener noreferrer">
        <img
          src={fileUrl}
          alt={fileName}
          className="max-w-[200px] max-h-[200px] rounded-lg object-cover border border-bg-border cursor-pointer hover:opacity-90 transition-opacity"
        />
      </a>
    )
  }

  return (
    <a
      href={fileUrl}
      download={fileName}
      className="flex items-center gap-2 px-3 py-2 rounded-lg bg-bg-base border border-bg-border hover:border-brand/40 transition-colors"
    >
      <FileText size={16} className="text-brand flex-shrink-0" />
      <span className="text-xs text-text-primary truncate max-w-[140px]">{fileName}</span>
      <Download size={13} className="text-text-muted flex-shrink-0" />
    </a>
  )
}

function ReadStatus({ isRead }: { isRead: boolean }) {
  return isRead
    ? <CheckCheck size={13} className="text-brand flex-shrink-0" />
    : <Check size={13} className="text-text-muted flex-shrink-0" />
}

export function ChatPanel({ taskId, onClose, overlay = false }: Props) {
  const user = useAuthStore(s => s.user)
  const { subscribe, publish, connected } = useWsStore()
  const [messages, setMessages] = useState<ChatMessageDTO[]>([])
  const [text, setText] = useState('')
  const [uploading, setUploading] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const { data: history } = useQuery({
    queryKey: ['chat', taskId],
    queryFn: () => chatApi.getHistory(taskId, 0, 50).then(r => r.data.payload.content),
  })

  useEffect(() => {
    if (history) setMessages(history)
  }, [history])

  // Mark messages as read when chat opens and subscribe to incoming messages
  useEffect(() => {
    chatApi.markRead(taskId)
  }, [taskId])

  // Subscribe to incoming chat messages
  useEffect(() => {
    const unsub = subscribe(`/topic/chat/${taskId}`, (data) => {
      setMessages(prev => {
        const incoming = data as ChatMessageDTO
        if (prev.some(m => m.id === incoming.id)) return prev
        return [...prev, incoming]
      })
      // Mark as read whenever a new message arrives while chat is open
      chatApi.markRead(taskId)
    })
    return unsub
  }, [taskId, subscribe])

  // Subscribe to read receipts — update isRead on our own messages
  // Only mark as read when the OTHER person reads (readByUserId !== me)
  useEffect(() => {
    const unsub = subscribe(`/topic/chat/${taskId}/read`, (data: any) => {
      if (data?.readByUserId === user?.id) return
      setMessages(prev =>
        prev.map(m =>
          m.senderId === user?.id ? { ...m, isRead: true } : m
        )
      )
    })
    return unsub
  }, [taskId, subscribe, user?.id])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const send = () => {
    const t = text.trim()
    if (!t) return
    publish(`/app/chat/${taskId}`, { messageText: t })
    setText('')
  }

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    e.target.value = ''

    setUploading(true)
    try {
      await chatApi.uploadFile(taskId, file)
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? 'Upload failed'
      alert(msg)
    } finally {
      setUploading(false)
    }
  }

  const inner = (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-bg-border flex-shrink-0">
        <span className="font-semibold text-brand text-sm">💬 Chat — Task #{taskId}</span>
        <button onClick={onClose} className="text-text-muted hover:text-text-primary p-1 rounded">
          <X size={18} />
        </button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-3 flex flex-col gap-2">
        {messages.length === 0 && (
          <p className="text-center text-text-muted text-xs mt-8">No messages</p>
        )}
        {messages.map(msg => {
          const mine = user?.id === msg.senderId
          return (
            <div key={msg.id} className={cn('flex', mine ? 'justify-end' : 'justify-start')}>
              <div className={cn(
                'max-w-[80%] rounded-xl px-3 py-2 text-sm',
                mine
                  ? 'bg-brand/20 border border-brand/30 text-text-primary rounded-br-sm'
                  : 'bg-bg-raised border border-bg-border text-text-primary rounded-bl-sm',
              )}>
                {!mine && <p className="text-xs text-text-muted mb-1">{msg.senderUsername}</p>}

                {msg.fileUrl && msg.fileName && (
                  <div className="mb-1">
                    <FileAttachment fileUrl={msg.fileUrl} fileName={msg.fileName} />
                  </div>
                )}

                {msg.messageText && (
                  <p className="leading-relaxed break-words">{msg.messageText}</p>
                )}

                <div className="flex items-center justify-end gap-1 mt-1">
                  <p className="text-[10px] text-text-muted">{formatTime(msg.created)}</p>
                  {mine && <ReadStatus isRead={msg.isRead} />}
                </div>
              </div>
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      {/* Connection status */}
      {!connected && (
        <div className="px-3 py-1.5 bg-warning/10 border-t border-warning/20 flex-shrink-0">
          <p className="text-xs text-warning text-center">No connection — messages unavailable</p>
        </div>
      )}

      {/* Input */}
      <div className="flex items-center gap-2 p-3 border-t border-bg-border flex-shrink-0">
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          accept=".jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.xls,.xlsx"
          onChange={handleFileChange}
        />
        <button
          onClick={() => fileInputRef.current?.click()}
          disabled={!connected || uploading}
          title="Attach file"
          className="w-9 h-9 rounded-full border border-bg-border flex items-center justify-center text-text-muted hover:text-brand hover:border-brand/40 disabled:opacity-40 transition-colors flex-shrink-0"
        >
          {uploading
            ? <div className="w-4 h-4 border-2 border-brand border-t-transparent rounded-full animate-spin" />
            : <Paperclip size={16} />
          }
        </button>

        <input
          className="flex-1 bg-bg-base border border-bg-border rounded-full px-4 py-2 text-sm text-text-primary placeholder:text-text-muted outline-none focus:border-brand transition-colors disabled:opacity-50"
          placeholder={connected ? 'Message...' : 'No connection...'}
          value={text}
          onChange={e => setText(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send()}
          disabled={!connected}
        />
        <button
          onClick={send}
          disabled={!text.trim() || !connected}
          className="w-9 h-9 rounded-full bg-brand flex items-center justify-center text-white disabled:opacity-40 transition-opacity flex-shrink-0"
        >
          <Send size={15} />
        </button>
      </div>
    </div>
  )

  if (!overlay) {
    return (
      <div className="fixed inset-y-0 right-0 w-full max-w-sm z-50 bg-bg-surface border-l border-bg-border shadow-2xl flex flex-col">
        {inner}
      </div>
    )
  }

  return (
    <>
      <div className="fixed inset-0 z-[600] bg-black/50" onClick={onClose} />
      <div className="fixed inset-y-0 right-0 w-full max-w-sm z-[700] bg-bg-surface border-l border-bg-border shadow-2xl flex flex-col">
        {inner}
      </div>
    </>
  )
}

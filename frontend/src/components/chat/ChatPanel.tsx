import { useState, useEffect, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { X, Send } from 'lucide-react'
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

export function ChatPanel({ taskId, onClose, overlay = false }: Props) {
  const user = useAuthStore(s => s.user)
  const { subscribe, publish, connected } = useWsStore()
  const [messages, setMessages] = useState<ChatMessageDTO[]>([])
  const [text, setText] = useState('')
  const bottomRef = useRef<HTMLDivElement>(null)

  const { data: history } = useQuery({
    queryKey: ['chat', taskId],
    queryFn: () => chatApi.getHistory(taskId, 0, 50).then(r => r.data.payload.content),
  })

  useEffect(() => {
    if (history) setMessages(history)
  }, [history])

  useEffect(() => {
    const unsub = subscribe(`/topic/chat/${taskId}`, (data) => {
      setMessages(prev => [...prev, data as ChatMessageDTO])
    })
    return unsub
  }, [taskId, subscribe])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const send = () => {
    const t = text.trim()
    if (!t) return
    publish(`/app/chat/${taskId}`, { messageText: t })
    setText('')
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
                <p className="leading-relaxed break-words">{msg.messageText}</p>
                <p className="text-[10px] text-text-muted mt-1 text-right">{formatTime(msg.created)}</p>
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
          className="w-9 h-9 rounded-full bg-brand flex items-center justify-center text-white disabled:opacity-40 transition-opacity"
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

import * as React from 'react'
import { Inbox, type LucideIcon } from 'lucide-react'
import { cn } from '../lib/cn'

export interface EmptyStateProps {
  /** Lucide icon component. Default: Inbox. */
  icon?:        LucideIcon
  title:        string
  description?: string
  action?:      React.ReactNode
  className?:   string
}

export function EmptyState({
  icon: Icon = Inbox,
  title,
  description,
  action,
  className,
}: EmptyStateProps) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed bg-white px-6 py-12 text-center',
        className,
      )}
    >
      <div className="rounded-full bg-gray-100 p-3 text-gray-500">
        <Icon className="h-6 w-6" />
      </div>
      <div className="space-y-1">
        <h3 className="text-sm font-semibold text-gray-900">{title}</h3>
        {description && (
          <p className="text-sm text-muted-foreground max-w-sm">{description}</p>
        )}
      </div>
      {action && <div className="mt-2">{action}</div>}
    </div>
  )
}

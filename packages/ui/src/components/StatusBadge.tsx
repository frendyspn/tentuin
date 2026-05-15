import * as React from 'react'
import { Badge, type BadgeProps } from '../primitives/badge'

export type StatusTone = 'success' | 'warning' | 'error' | 'gray' | 'default'

/**
 * Generic status badge dengan mapping `status string → tone`. Konsumen bisa
 * pass `toneMap` untuk override default mapping (mis. 'pending' → 'warning').
 *
 * Default mapping cocok untuk status pattern Tentuin (claim, withdrawal, follow-up).
 */
const DEFAULT_TONE_MAP: Record<string, StatusTone> = {
  // approval / withdrawal
  pending:   'warning',
  approved:  'success',
  rejected:  'error',
  paid:      'success',
  // claim
  active:    'success',
  expired:   'gray',
  // follow-up
  new:           'default',
  in_progress:   'warning',
  converted:     'success',
  released:      'gray',
  // generic
  success: 'success',
  error:   'error',
  warning: 'warning',
}

export interface StatusBadgeProps extends Omit<BadgeProps, 'variant'> {
  status:    string
  /** Override default mapping for specific status values. */
  toneMap?:  Record<string, StatusTone>
  /** Override label rendered inside the badge (defaults to humanized status). */
  label?:    string
}

export function StatusBadge({
  status,
  toneMap = DEFAULT_TONE_MAP,
  label,
  ...props
}: StatusBadgeProps) {
  const tone: StatusTone = toneMap[status.toLowerCase()] ?? 'gray'
  const display = label ?? humanize(status)
  return (
    <Badge variant={tone === 'default' ? 'default' : tone} {...props}>
      {display}
    </Badge>
  )
}

function humanize(s: string): string {
  return s
    .replace(/[_-]+/g, ' ')
    .replace(/\b\w/g, (c) => c.toUpperCase())
}

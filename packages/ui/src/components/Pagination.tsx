import * as React from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '../primitives/button'
import { cn } from '../lib/cn'

export interface PaginationProps {
  page:          number      // 1-based
  pageCount:     number
  onPageChange:  (page: number) => void
  totalRows?:    number
  className?:    string
}

/**
 * Standalone pagination control (paginate server-side data atau saat tidak
 * pakai DataTable's built-in pagination).
 */
export function Pagination({
  page,
  pageCount,
  onPageChange,
  totalRows,
  className,
}: PaginationProps) {
  const safePageCount = Math.max(1, pageCount)
  const canPrev = page > 1
  const canNext = page < safePageCount

  return (
    <div className={cn('flex items-center justify-between text-sm text-muted-foreground', className)}>
      <span>{typeof totalRows === 'number' ? `${totalRows} baris` : ''}</span>
      <div className="flex items-center gap-1">
        <Button
          variant="outline"
          size="icon"
          onClick={() => canPrev && onPageChange(page - 1)}
          disabled={!canPrev}
          className="h-8 w-8"
          aria-label="Halaman sebelumnya"
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <span className="px-2">
          {page} / {safePageCount}
        </span>
        <Button
          variant="outline"
          size="icon"
          onClick={() => canNext && onPageChange(page + 1)}
          disabled={!canNext}
          className="h-8 w-8"
          aria-label="Halaman selanjutnya"
        >
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}

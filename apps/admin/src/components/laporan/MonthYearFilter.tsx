'use client'

import { useRouter } from 'next/navigation'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { formatMonth } from '@/lib/format'

interface MonthYearFilterProps {
  currentMonth: number
  currentYear: number
}

const MONTHS = Array.from({ length: 12 }, (_, i) => i + 1)
const YEARS = Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i)

export function MonthYearFilter({ currentMonth, currentYear }: MonthYearFilterProps) {
  const router = useRouter()

  const update = (month: number, year: number) => {
    router.push(`/laporan?month=${month}&year=${year}`)
  }

  return (
    <div className="flex items-center gap-2">
      <Select
        value={String(currentMonth)}
        onValueChange={(v) => update(parseInt(v, 10), currentYear)}
      >
        <SelectTrigger className="w-32">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {MONTHS.map((m) => (
            <SelectItem key={m} value={String(m)}>
              {formatMonth(m)}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select
        value={String(currentYear)}
        onValueChange={(v) => update(currentMonth, parseInt(v, 10))}
      >
        <SelectTrigger className="w-24">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {YEARS.map((y) => (
            <SelectItem key={y} value={String(y)}>
              {y}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}

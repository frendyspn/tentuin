import { format, formatDistanceToNow } from 'date-fns'
import { id } from 'date-fns/locale'

/** Rp 1.234.567 */
export function formatRupiah(amount: number): string {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount)
}

/** "3 hari lalu" */
export function formatRelative(date: string | Date): string {
  return formatDistanceToNow(new Date(date), { addSuffix: true, locale: id })
}

/** "7 Mei 2025" */
export function formatDate(date: string | Date): string {
  return format(new Date(date), 'd MMMM yyyy', { locale: id })
}

/** "7 Mei 2025, 14:30" */
export function formatDateTime(date: string | Date): string {
  return format(new Date(date), "d MMMM yyyy, HH:mm", { locale: id })
}

/** "Januari", "Februari", dst */
export function formatMonth(month: number): string {
  const months = [
    'Januari', 'Februari', 'Maret', 'April', 'Mei', 'Juni',
    'Juli', 'Agustus', 'September', 'Oktober', 'November', 'Desember',
  ]
  return months[month - 1] ?? ''
}

/** "Jan 2025" (untuk label chart) */
export function formatMonthShort(month: number, year: number): string {
  const months = ['Jan','Feb','Mar','Apr','Mei','Jun','Jul','Ags','Sep','Okt','Nov','Des']
  return `${months[month - 1]} ${year}`
}

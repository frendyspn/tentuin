import { notFound } from 'next/navigation'
import { createAdminServerClient } from '@/lib/supabase/server'
import {
  getSchool,
  getSchoolTarget,
  getSchoolProgress,
} from '@tentuin/supabase'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { SchoolFormDialog } from '@/components/sekolah/SchoolFormDialog'
import { SchoolProgressChart } from '@/components/sekolah/SchoolProgressChart'
import { SchoolTargetForm } from '@/components/sekolah/SchoolTargetForm'
import { Badge } from '@/components/ui/badge'
import { ArrowLeft, User } from 'lucide-react'
import Link from 'next/link'
import { formatDate, formatMonth, formatMonthShort } from '@/lib/format'

export default async function SekolahDetailPage({ params }: { params: { id: string } }) {
  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()
  if (!session) notFound()

  const token = session.access_token
  const year = new Date().getFullYear()
  const currentMonth = new Date().getMonth() + 1

  const [school, target] = await Promise.all([
    getSchool(params.id).catch(() => null),
    getSchoolTarget(params.id, year, token).catch(() => null),
  ])

  if (!school) notFound()

  // Ambil klaim agen via Supabase langsung
  const { data: claimData } = await supabase
    .from('agent_school_claims')
    .select('agent_id, claimed_at, agents(full_name, referral_code)')
    .eq('school_id', params.id)
    .eq('is_active', true)
    .maybeSingle()

  // Bangun data chart 12 bulan (sampai bulan saat ini)
  const annualTarget = target?.annual_target ?? Math.ceil(school.total_students * 0.1)
  const chartData = Array.from({ length: currentMonth }, (_, i) => {
    const month = i + 1
    const monthlyTarget = target?.monthly_targets
      ? target.monthly_targets.slice(0, month).reduce((a: number, b: number) => a + b, 0)
      : Math.ceil((annualTarget * month) / 12)
    return {
      month: formatMonth(month).substring(0, 3),
      actual: 0, // Placeholder — idealnya dari prospect_usage_logs per bulan
      target: monthlyTarget,
    }
  })

  return (
    <div className="space-y-5 max-w-4xl">
      <Link href="/sekolah" className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800">
        <ArrowLeft className="h-4 w-4" />
        Kembali ke daftar sekolah
      </Link>

      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">{school.name}</h1>
          <p className="text-sm text-gray-500 mt-0.5">{school.city}, {school.province}</p>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant={school.is_active ? 'success' : 'gray'}>
            {school.is_active ? 'Aktif' : 'Nonaktif'}
          </Badge>
          <SchoolFormDialog school={school} />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Info Sekolah */}
        <Card>
          <CardHeader><CardTitle className="text-sm">Info Sekolah</CardTitle></CardHeader>
          <CardContent className="space-y-2.5 text-sm">
            <Row label="Jumlah Siswa" value={school.total_students.toLocaleString('id-ID')} />
            <Row label="Alamat" value={school.address ?? '—'} />
            <Row label="Email" value={school.email ?? '—'} />
            <Row label="Telepon" value={school.phone ?? '—'} />
          </CardContent>
        </Card>

        {/* Agen Klaim */}
        <Card>
          <CardHeader><CardTitle className="text-sm">Agen Klaim</CardTitle></CardHeader>
          <CardContent>
            {claimData ? (
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-full bg-[#EEEEFF] flex items-center justify-center">
                  <User className="h-4 w-4 text-[#5C59F8]" />
                </div>
                <div>
                  <p className="font-medium text-sm">{(claimData.agents as any)?.full_name}</p>
                  <p className="font-mono text-xs text-gray-400">{(claimData.agents as any)?.referral_code}</p>
                  <p className="text-xs text-gray-400 mt-0.5">Diklaim {formatDate(claimData.claimed_at)}</p>
                </div>
              </div>
            ) : (
              <p className="text-sm text-gray-400">Belum ada agen yang mengklaim sekolah ini.</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Progress Chart */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">
            Progress Pendaftaran {year}
            {' '}
            <span className="font-normal text-gray-400">
              (Target: {annualTarget.toLocaleString('id-ID')} siswa/tahun)
            </span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <SchoolProgressChart data={chartData} />
        </CardContent>
      </Card>

      {/* Target Form */}
      <Card>
        <CardHeader><CardTitle className="text-sm">Set Target {year}</CardTitle></CardHeader>
        <CardContent>
          <SchoolTargetForm
            schoolId={params.id}
            year={year}
            currentAnnualTarget={target?.annual_target}
          />
        </CardContent>
      </Card>
    </div>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <span className="text-gray-500">{label}</span>
      <span className="font-medium text-right">{value}</span>
    </div>
  )
}

import { notFound } from 'next/navigation'
import { createAdminServerClient } from '@/lib/supabase/server'
import { getUniversityWithMajors, getUniversitySubscribeLogs } from '@tentuin/supabase'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { RecordSubscribeDialog } from '@/components/universitas/RecordSubscribeDialog'
import { ArrowLeft, User } from 'lucide-react'
import Link from 'next/link'
import { formatDate, formatDateTime, formatRupiah } from '@/lib/format'

export default async function UniversitasDetailPage({ params }: { params: { id: string } }) {
  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()
  if (!session) notFound()

  const token = session.access_token

  const [university, subscribeLogs] = await Promise.all([
    getUniversityWithMajors(params.id).catch(() => null),
    getUniversitySubscribeLogs(params.id, token).catch(() => []),
  ])

  if (!university) notFound()

  // Ambil klaim agen
  const { data: claimData } = await supabase
    .from('agent_university_claims')
    .select('agent_id, claimed_at, agents(full_name, referral_code)')
    .eq('university_id', params.id)
    .eq('is_active', true)
    .maybeSingle()

  const claimingAgentId = claimData?.agent_id ?? null

  return (
    <div className="space-y-5 max-w-4xl">
      <Link href="/universitas" className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800">
        <ArrowLeft className="h-4 w-4" />
        Kembali ke daftar universitas
      </Link>

      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">{university.name}</h1>
          <p className="text-sm text-gray-500 mt-0.5">{university.city}, {university.province}</p>
        </div>
        <div className="flex items-center gap-2">
          {university.partner_tier && (
            <Badge variant={university.partner_tier === 'premium' ? 'default' : 'secondary'}>
              {university.partner_tier === 'premium' ? 'Premium' : 'Basic'}
            </Badge>
          )}
          <RecordSubscribeDialog
            universityId={params.id}
            universityName={university.name}
            agentId={claimingAgentId}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Info Kuota */}
        <Card>
          <CardHeader><CardTitle className="text-sm">Info Kuota</CardTitle></CardHeader>
          <CardContent className="space-y-2.5 text-sm">
            <Row label="Sisa Kuota" value={university.quota_balance.toLocaleString('id-ID')} />
            <Row label="Total Dibeli" value={university.total_quota_purchased.toLocaleString('id-ID')} />
            <Row label="PIC" value={university.pic_name ?? '—'} />
            <Row label="Telepon PIC" value={university.pic_phone ?? '—'} />
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
              <p className="text-sm text-gray-400">Belum ada agen yang mengklaim universitas ini.</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Log Subscribe */}
      <Card>
        <CardHeader><CardTitle className="text-sm">Riwayat Subscribe</CardTitle></CardHeader>
        <CardContent>
          {subscribeLogs.length === 0 ? (
            <p className="text-sm text-gray-400 py-4 text-center">Belum ada riwayat subscribe.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-gray-500 text-left">
                  <th className="pb-2 font-medium">Tanggal</th>
                  <th className="pb-2 font-medium">Nilai</th>
                  <th className="pb-2 font-medium">Kuota</th>
                  <th className="pb-2 font-medium">Komisi Agen</th>
                </tr>
              </thead>
              <tbody>
                {subscribeLogs.map((log) => (
                  <tr key={log.id} className="border-b last:border-0">
                    <td className="py-2.5 text-gray-500">{formatDateTime(log.subscribed_at)}</td>
                    <td className="py-2.5 font-medium">{formatRupiah(log.amount)}</td>
                    <td className="py-2.5">{log.quota_purchased.toLocaleString('id-ID')}</td>
                    <td className="py-2.5 text-[#5C59F8]">
                      {log.commission_agent > 0 ? formatRupiah(log.commission_agent) : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
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

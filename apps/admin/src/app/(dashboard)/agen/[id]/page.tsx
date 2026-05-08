import { notFound } from 'next/navigation'
import { createAdminServerClient } from '@/lib/supabase/server'
import {
  getAgentProfile,
  getAgentSchoolClaims,
  getAgentUniversityClaims,
  getAgentCommissions,
} from '@tentuin/supabase'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { AgentStatusBadge } from '@/components/agen/AgentStatusBadge'
import { Badge } from '@/components/ui/badge'
import { formatDate, formatRupiah, formatMonth } from '@/lib/format'
import { ArrowLeft, Building2, School, Wallet } from 'lucide-react'
import Link from 'next/link'

export default async function AgenDetailPage({ params }: { params: { id: string } }) {
  const supabase = await createAdminServerClient()
  const {
    data: { session },
  } = await supabase.auth.getSession()

  if (!session) notFound()

  const token = session.access_token
  const year = new Date().getFullYear()

  const [agent, schoolClaims, uniClaims, commissions] = await Promise.all([
    getAgentProfile(params.id, token).catch(() => null),
    getAgentSchoolClaims(params.id, token).catch(() => []),
    getAgentUniversityClaims(params.id, token).catch(() => []),
    getAgentCommissions(params.id, year, token).catch(() => []),
  ])

  if (!agent) notFound()

  const totalComm = commissions.reduce((s, c) => s + c.total_amount, 0)

  return (
    <div className="space-y-5 max-w-4xl">
      {/* Back */}
      <Link href="/agen" className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800">
        <ArrowLeft className="h-4 w-4" />
        Kembali ke daftar agen
      </Link>

      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">{agent.full_name}</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            <span className="font-mono bg-gray-100 px-1.5 py-0.5 rounded text-xs">{agent.referral_code}</span>
            {' · '}
            <span>{agent.email}</span>
          </p>
        </div>
        <AgentStatusBadge status={agent.status} />
      </div>

      <Tabs defaultValue="profil">
        <TabsList>
          <TabsTrigger value="profil">Profil</TabsTrigger>
          <TabsTrigger value="klaim">
            Klaim ({schoolClaims.length + uniClaims.length})
          </TabsTrigger>
          <TabsTrigger value="komisi">Komisi {year}</TabsTrigger>
        </TabsList>

        {/* Tab Profil */}
        <TabsContent value="profil">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
            <Card>
              <CardHeader><CardTitle className="text-sm">Info Pribadi</CardTitle></CardHeader>
              <CardContent className="space-y-3 text-sm">
                <Row label="Nama" value={agent.full_name} />
                <Row label="Email" value={agent.email} />
                <Row label="Telepon" value={agent.phone ?? '—'} />
                <Row label="Bergabung" value={formatDate(agent.created_at)} />
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="text-sm">Info Bank</CardTitle></CardHeader>
              <CardContent className="space-y-3 text-sm">
                <Row label="Bank" value={agent.bank_name ?? '—'} />
                <Row label="No. Rekening" value={agent.bank_account_number ?? '—'} />
                <Row label="Atas Nama" value={agent.bank_account_name ?? '—'} />
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Tab Klaim */}
        <TabsContent value="klaim">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
            <Card>
              <CardHeader>
                <CardTitle className="text-sm flex items-center gap-2">
                  <School className="h-4 w-4" /> Sekolah ({schoolClaims.length})
                </CardTitle>
              </CardHeader>
              <CardContent>
                {schoolClaims.length === 0 ? (
                  <p className="text-sm text-gray-400">Belum ada klaim sekolah.</p>
                ) : (
                  <ul className="space-y-2">
                    {schoolClaims.map((c) => (
                      <li key={c.id} className="text-sm flex items-center justify-between">
                        <span className="font-medium">{c.school?.name ?? c.school_id}</span>
                        <span className="text-xs text-gray-400">{formatDate(c.claimed_at)}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-sm flex items-center gap-2">
                  <Building2 className="h-4 w-4" /> Universitas ({uniClaims.length})
                </CardTitle>
              </CardHeader>
              <CardContent>
                {uniClaims.length === 0 ? (
                  <p className="text-sm text-gray-400">Belum ada klaim universitas.</p>
                ) : (
                  <ul className="space-y-2">
                    {uniClaims.map((c) => (
                      <li key={c.id} className="text-sm flex items-center justify-between">
                        <span className="font-medium">{c.university?.name ?? c.university_id}</span>
                        <span className="text-xs text-gray-400">{formatDate(c.claimed_at)}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Tab Komisi */}
        <TabsContent value="komisi">
          <div className="mt-2 space-y-4">
            {/* Summary */}
            <div className="grid grid-cols-3 gap-3">
              <StatCard
                label="Total Komisi"
                value={formatRupiah(totalComm)}
                icon={<Wallet className="h-4 w-4 text-[#5C59F8]" />}
              />
              <StatCard
                label="Stream A"
                value={formatRupiah(commissions.reduce((s, c) => s + c.stream_a_amount, 0))}
              />
              <StatCard
                label="Stream B"
                value={formatRupiah(commissions.reduce((s, c) => s + c.stream_b_amount, 0))}
              />
            </div>

            {/* Monthly breakdown */}
            <Card>
              <CardContent className="pt-5">
                {commissions.length === 0 ? (
                  <p className="text-sm text-gray-400 text-center py-6">
                    Belum ada komisi di tahun {year}.
                  </p>
                ) : (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b text-gray-500 text-left">
                        <th className="pb-2 font-medium">Bulan</th>
                        <th className="pb-2 font-medium">Stream A</th>
                        <th className="pb-2 font-medium">Stream B</th>
                        <th className="pb-2 font-medium">Total</th>
                        <th className="pb-2 font-medium">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {commissions.map((c) => (
                        <tr key={c.id} className="border-b last:border-0">
                          <td className="py-2.5 font-medium">{formatMonth(c.month)}</td>
                          <td className="py-2.5 text-gray-600">{formatRupiah(c.stream_a_amount)}</td>
                          <td className="py-2.5 text-gray-600">{formatRupiah(c.stream_b_amount)}</td>
                          <td className="py-2.5 font-semibold">{formatRupiah(c.total_amount)}</td>
                          <td className="py-2.5">
                            <CommissionStatusBadge status={c.status} />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
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

function StatCard({ label, value, icon }: { label: string; value: string; icon?: React.ReactNode }) {
  return (
    <Card>
      <CardContent className="pt-4 pb-4">
        <div className="flex items-center gap-2 mb-1">
          {icon}
          <span className="text-xs text-gray-500">{label}</span>
        </div>
        <p className="font-bold text-gray-900">{value}</p>
      </CardContent>
    </Card>
  )
}

function CommissionStatusBadge({ status }: { status: string }) {
  const map: Record<string, { label: string; variant: 'warning' | 'success' | 'gray' }> = {
    pending: { label: 'Pending', variant: 'warning' },
    paid: { label: 'Dibayar', variant: 'success' },
    cancelled: { label: 'Dibatalkan', variant: 'gray' },
  }
  const { label, variant } = map[status] ?? { label: status, variant: 'gray' as const }
  return <Badge variant={variant}>{label}</Badge>
}

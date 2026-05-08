import { createAdminServerClient } from '@/lib/supabase/server'
import { getAllWithdrawals } from '@tentuin/supabase'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { WithdrawalStatusBadge } from '@/components/withdrawal/WithdrawalStatusBadge'
import { WithdrawalActionDialog } from '@/components/withdrawal/WithdrawalActionDialog'
import { Card, CardContent } from '@/components/ui/card'
import { formatRupiah, formatDateTime } from '@/lib/format'

export default async function WithdrawalPage() {
  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()

  const [pending, all] = await Promise.all([
    session
      ? getAllWithdrawals(session.access_token, 'requested').catch(() => [])
      : [],
    session
      ? getAllWithdrawals(session.access_token).catch(() => [])
      : [],
  ])

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Approval Withdrawal</h1>
        <p className="text-sm text-gray-500 mt-1">
          {pending.length} permintaan menunggu persetujuan
        </p>
      </div>

      <Tabs defaultValue="pending">
        <TabsList>
          <TabsTrigger value="pending">
            Menunggu ({pending.length})
          </TabsTrigger>
          <TabsTrigger value="history">Riwayat ({all.length})</TabsTrigger>
        </TabsList>

        {/* Tab Pending */}
        <TabsContent value="pending">
          <Card className="mt-2">
            <CardContent className="pt-4">
              {pending.length === 0 ? (
                <p className="text-sm text-gray-400 text-center py-8">
                  Tidak ada permintaan withdrawal yang menunggu.
                </p>
              ) : (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-gray-500 text-left">
                      <th className="pb-2 font-medium">Agen</th>
                      <th className="pb-2 font-medium">Jumlah</th>
                      <th className="pb-2 font-medium">Bank</th>
                      <th className="pb-2 font-medium">Tanggal Request</th>
                      <th className="pb-2 font-medium">Aksi</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pending.map((w) => (
                      <tr key={w.id} className="border-b last:border-0">
                        <td className="py-3">
                          <p className="font-medium">{w.agent.full_name}</p>
                          <p className="font-mono text-xs text-gray-400">{w.agent.referral_code}</p>
                        </td>
                        <td className="py-3 font-bold text-gray-900">{formatRupiah(w.amount)}</td>
                        <td className="py-3 text-gray-600">
                          <p>{w.agent.bank_name ?? '—'}</p>
                          <p className="font-mono text-xs">{w.agent.bank_account_number ?? '—'}</p>
                          <p className="text-xs text-gray-400">{w.agent.bank_account_name ?? '—'}</p>
                        </td>
                        <td className="py-3 text-gray-500">{formatDateTime(w.requested_at)}</td>
                        <td className="py-3">
                          <WithdrawalActionDialog
                            withdrawalId={w.id}
                            agentName={w.agent.full_name}
                            amount={w.amount}
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Tab History */}
        <TabsContent value="history">
          <Card className="mt-2">
            <CardContent className="pt-4">
              {all.length === 0 ? (
                <p className="text-sm text-gray-400 text-center py-8">Belum ada riwayat withdrawal.</p>
              ) : (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-gray-500 text-left">
                      <th className="pb-2 font-medium">Agen</th>
                      <th className="pb-2 font-medium">Jumlah</th>
                      <th className="pb-2 font-medium">Status</th>
                      <th className="pb-2 font-medium">Tanggal Request</th>
                      <th className="pb-2 font-medium">Diproses</th>
                    </tr>
                  </thead>
                  <tbody>
                    {all.map((w) => (
                      <tr key={w.id} className="border-b last:border-0">
                        <td className="py-2.5">
                          <p className="font-medium">{w.agent.full_name}</p>
                          <p className="font-mono text-xs text-gray-400">{w.agent.referral_code}</p>
                        </td>
                        <td className="py-2.5 font-medium">{formatRupiah(w.amount)}</td>
                        <td className="py-2.5">
                          <WithdrawalStatusBadge status={w.status as any} />
                        </td>
                        <td className="py-2.5 text-gray-500">{formatDateTime(w.requested_at)}</td>
                        <td className="py-2.5 text-gray-400">
                          {w.processed_at ? formatDateTime(w.processed_at) : '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}

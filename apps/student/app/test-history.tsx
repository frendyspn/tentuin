import React, { useCallback, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useFocusEffect, useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { getTestHistory, type RiasecScores } from '@tentuin/supabase'
import { useAuthStore } from '../stores/authStore'
import { hapticLight } from '../utils/haptics'

// ─── Types & constants ────────────────────────────────────────────────────────

interface TestResult {
  id:           string
  user_id:      string
  scores:       RiasecScores
  riasec_code:  string
  completed_at: string
}

const CODE_COLOR: Record<string, string> = {
  R: '#F97316', I: '#3B82F6', A: '#EC4899',
  S: '#10B981', E: '#F59E0B', C: '#5C59F8',
}

const CODE_NAME: Record<string, string> = {
  R: 'Realistic', I: 'Investigative', A: 'Artistic',
  S: 'Social',    E: 'Enterprising',  C: 'Conventional',
}

const CAT_TO_CODE: Record<string, string> = {
  realistic: 'R', investigative: 'I', artistic: 'A',
  social:    'S', enterprising:  'E', conventional: 'C',
}

// ─── Header component (shared) ───────────────────────────────────────────────

function ScreenHeader({ onBack }: { onBack: () => void }) {
  return (
    <View style={styles.header}>
      <Pressable onPress={onBack} hitSlop={12} style={styles.backBtn}>
        <Ionicons name="arrow-back" size={22} color={colors.text} />
      </Pressable>
      <Text style={styles.title}>Riwayat Test</Text>
      <View style={{ width: 36 }} />
    </View>
  )
}

// ─── Result card ─────────────────────────────────────────────────────────────

function ResultCard({ item, onPress }: { item: TestResult; onPress: () => void }) {
  // Ambil top-3 skor untuk ditampilkan di card
  const topScores = (Object.entries(item.scores) as [keyof RiasecScores, number][])
    .sort((a, b) => b[1] - a[1])
    .slice(0, 3)

  const dominantCode = item.riasec_code[0]
  const dominantName = CODE_NAME[dominantCode] ?? dominantCode

  const dateStr = new Date(item.completed_at).toLocaleDateString('id-ID', {
    day: 'numeric', month: 'long', year: 'numeric',
  })

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [styles.card, pressed && { opacity: 0.85, transform: [{ scale: 0.99 }] }]}
    >
      {/* Top row: kode + tanggal */}
      <View style={styles.cardTop}>
        <View style={styles.codeRow}>
          {item.riasec_code.split('').map((ch, i) => (
            <View
              key={i}
              style={[styles.codeChip, { backgroundColor: CODE_COLOR[ch] + '20', borderColor: CODE_COLOR[ch] + '60' }]}
            >
              <Text style={[styles.codeText, { color: CODE_COLOR[ch] }]}>{ch}</Text>
            </View>
          ))}
        </View>
        <Text style={styles.dateText}>{dateStr}</Text>
      </View>

      {/* Tipe dominan */}
      <Text style={styles.dominantLabel}>
        Tipe dominan:{' '}
        <Text style={[styles.dominantValue, { color: CODE_COLOR[dominantCode] }]}>
          {dominantName}
        </Text>
      </Text>

      {/* Top-3 score bars */}
      <View style={styles.barsWrap}>
        {topScores.map(([cat, score]) => {
          const code = CAT_TO_CODE[cat]
          return (
            <View key={cat} style={styles.barRow}>
              <Text style={[styles.barCode, { color: CODE_COLOR[code] }]}>{code}</Text>
              <View style={styles.barTrack}>
                <View
                  style={[styles.barFill, { width: `${score}%`, backgroundColor: CODE_COLOR[code] }]}
                />
              </View>
              <Text style={styles.barNum}>{score}%</Text>
            </View>
          )
        })}
      </View>

      {/* Affordance */}
      <View style={styles.cardFooter}>
        <Text style={styles.detailHint}>Lihat hasil lengkap</Text>
        <Ionicons name="chevron-forward" size={14} color={colors.textMuted} />
      </View>
    </Pressable>
  )
}

// ─── Main screen ─────────────────────────────────────────────────────────────

export default function TestHistoryScreen() {
  const router  = useRouter()
  const session = useAuthStore((s) => s.session)

  const [history, setHistory]   = useState<TestResult[]>([])
  const [loading, setLoading]   = useState(true)
  const [error,   setError]     = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!session?.user?.id || !session?.access_token) {
      setError('Sesi tidak ditemukan. Coba login ulang.')
      setLoading(false)
      return
    }
    setLoading(true)
    setError(null)
    try {
      const data = await getTestHistory(session.user.id, session.access_token)
      setHistory(data ?? [])
    } catch (err: any) {
      setError(err?.message ?? 'Gagal memuat riwayat')
    } finally {
      setLoading(false)
    }
  }, [session?.user?.id, session?.access_token])

  useFocusEffect(useCallback(() => { load() }, [load]))

  const goBack = () => router.back()

  // ── Loading ───────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <ScreenHeader onBack={goBack} />
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.primary} />
          <Text style={styles.loadingText}>Memuat riwayat…</Text>
        </View>
      </SafeAreaView>
    )
  }

  // ── Error ─────────────────────────────────────────────────────────────────
  if (error) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <ScreenHeader onBack={goBack} />
        <View style={styles.centered}>
          <View style={[styles.stateIcon, { backgroundColor: colors.errorLight }]}>
            <Ionicons name="alert-circle-outline" size={40} color={colors.error} />
          </View>
          <Text style={styles.stateTitle}>Gagal Memuat</Text>
          <Text style={styles.stateDesc}>{error}</Text>
          <Pressable style={styles.actionBtn} onPress={load}>
            <Ionicons name="refresh-outline" size={16} color={colors.white} />
            <Text style={styles.actionBtnText}>Coba Lagi</Text>
          </Pressable>
        </View>
      </SafeAreaView>
    )
  }

  // ── Empty ─────────────────────────────────────────────────────────────────
  if (history.length === 0) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <ScreenHeader onBack={goBack} />
        <View style={styles.centered}>
          <View style={[styles.stateIcon, { backgroundColor: colors.primaryLight }]}>
            <Ionicons name="clipboard-outline" size={40} color={colors.primary} />
          </View>
          <Text style={styles.stateTitle}>Belum Ada Riwayat</Text>
          <Text style={styles.stateDesc}>
            Selesaikan test RIASEC pertamamu untuk melihat hasil dan rekomendasinya di sini.
          </Text>
          <Pressable
            style={styles.actionBtn}
            onPress={() => { hapticLight(); router.replace('/(tabs)/test') }}
          >
            <Ionicons name="play-outline" size={16} color={colors.white} />
            <Text style={styles.actionBtnText}>Mulai Test Sekarang</Text>
          </Pressable>
        </View>
      </SafeAreaView>
    )
  }

  // ── List ──────────────────────────────────────────────────────────────────
  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <ScreenHeader onBack={goBack} />
      <FlatList
        data={history}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        ListHeaderComponent={
          <Text style={styles.listCount}>{history.length} hasil test tersimpan</Text>
        }
        renderItem={({ item }) => (
          <ResultCard
            item={item}
            onPress={() => {
              hapticLight()
              router.push({
                pathname: '/test-result',
                params: {
                  scores:      JSON.stringify(item.scores),
                  riasecCode:  item.riasec_code,
                  isHistorical: 'true',
                },
              })
            }}
          />
        )}
      />
    </SafeAreaView>
  )
}

// ─── Styles ───────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },

  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 20, paddingTop: 4, paddingBottom: 12,
    borderBottomWidth: 1, borderBottomColor: colors.border,
  },
  backBtn: {
    width: 36, height: 36, borderRadius: 10,
    backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center',
  },
  title: { fontSize: 20, fontFamily: fonts.bold, color: colors.text },

  centered: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32, gap: 12 },
  loadingText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub },

  stateIcon: {
    width: 80, height: 80, borderRadius: 20,
    alignItems: 'center', justifyContent: 'center', marginBottom: 4,
  },
  stateTitle: { fontSize: 18, fontFamily: fonts.bold, color: colors.text },
  stateDesc: {
    fontSize: 14, fontFamily: fonts.regular, color: colors.textSub,
    textAlign: 'center', lineHeight: 22,
  },
  actionBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    paddingHorizontal: 24, paddingVertical: 12,
    backgroundColor: colors.primary, borderRadius: 12, marginTop: 4,
  },
  actionBtnText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.white },

  listContent: { padding: 20, paddingBottom: 40, gap: 12 },
  listCount: {
    fontSize: 13, fontFamily: fonts.medium,
    color: colors.textMuted, marginBottom: 4,
  },

  // Card
  card: {
    backgroundColor: colors.surface,
    borderRadius: 16, padding: 16, gap: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
  },
  cardTop: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  codeRow: { flexDirection: 'row', gap: 6 },
  codeChip: {
    width: 36, height: 36, borderRadius: 9,
    borderWidth: 1.5, alignItems: 'center', justifyContent: 'center',
  },
  codeText: { fontSize: 14, fontFamily: fonts.bold },
  dateText: { fontSize: 12, fontFamily: fonts.medium, color: colors.textMuted },

  dominantLabel: { fontSize: 13, fontFamily: fonts.regular, color: colors.textSub },
  dominantValue: { fontFamily: fonts.bold },

  barsWrap: { gap: 6 },
  barRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  barCode: { fontSize: 11, fontFamily: fonts.bold, width: 14 },
  barTrack: {
    flex: 1, height: 6, backgroundColor: colors.border,
    borderRadius: 3, overflow: 'hidden',
  },
  barFill: { height: '100%', borderRadius: 3 },
  barNum: {
    fontSize: 11, fontFamily: fonts.semiBold,
    color: colors.textMuted, width: 32, textAlign: 'right',
  },

  cardFooter: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'flex-end',
    gap: 2, marginTop: 2,
  },
  detailHint: { fontSize: 12, fontFamily: fonts.medium, color: colors.textMuted },
})

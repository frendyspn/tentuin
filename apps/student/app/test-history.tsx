import React, { useEffect, useState } from 'react'
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, View } from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { getTestHistory, type RiasecScores } from '@tentuin/supabase'
import { useAuthStore } from '../stores/authStore'

interface TestResult {
  id: string
  user_id: string
  scores: RiasecScores
  riasec_code: string
  completed_at: string
}

const CATEGORY_COLORS: Record<string, string> = {
  R: '#F97316', I: '#3B82F6', A: '#EC4899', S: '#10B981', E: '#F59E0B', C: '#5C59F8',
}

const CATEGORY_NAMES: Record<string, string> = {
  R: 'Realistic', I: 'Investigative', A: 'Artistic', S: 'Social', E: 'Enterprising', C: 'Conventional',
}

export default function TestHistoryScreen() {
  const router = useRouter()
  const session = useAuthStore((s) => s.session)
  const [history, setHistory] = useState<TestResult[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!session?.user?.id || !session?.access_token) {
      setError('Session tidak ditemukan')
      setLoading(false)
      return
    }
    
    setLoading(true)
    setError(null)
    console.log('[TestHistory] Fetching with token for user:', session.user.id)
    getTestHistory(session.user.id, session.access_token)
      .then((data) => {
        console.log('[TestHistory] Loaded', data?.length ?? 0, 'results')
        setHistory(data ?? [])
      })
      .catch((err) => {
        console.error('[TestHistory] Error loading history:', err?.message)
        setError(err?.message ?? 'Gagal memuat riwayat test')
      })
      .finally(() => setLoading(false))
  }, [session?.user?.id, session?.access_token])

  if (loading) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <View style={styles.loadingWrap}>
          <ActivityIndicator size="large" color={colors.primary} />
          <Text style={styles.loadingText}>Memuat riwayat...</Text>
        </View>
      </SafeAreaView>
    )
  }

  if (error) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <View style={styles.header}>
          <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
            <Ionicons name="arrow-back" size={22} color={colors.text} />
          </Pressable>
          <Text style={styles.title}>Riwayat Test</Text>
          <View style={{ width: 36 }} />
        </View>

        <View style={styles.emptyWrap}>
          <View style={styles.errorIcon}>
            <Ionicons name="alert-circle-outline" size={48} color={colors.error} />
          </View>
          <Text style={styles.errorTitle}>Gagal Memuat</Text>
          <Text style={styles.errorDesc}>{error}</Text>
          <Pressable 
            style={styles.retryBtn}
            onPress={() => {
              if (!session?.access_token) {
                setError('Session tidak valid')
                return
              }
              setError(null)
              setLoading(true)
              getTestHistory(session.user?.id ?? '', session.access_token)
                .then((data) => {
                  console.log('[TestHistory] Loaded', data?.length ?? 0, 'results')
                  setHistory(data ?? [])
                })
                .catch((err) => {
                  console.error('[TestHistory] Error loading history:', err?.message)
                  setError(err?.message ?? 'Gagal memuat riwayat test')
                })
                .finally(() => setLoading(false))
            }}
          >
            <Ionicons name="refresh-outline" size={16} color={colors.white} />
            <Text style={styles.retryBtnText}>Coba Lagi</Text>
          </Pressable>
        </View>
      </SafeAreaView>
    )
  }

  if (history.length === 0) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <View style={styles.header}>
          <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
            <Ionicons name="arrow-back" size={22} color={colors.text} />
          </Pressable>
          <Text style={styles.title}>Riwayat Test</Text>
          <View style={{ width: 36 }} />
        </View>

        <View style={styles.emptyWrap}>
          <View style={styles.emptyIcon}>
            <Ionicons name="document-text-outline" size={48} color={colors.primary} />
          </View>
          <Text style={styles.emptyTitle}>Belum Ada Riwayat</Text>
          <Text style={styles.emptyDesc}>Mulai test RIASEC sekarang untuk melihat hasil dan prognosis karirmu</Text>
          <Pressable 
            style={styles.startBtn}
            onPress={() => router.replace('/(tabs)/test')}
          >
            <Ionicons name="play-outline" size={16} color={colors.white} />
            <Text style={styles.startBtnText}>Mulai Test Sekarang</Text>
          </Pressable>
        </View>
      </SafeAreaView>
    )
  }

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={22} color={colors.text} />
        </Pressable>
        <Text style={styles.title}>Riwayat Test</Text>
        <View style={{ width: 36 }} />
      </View>

      <FlatList
        data={history}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        renderItem={({ item }) => (
          <Pressable 
            onPress={() => router.push({
              pathname: '/test-result',
              params: {
                scores: JSON.stringify(item.scores),
                riasecCode: item.riasec_code,
                isHistorical: 'true',
              }
            })}
            style={({ pressed }) => [styles.cardPress, pressed && styles.cardPressed]}
          >
            <View style={styles.card}>
              <View style={styles.cardHeader}>
                <View style={styles.codeWrap}>
                  {item.riasec_code.split('').map((code, i) => (
                    <View
                      key={i}
                      style={[
                        styles.codeBox,
                        { backgroundColor: CATEGORY_COLORS[code] + '22', borderColor: CATEGORY_COLORS[code] },
                      ]}
                    >
                      <Text style={[styles.code, { color: CATEGORY_COLORS[code] }]}>{code}</Text>
                    </View>
                  ))}
                </View>
                <Text style={styles.date}>
                  {new Date(item.completed_at).toLocaleDateString('id-ID', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric',
                  })}
                </Text>
              </View>

              <View style={styles.scoresWrap}>
                {(Object.entries(item.scores) as [keyof RiasecScores, number][]).map(([category, score]) => {
                  const code = category.charAt(0).toUpperCase()
                  return (
                    <View key={category} style={styles.scoreRow}>
                      <View style={styles.scoreContent}>
                        <View style={styles.scoreBarWithNum}>
                          <View style={styles.scoreBar}>
                            <View
                              style={[
                                styles.scoreFill,
                                { width: `${score}%`, backgroundColor: CATEGORY_COLORS[code] },
                              ]}
                            />
                          </View>
                          <Text style={styles.scoreNum}>{score}%</Text>
                        </View>
                        <Text style={[styles.categoryName, { color: CATEGORY_COLORS[code] }]}>
                          {CATEGORY_NAMES[code]}
                        </Text>
                      </View>
                    </View>
                  )
                })}
              </View>
            </View>
          </Pressable>
        )}
        showsVerticalScrollIndicator={false}
        scrollEnabled
      />
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 4,
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  backBtn: { width: 36, height: 36, borderRadius: 10, backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: 20, fontFamily: fonts.bold, color: colors.text },

  loadingWrap: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12 },
  loadingText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub },

  emptyWrap: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 24, gap: 12 },
  emptyIcon: { width: 80, height: 80, borderRadius: 20, backgroundColor: colors.primaryLight, alignItems: 'center', justifyContent: 'center', marginBottom: 8 },
  emptyTitle: { fontSize: 18, fontFamily: fonts.bold, color: colors.text },
  emptyDesc: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub, textAlign: 'center', lineHeight: 20 },
  startBtn: { flexDirection: 'row', alignItems: 'center', gap: 8, paddingHorizontal: 24, paddingVertical: 12, backgroundColor: colors.primary, borderRadius: 12, marginTop: 8 },
  startBtnText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.white },

  errorIcon: { width: 80, height: 80, borderRadius: 20, backgroundColor: colors.errorLight, alignItems: 'center', justifyContent: 'center', marginBottom: 8 },
  errorTitle: { fontSize: 18, fontFamily: fonts.bold, color: colors.text },
  errorDesc: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub, textAlign: 'center', lineHeight: 20 },
  retryBtn: { flexDirection: 'row', alignItems: 'center', gap: 8, paddingHorizontal: 24, paddingVertical: 12, backgroundColor: colors.error, borderRadius: 12, marginTop: 8 },
  retryBtnText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.white },

  listContent: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 24, gap: 12 },

  cardPress: { borderRadius: 16 },
  cardPressed: { opacity: 0.7 },
  card: { backgroundColor: colors.surface, borderRadius: 16, padding: 16, gap: 12, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 8, elevation: 2 },
  cardHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  codeWrap: { flexDirection: 'row', gap: 6 },
  codeBox: { width: 40, height: 40, borderRadius: 10, borderWidth: 2, alignItems: 'center', justifyContent: 'center' },
  code: { fontSize: 16, fontFamily: fonts.bold },
  date: { fontSize: 12, fontFamily: fonts.medium, color: colors.textMuted },

  scoresWrap: { gap: 6 },
  scoreRow: { flexDirection: 'row', alignItems: 'flex-start' },
  scoreContent: { flex: 1, gap: 1 },
  scoreBarWithNum: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  scoreBar: { flex: 1, height: 6, backgroundColor: colors.border, borderRadius: 3, overflow: 'hidden' },
  scoreFill: { height: '100%', borderRadius: 3 },
  scoreNum: { fontSize: 12, fontFamily: fonts.bold, color: colors.text, minWidth: 36, textAlign: 'right' },
  categoryName: { fontSize: 11, fontFamily: fonts.semiBold },
})

import React, { useEffect, useState } from 'react'
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { getPartnerUniversities, getTestHistory, type UniversityRow } from '@tentuin/supabase'
import { useAuthStore } from '../../../stores/authStore'
import { useRequireAuth } from '../../../hooks/useRequireAuth'
import { AuthPromptSheet } from '../../../components/auth/AuthPromptSheet'

// ─── Static content ───────────────────────────────────────────────────────────

const ARTICLES = [
  { icon: 'sparkles-outline' as const,  category: 'Psikologi', title: 'Gimana Rasanya Jadi Mahasiswa Psikologi?',          time: '3 min', bg: '#EEF2FF' },
  { icon: 'construct-outline' as const, category: 'Teknik',    title: 'Jurusan Teknik: Berat Tapi Penuh Peluang',           time: '4 min', bg: '#FFF0F6' },
  { icon: 'bulb-outline' as const,      category: 'Tips',      title: '5 Hal yang Harus Kamu Tahu Sebelum Milih Jurusan',   time: '5 min', bg: '#FFFBEB' },
]



// ─── Main Screen ─────────────────────────────────────────────────────────────

export default function HomeScreen() {
  const router = useRouter()
  const { profile, user, session } = useAuthStore()
  const { requireAuth, showPrompt, closePrompt } = useRequireAuth()

  const [featuredUnis, setFeaturedUnis] = useState<UniversityRow[]>([])
  const [loadingUnis, setLoadingUnis] = useState(true)

  const [lastTest, setLastTest] = useState<any | null>(null)

  const firstName = profile?.full_name?.split(' ')[0] ?? (user ? 'Kamu' : 'Sobat')
  const isLoggedIn = !!session?.user

  // Load featured (partner) universities
  useEffect(() => {
    getPartnerUniversities()
      .then(setFeaturedUnis)
      .catch(console.error)
      .finally(() => setLoadingUnis(false))
  }, [])

  // Load last test result for logged-in users
  useEffect(() => {
    if (!session?.user?.id || !session?.access_token) return
    getTestHistory(session.user.id, session.access_token)
      .then((data) => setLastTest(data?.[0] ?? null))
      .catch(console.error)
  }, [session?.user?.id])

  const handleStartTest = () => requireAuth(() => router.push('/(tabs)/test'))

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >

        {/* ── Header ── */}
        <View style={styles.header}>
          <View>
            <Text style={styles.greeting}>Hei, {firstName} 👋</Text>
            <Text style={styles.subGreeting}>
              {lastTest ? 'Kamu sudah kenal dirimu!' : 'Udah tentuin jurusan belum?'}
            </Text>
          </View>
          <Pressable
            style={styles.avatarBtn}
            onPress={() => router.push('/(tabs)/profile')}
          >
            <Text style={styles.avatarText}>
              {profile?.full_name?.[0]?.toUpperCase() ?? user?.email?.[0]?.toUpperCase() ?? '?'}
            </Text>
          </Pressable>
        </View>

        {/* ── Hero Card ── */}
        <Pressable
          style={({ pressed }) => [styles.heroCard, pressed && { opacity: 0.93 }]}
          onPress={lastTest
            ? () => router.push(`/test-result?riasecCode=${lastTest.riasec_code}&scores=${JSON.stringify(lastTest.scores)}&isHistorical=true`)
            : handleStartTest
          }
        >
          <View style={styles.heroContent}>
            <View style={styles.heroBadge}>
              <Ionicons name={lastTest ? 'checkmark-circle' : 'star'} size={10} color={colors.white} />
              <Text style={styles.heroBadgeText}>{lastTest ? 'Test Selesai' : 'Test Gratis'}</Text>
            </View>
            <Text style={styles.heroTitle}>
              {lastTest ? `Tipe: ${lastTest.riasec_code}` : 'Kenali Kepribadianmu'}
            </Text>
            <Text style={styles.heroSub}>
              {lastTest
                ? `Tipe ${lastTest.riasec_code} · Tap untuk lihat detail`
                : '15 menit · 60 soal · Hasil akurat'
              }
            </Text>
            <View style={styles.heroBtn}>
              <Text style={styles.heroBtnText}>
                {lastTest ? 'Lihat Hasil' : 'Mulai Test'}
              </Text>
              <Ionicons name="arrow-forward" size={14} color={colors.white} />
            </View>
          </View>
          <Ionicons
            name={lastTest ? 'bar-chart-outline' : 'sparkles'}
            size={64}
            color="rgba(255,255,255,0.2)"
          />
        </Pressable>


        {/* ── Featured Universities ── */}
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Kampus Unggulan</Text>
          <Pressable
            style={styles.seeAllBtn}
            onPress={() => router.push('/(tabs)/explore')}
          >
            <Text style={styles.seeAllText}>Lihat semua</Text>
            <Ionicons name="chevron-forward" size={14} color={colors.primary} />
          </Pressable>
        </View>

        {loadingUnis ? (
          <View style={styles.uniLoading}>
            <ActivityIndicator color={colors.primary} />
          </View>
        ) : featuredUnis.length > 0 ? (
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.uniScroll}
          >
            {featuredUnis.map((uni) => {
              const initial = uni.short_name?.[0]?.toUpperCase() ?? uni.name[0]?.toUpperCase() ?? '?'
              return (
                <Pressable
                  key={uni.id}
                  style={({ pressed }) => [styles.uniCard, pressed && { opacity: 0.88 }]}
                  onPress={() => router.push(`/university-detail?id=${uni.id}`)}
                >
                  {uni.partner_tier === 'premium' && (
                    <View style={styles.uniPremiumBadge}>
                      <Ionicons name="star" size={9} color="#F59E0B" />
                      <Text style={styles.uniPremiumText}>Premium</Text>
                    </View>
                  )}
                  <View style={styles.uniLogo}>
                    <Text style={styles.uniLogoText}>{initial}</Text>
                  </View>
                  <Text style={styles.uniShort} numberOfLines={1}>{uni.short_name}</Text>
                  <Text style={styles.uniName} numberOfLines={2}>{uni.name}</Text>
                  <View style={styles.uniMeta}>
                    <View style={[
                      styles.typeBadge,
                      uni.type === 'negeri' ? styles.typeBadgeNegeri : styles.typeBadgeSwasta,
                    ]}>
                      <Text style={[
                        styles.typeText,
                        uni.type === 'negeri' ? styles.typeTextNegeri : styles.typeTextSwasta,
                      ]}>
                        PT{uni.type === 'negeri' ? 'N' : 'S'}
                      </Text>
                    </View>
                    <Text style={styles.uniCity} numberOfLines={1}>{uni.city}</Text>
                  </View>
                </Pressable>
              )
            })}

            {/* See more card */}
            <Pressable
              style={[styles.uniCard, styles.uniSeeMoreCard]}
              onPress={() => router.push('/(tabs)/explore')}
            >
              <View style={styles.uniSeeMoreIcon}>
                <Ionicons name="arrow-forward" size={22} color={colors.primary} />
              </View>
              <Text style={styles.uniSeeMoreText}>Lihat{'\n'}Semua</Text>
            </Pressable>
          </ScrollView>
        ) : null}

        {/* ── Shortcuts ── */}
        <View style={styles.shortcuts}>
          {[
            { icon: 'clipboard-outline' as const, label: isLoggedIn && lastTest ? 'Ulangi Test' : 'Mulai Test', bg: '#EEF2FF', action: handleStartTest },
            { icon: 'business-outline' as const,  label: 'Cari Kampus',   bg: '#ECFDF5', action: () => router.push('/(tabs)/explore') },
            { icon: 'book-outline' as const,      label: 'Lihat Jurusan', bg: '#FFF0F6', action: () => router.push('/(tabs)/explore') },
          ].map((s, i) => (
            <Pressable
              key={i}
              style={({ pressed }) => [styles.shortcut, pressed && { opacity: 0.85 }]}
              onPress={s.action}
            >
              <View style={[styles.shortcutIcon, { backgroundColor: s.bg }]}>
                <Ionicons name={s.icon} size={22} color={colors.primary} />
              </View>
              <Text style={styles.shortcutLabel}>{s.label}</Text>
            </Pressable>
          ))}
        </View>

        {/* ── Articles ── */}
        <View style={[styles.sectionHeader, { marginTop: 28 }]}>
          <Text style={styles.sectionTitle}>Tips & Cerita</Text>
        </View>

        {ARTICLES.map((a, i) => (
          <Pressable key={i} style={({ pressed }) => [styles.articleCard, pressed && { opacity: 0.88 }]}>
            <View style={[styles.articleThumb, { backgroundColor: a.bg }]}>
              <Ionicons name={a.icon} size={24} color={colors.primary} />
            </View>
            <View style={styles.articleBody}>
              <View style={styles.articleMeta}>
                <Text style={styles.articleCategory}>{a.category}</Text>
                <Text style={styles.articleTime}>· {a.time}</Text>
              </View>
              <Text style={styles.articleTitle}>{a.title}</Text>
            </View>
            <Ionicons name="chevron-forward" size={18} color={colors.gray[300]} />
          </Pressable>
        ))}

      </ScrollView>

      <AuthPromptSheet
        visible={showPrompt}
        onClose={closePrompt}
        message="Daftar gratis untuk mulai test psikologi dan temukan jurusan yang paling cocok untukmu."
      />
    </SafeAreaView>
  )
}

// ─── Styles ───────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  scroll: { flex: 1 },
  content: { paddingBottom: 40 },

  header: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingHorizontal: 24, paddingTop: 16, paddingBottom: 8,
  },
  greeting: { fontSize: 24, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.5 },
  subGreeting: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub, marginTop: 2 },
  avatarBtn: {
    width: 44, height: 44, borderRadius: 12,
    backgroundColor: colors.primaryLight, alignItems: 'center', justifyContent: 'center',
  },
  avatarText: { fontSize: 18, fontFamily: fonts.bold, color: colors.primary },

  heroCard: {
    marginHorizontal: 24, marginTop: 16,
    backgroundColor: colors.primary, borderRadius: 16,
    padding: 24, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    shadowColor: colors.primary, shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.3, shadowRadius: 20, elevation: 8,
  },
  heroContent: { flex: 1, gap: 8 },
  heroBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    backgroundColor: 'rgba(255,255,255,0.2)',
    paddingHorizontal: 10, paddingVertical: 4, borderRadius: 100, alignSelf: 'flex-start',
  },
  heroBadgeText: { fontSize: 11, fontFamily: fonts.bold, color: colors.white },
  heroTitle: { fontSize: 20, fontFamily: fonts.extraBold, color: colors.white, letterSpacing: -0.4 },
  heroSub: { fontSize: 13, fontFamily: fonts.regular, color: 'rgba(255,255,255,0.75)' },
  heroBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    backgroundColor: 'rgba(255,255,255,0.2)',
    paddingHorizontal: 14, paddingVertical: 8, borderRadius: 100, alignSelf: 'flex-start', marginTop: 4,
  },
  heroBtnText: { fontSize: 13, fontFamily: fonts.bold, color: colors.white },


  sectionHeader: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingHorizontal: 24, marginTop: 28, marginBottom: 12,
  },
  sectionTitle: { fontSize: 18, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.3 },
  seeAllBtn: { flexDirection: 'row', alignItems: 'center', gap: 2 },
  seeAllText: { fontSize: 13, fontFamily: fonts.semiBold, color: colors.primary },

  // Featured universities
  uniLoading: { height: 160, alignItems: 'center', justifyContent: 'center' },
  uniScroll: { paddingLeft: 24, paddingRight: 12, gap: 12 },
  uniCard: {
    width: 150, backgroundColor: colors.surface, borderRadius: 14,
    padding: 16, gap: 6,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
  },
  uniPremiumBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 3,
    backgroundColor: '#FFFBEB', paddingHorizontal: 7, paddingVertical: 3, borderRadius: 6,
    alignSelf: 'flex-start',
  },
  uniPremiumText: { fontSize: 9, fontFamily: fonts.bold, color: '#F59E0B' },
  uniLogo: {
    width: 48, height: 48, borderRadius: 12,
    backgroundColor: colors.primaryLight, alignItems: 'center', justifyContent: 'center',
  },
  uniLogoText: { fontSize: 20, fontFamily: fonts.extraBold, color: colors.primary },
  uniShort: { fontSize: 12, fontFamily: fonts.bold, color: colors.primary },
  uniName: { fontSize: 12, fontFamily: fonts.medium, color: colors.text, lineHeight: 17 },
  uniMeta: { flexDirection: 'row', alignItems: 'center', gap: 5, marginTop: 2 },
  uniCity: { flex: 1, fontSize: 11, fontFamily: fonts.regular, color: colors.textMuted },
  typeBadge: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 5 },
  typeBadgeNegeri: { backgroundColor: '#EEF2FF' },
  typeBadgeSwasta: { backgroundColor: '#FFF3CD' },
  typeText: { fontSize: 9, fontFamily: fonts.bold },
  typeTextNegeri: { color: colors.primary },
  typeTextSwasta: { color: '#D97706' },
  uniSeeMoreCard: {
    alignItems: 'center', justifyContent: 'center',
    backgroundColor: colors.primaryMuted, borderWidth: 1.5, borderColor: colors.primaryLight,
    borderStyle: 'dashed',
  },
  uniSeeMoreIcon: {
    width: 48, height: 48, borderRadius: 24,
    backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center',
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06, shadowRadius: 6, elevation: 2,
  },
  uniSeeMoreText: { fontSize: 13, fontFamily: fonts.semiBold, color: colors.primary, textAlign: 'center', marginTop: 8 },

  // Shortcuts
  shortcuts: { flexDirection: 'row', paddingHorizontal: 24, marginTop: 20, gap: 12 },
  shortcut: {
    flex: 1, backgroundColor: colors.surface, borderRadius: 12,
    paddingVertical: 18, alignItems: 'center', gap: 10,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
  },
  shortcutIcon: { width: 48, height: 48, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  shortcutLabel: { fontSize: 12, fontFamily: fonts.semiBold, color: colors.text, textAlign: 'center' },

  // Articles
  articleCard: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: colors.surface, marginHorizontal: 24, marginBottom: 10,
    borderRadius: 12, padding: 14, gap: 14,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
  },
  articleThumb: { width: 52, height: 52, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  articleBody: { flex: 1, gap: 5 },
  articleMeta: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  articleCategory: { fontSize: 11, fontFamily: fonts.bold, color: colors.primary, textTransform: 'uppercase', letterSpacing: 0.5 },
  articleTime: { fontSize: 11, fontFamily: fonts.regular, color: colors.textMuted },
  articleTitle: { fontSize: 14, fontFamily: fonts.semiBold, color: colors.text, lineHeight: 20 },
})

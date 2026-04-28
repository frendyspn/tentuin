import React, { useEffect } from 'react'
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { Button } from '@tentuin/ui'
import { useRequireAuth } from '../../../hooks/useRequireAuth'
import { AuthPromptSheet } from '../../../components/auth/AuthPromptSheet'
import { useTestStore } from '../../../stores/testStore'
import { hapticMedium } from '../../../utils/haptics'

const INFO_ITEMS = [
  { icon: 'time-outline' as const,         label: '15–20 Menit',   sub: 'Estimasi waktu' },
  { icon: 'chatbubbles-outline' as const,  label: '60 Soal', sub: 'Berbasis RIASEC' },
  { icon: 'save-outline' as const,         label: 'Bisa Dijeda',   sub: 'Progress tersimpan' },
]

const RIASEC_TYPES = [
  { code: 'R', name: 'Realistic',     icon: 'hammer-outline' as const,        color: '#FFF3E0', iconColor: '#F97316' },
  { code: 'I', name: 'Investigative', icon: 'search-outline' as const,        color: '#E3F2FD', iconColor: '#3B82F6' },
  { code: 'A', name: 'Artistic',      icon: 'color-palette-outline' as const, color: '#FCE4EC', iconColor: '#EC4899' },
  { code: 'S', name: 'Social',        icon: 'people-outline' as const,        color: '#E8F5E9', iconColor: '#10B981' },
  { code: 'E', name: 'Enterprising',  icon: 'trending-up-outline' as const,   color: '#FFFDE7', iconColor: '#F59E0B' },
  { code: 'C', name: 'Conventional',  icon: 'document-outline' as const,        color: '#EDE7F6', iconColor: '#5C59F8' },
]

export default function TestScreen() {
  const router = useRouter()
  const { isGuest, requireAuth, showPrompt, closePrompt } = useRequireAuth()
  const { answers, currentIndex, questions, resetTest } = useTestStore()
  const hasProgress = questions.length > 0 && Object.keys(answers).length > 0
  const answeredCount = Object.keys(answers).length

  useEffect(() => {
    if (isGuest) {
      const t = setTimeout(() => requireAuth(() => {}), 400)
      return () => clearTimeout(t)
    }
  }, [isGuest])

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      {isGuest ? (
        <View style={styles.guestWrap}>
          <View style={styles.guestCard}>
            <View style={styles.lockIcon}>
              <Ionicons name="lock-closed" size={32} color={colors.primary} />
            </View>
            <Text style={styles.lockTitle}>Perlu Login Dulu</Text>
            <Text style={styles.lockDesc}>
              Buat akun gratis untuk mengikuti test psikologi dan dapatkan rekomendasi jurusan yang cocok.
            </Text>
            <Button label="Daftar Gratis — Yuk!" variant="primary" size="lg"
              onPress={() => router.push('/(auth)/register')} fullWidth />
            <Button label="Sudah Punya Akun" variant="outline" size="md"
              onPress={() => router.push('/(auth)/login')} fullWidth />
          </View>
        </View>
      ) : (
        <ScrollView
          contentContainerStyle={styles.readyContent}
          showsVerticalScrollIndicator={false}
        >
          {/* Header */}
          <View style={styles.testHeader}>
            <View style={styles.testBadge}>
              <Ionicons name="ribbon-outline" size={12} color={colors.primary} />
              <Text style={styles.testBadgeText}>RIASEC Test</Text>
            </View>
            <Text style={styles.testTitle}>Test Kepribadian</Text>
            <Text style={styles.testSub}>
              Jawab 60 pertanyaan singkat dan temukan tipe kepribadianmu beserta jurusan yang paling cocok.
            </Text>
          </View>

          {/* Info Cards */}
          <View style={styles.infoRow}>
            {INFO_ITEMS.map((item, i) => (
              <View key={i} style={styles.infoCard}>
                <Ionicons name={item.icon} size={22} color={colors.primary} />
                <Text style={styles.infoLabel}>{item.label}</Text>
                <Text style={styles.infoSub}>{item.sub}</Text>
              </View>
            ))}
          </View>

          {/* RIASEC */}
          <Text style={styles.riasecTitle}>6 Dimensi Kepribadian</Text>
          <View style={styles.riasecGrid}>
            {RIASEC_TYPES.map(t => (
              <View key={t.code} style={[styles.riasecCard, { backgroundColor: t.color }]}>
                <Ionicons name={t.icon} size={22} color={t.iconColor} />
                <Text style={styles.riasecCode}>{t.code}</Text>
                <Text style={styles.riasecName}>{t.name}</Text>
              </View>
            ))}
          </View>

          {/* Resume banner — tampil jika ada progress tersimpan */}
          {hasProgress && (
            <View style={styles.resumeBanner}>
              <Ionicons name="bookmark-outline" size={18} color={colors.primary} />
              <View style={{ flex: 1 }}>
                <Text style={styles.resumeTitle}>Ada test yang belum selesai</Text>
                <Text style={styles.resumeSub}>
                  Sudah menjawab {answeredCount} dari {questions.length} soal (soal {currentIndex + 1})
                </Text>
              </View>
              <Pressable onPress={resetTest} hitSlop={8}>
                <Ionicons name="close-circle-outline" size={20} color={colors.textMuted} />
              </Pressable>
            </View>
          )}

          {/* CTA */}
          <Pressable
            style={({ pressed }) => [styles.startBtn, pressed && { opacity: 0.88, transform: [{ scale: 0.97 }] }]}
            onPress={() => {
              hapticMedium()
              if (!hasProgress) resetTest()
              router.push('/test-session')
            }}
          >
            <Text style={styles.startBtnText}>
              {hasProgress ? `Lanjutkan (soal ${currentIndex + 1})` : 'Mulai Test Sekarang'}
            </Text>
            <Ionicons name="arrow-forward-circle" size={22} color={colors.white} />
          </Pressable>
        </ScrollView>
      )}

      <AuthPromptSheet
        visible={showPrompt}
        onClose={closePrompt}
        message="Buat akun gratis untuk ikut test psikologi RIASEC dan temukan jurusan terbaikmu!"
      />
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },

  guestWrap: { flex: 1, justifyContent: 'center', padding: 24 },
  guestCard: {
    backgroundColor: colors.surface,
    borderRadius: 20,
    padding: 28,
    alignItems: 'center',
    gap: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.06,
    shadowRadius: 16,
    elevation: 4,
  },
  lockIcon: {
    width: 72,
    height: 72,
    borderRadius: 18,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
  },
  lockTitle: { fontSize: 22, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.5 },
  lockDesc: { fontSize: 15, fontFamily: fonts.regular, color: colors.textSub, textAlign: 'center', lineHeight: 22, marginBottom: 4 },

  readyContent: { padding: 24, paddingBottom: 40, gap: 20 },

  testHeader: { gap: 10 },
  testBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: colors.primaryMuted,
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 100,
    alignSelf: 'flex-start',
  },
  testBadgeText: { fontSize: 11, fontFamily: fonts.bold, color: colors.primary, letterSpacing: 0.5 },
  testTitle: { fontSize: 36, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -1.2, lineHeight: 42 },
  testSub: { fontSize: 15, fontFamily: fonts.regular, color: colors.textSub, lineHeight: 23 },

  infoRow: { flexDirection: 'row', gap: 10 },
  infoCard: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    gap: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 2,
  },
  infoLabel: { fontSize: 12, fontFamily: fonts.bold, color: colors.text, textAlign: 'center' },
  infoSub:   { fontSize: 10, fontFamily: fonts.regular, color: colors.textMuted, textAlign: 'center' },

  riasecTitle: { fontSize: 13, fontFamily: fonts.bold, color: colors.textSub, letterSpacing: 0.8, textTransform: 'uppercase' },
  riasecGrid:  { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  riasecCard: {
    width: '30.5%',
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    gap: 5,
  },
  riasecCode: { fontSize: 15, fontFamily: fonts.extraBold, color: colors.text },
  riasecName: { fontSize: 10, fontFamily: fonts.medium, color: colors.textSub, textAlign: 'center' },

  resumeBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    backgroundColor: colors.primaryMuted,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.primaryLight,
  },
  resumeTitle: { fontSize: 13, fontFamily: fonts.semiBold, color: colors.primary },
  resumeSub:   { fontSize: 12, fontFamily: fonts.regular, color: colors.textSub, marginTop: 2 },

  startBtn: {
    backgroundColor: colors.primary,
    borderRadius: 14,
    paddingVertical: 18,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    shadowColor: colors.primary,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 16,
    elevation: 6,
  },
  startBtnText: { fontSize: 16, fontFamily: fonts.bold, color: colors.white },
})

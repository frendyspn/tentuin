import React, { useEffect, useState } from 'react'
import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { signOut, clearPushToken, getTestHistory, getBookmarkedUniversities } from '@tentuin/supabase'
import { colors, fonts } from '@tentuin/config'
import { Button } from '@tentuin/ui'
import { useAuthStore } from '../../../stores/authStore'
import { useRequireAuth } from '../../../hooks/useRequireAuth'
import { AuthPromptSheet } from '../../../components/auth/AuthPromptSheet'
import { hapticLight, hapticWarning } from '../../../utils/haptics'

const MENU_ITEMS = [
  { icon: 'stats-chart-outline' as const, label: 'Riwayat Test',    sub: 'Lihat hasil test sebelumnya', bg: '#EEF2FF', color: colors.primary },
  { icon: 'heart-outline' as const,       label: 'Jurusan Favorit', sub: 'Jurusan yang kamu simpan',   bg: '#FFF0F6', color: '#EC4899' },
  { icon: 'business-outline' as const,    label: 'Kampus Favorit',  sub: 'Universitas yang kamu suka', bg: '#ECFDF5', color: '#10B981' },
  { icon: 'create-outline' as const,      label: 'Edit Profil',     sub: 'Ubah nama, sekolah, kota',   bg: '#FFFBEB', color: '#F59E0B' },
]

export default function ProfileScreen() {
  const router = useRouter()
  const { user, profile, session, reset } = useAuthStore()
  const { isGuest, showPrompt, closePrompt } = useRequireAuth()
  const [testCount, setTestCount] = useState<number | null>(null)
  const [campusCount, setCampusCount] = useState<number | null>(null)

  useEffect(() => {
    if (!session?.user?.id || !session?.access_token) return
    const uid = session.user.id
    const token = session.access_token
    getTestHistory(uid, token)
      .then((data) => setTestCount(data?.length ?? 0))
      .catch(() => setTestCount(0))
    getBookmarkedUniversities(uid, token)
      .then((data) => setCampusCount(data?.length ?? 0))
      .catch(() => setCampusCount(0))
  }, [session?.user?.id])

  const handleSignOut = () => {
    Alert.alert('Keluar', 'Yakin mau keluar dari akun?', [
      { text: 'Batal', style: 'cancel' },
      {
        text: 'Keluar', style: 'destructive',
        onPress: () => {
          hapticWarning()
          // Simpan userId sebelum reset (untuk clearPushToken di background)
          const uid = session?.user?.id
          // Hapus sesi lokal & navigasi DULU — tidak perlu tunggu network
          reset()
          router.replace('/(auth)/login')
          // Background: hapus push token dari DB + invalidate Supabase token
          if (uid) clearPushToken(uid).catch(() => {})
          signOut().catch(err => console.warn('[SignOut] Background error:', err))
        },
      },
    ])
  }

  if (isGuest) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <View style={styles.guestWrap}>
          <View style={styles.guestCard}>
            <View style={styles.guestIcon}>
              <Ionicons name="person" size={36} color={colors.primary} />
            </View>
            <Text style={styles.guestTitle}>Belum Masuk</Text>
            <Text style={styles.guestDesc}>
              Masuk untuk melihat profil, riwayat test, dan jurusan favoritmu.
            </Text>
            <Button label="Daftar Gratis" variant="primary" size="lg"
              onPress={() => router.push('/(auth)/register')} fullWidth />
            <Button label="Masuk" variant="outline" size="md"
              onPress={() => router.push('/(auth)/login')} fullWidth />
          </View>
        </View>
        <AuthPromptSheet visible={showPrompt} onClose={closePrompt} />
      </SafeAreaView>
    )
  }

  const initial = profile?.full_name?.[0]?.toUpperCase() ?? user?.email?.[0]?.toUpperCase() ?? '?'

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>

        {/* ── Profile Card ── */}
        <View style={styles.profileCard}>
          <View style={styles.avatarWrap}>
            <View style={styles.avatar}>
              <Text style={styles.avatarText}>{initial}</Text>
            </View>
            <View style={styles.editDot}>
              <Ionicons name="pencil" size={10} color={colors.primary} />
            </View>
          </View>
          <Text style={styles.name}>{profile?.full_name ?? 'Pengguna Tentuin'}</Text>
          <Text style={styles.email}>{user?.email}</Text>
          {profile?.school_name && (
            <View style={styles.schoolBadge}>
              <Ionicons name="school-outline" size={12} color={colors.primary} />
              <Text style={styles.schoolText}>{profile.school_name}</Text>
            </View>
          )}
        </View>

        {/* ── Stats ── */}
        <View style={styles.statsRow}>
          {[
            { icon: 'clipboard-outline' as const, value: testCount === null ? '…' : String(testCount), label: 'Test' },
            { icon: 'heart-outline' as const,     value: '0', label: 'Simpan' },
            { icon: 'business-outline' as const,  value: campusCount === null ? '…' : String(campusCount), label: 'Kampus' },
          ].map((s, i) => (
            <View key={i} style={styles.statCard}>
              <Ionicons name={s.icon} size={20} color={colors.primary} />
              <Text style={styles.statValue}>{s.value}</Text>
              <Text style={styles.statLabel}>{s.label}</Text>
            </View>
          ))}
        </View>

        {/* ── Menu ── */}
        <Text style={styles.sectionLabel}>Akun Saya</Text>
        <View style={styles.menuCard}>
          {MENU_ITEMS.map((item, i) => (
            <Pressable
              key={i}
              onPress={() => {
                hapticLight()
                if (i === 0) router.push('/test-history')
                else if (i === 1) router.push('/favorites-majors')
                else if (i === 2) router.push('/favorites-campus')
                else if (i === 3) router.push('/edit-profile')
              }}
              style={({ pressed }) => [
                styles.menuItem,
                i < MENU_ITEMS.length - 1 && styles.menuItemBorder,
                pressed && { backgroundColor: colors.gray[50] },
              ]}
            >
              <View style={[styles.menuIcon, { backgroundColor: item.bg }]}>
                <Ionicons name={item.icon} size={20} color={item.color} />
              </View>
              <View style={styles.menuBody}>
                <Text style={styles.menuLabel}>{item.label}</Text>
                <Text style={styles.menuSub}>{item.sub}</Text>
              </View>
              <Ionicons name="chevron-forward" size={18} color={colors.gray[300]} />
            </Pressable>
          ))}
        </View>

        {/* ── Logout ── */}
        <Pressable style={styles.logoutBtn} onPress={handleSignOut}>
          <Ionicons name="log-out-outline" size={16} color={colors.error} />
          <Text style={styles.logoutText}>Keluar dari Akun</Text>
        </Pressable>

      </ScrollView>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  content: { padding: 24, gap: 16, paddingBottom: 48 },

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
  guestIcon: {
    width: 80,
    height: 80,
    borderRadius: 20,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
  },
  guestTitle: { fontSize: 22, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.5 },
  guestDesc: { fontSize: 15, fontFamily: fonts.regular, color: colors.textSub, textAlign: 'center', lineHeight: 22, marginBottom: 4 },

  profileCard: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: 24,
    alignItems: 'center',
    gap: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 12,
    elevation: 3,
  },
  avatarWrap: { position: 'relative', marginBottom: 4 },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { fontSize: 32, fontFamily: fonts.extraBold, color: colors.primary },
  editDot: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 26,
    height: 26,
    borderRadius: 8,
    backgroundColor: colors.surface,
    borderWidth: 2,
    borderColor: colors.background,
    alignItems: 'center',
    justifyContent: 'center',
  },
  name: { fontSize: 20, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.4 },
  email: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub },
  schoolBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: colors.primaryMuted,
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 100,
    marginTop: 4,
  },
  schoolText: { fontSize: 12, fontFamily: fonts.semiBold, color: colors.primary },

  statsRow: { flexDirection: 'row', gap: 12 },
  statCard: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    gap: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 2,
  },
  statValue: { fontSize: 20, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.5 },
  statLabel: { fontSize: 11, fontFamily: fonts.medium, color: colors.textSub },

  sectionLabel: {
    fontSize: 12,
    fontFamily: fonts.bold,
    color: colors.textMuted,
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  menuCard: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 12,
    elevation: 3,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 18,
    paddingVertical: 16,
    gap: 14,
  },
  menuItemBorder: { borderBottomWidth: 1, borderBottomColor: colors.border },
  menuIcon: {
    width: 42,
    height: 42,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  menuBody: { flex: 1, gap: 2 },
  menuLabel: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.text },
  menuSub: { fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },

  logoutBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    alignSelf: 'center',
    paddingHorizontal: 20,
    paddingVertical: 11,
    borderRadius: 100,
    backgroundColor: colors.errorLight,
    marginTop: 4,
  },
  logoutText: { fontSize: 14, fontFamily: fonts.bold, color: colors.error },
})

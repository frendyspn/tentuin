import React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { hapticLight } from '../utils/haptics'

const COMING_FEATURES = [
  {
    icon: 'bookmark-outline' as const,
    label: 'Simpan jurusan dari halaman detail',
    color: '#EC4899',
    bg:    '#FFF0F6',
  },
  {
    icon: 'notifications-outline' as const,
    label: 'Notifikasi info pendaftaran jurusan',
    color: '#F59E0B',
    bg:    '#FFFBEB',
  },
  {
    icon: 'git-compare-outline' as const,
    label: 'Bandingkan peluang antar jurusan',
    color: colors.primary,
    bg:    colors.primaryMuted,
  },
]

export default function FavoritesMajorsScreen() {
  const router = useRouter()

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>

      {/* Header */}
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={22} color={colors.text} />
        </Pressable>
        <Text style={styles.title}>Jurusan Favorit</Text>
        <View style={{ width: 36 }} />
      </View>

      {/* Content */}
      <View style={styles.content}>

        {/* Icon + badge */}
        <View style={styles.iconWrap}>
          <Ionicons name="heart" size={44} color="#EC4899" />
          <View style={styles.soonBadge}>
            <Text style={styles.soonBadgeText}>Segera</Text>
          </View>
        </View>

        {/* Copy */}
        <Text style={styles.heading}>Fitur Sedang Disiapkan</Text>
        <Text style={styles.sub}>
          Kami sedang membangun fitur ini agar kamu bisa menyimpan dan
          mengelola jurusan impianmu dengan lebih mudah.
        </Text>

        {/* Preview fitur */}
        <View style={styles.featureCard}>
          <Text style={styles.featureCardTitle}>Yang akan hadir:</Text>
          {COMING_FEATURES.map((f, i) => (
            <View key={i} style={styles.featureRow}>
              <View style={[styles.featureIcon, { backgroundColor: f.bg }]}>
                <Ionicons name={f.icon} size={16} color={f.color} />
              </View>
              <Text style={styles.featureLabel}>{f.label}</Text>
            </View>
          ))}
        </View>

        {/* CTA — arahkan ke Explore supaya user tetap engaged */}
        <Pressable
          style={({ pressed }) => [styles.ctaBtn, pressed && { opacity: 0.88 }]}
          onPress={() => { hapticLight(); router.replace('/(tabs)/explore') }}
        >
          <Ionicons name="search-outline" size={18} color={colors.white} />
          <Text style={styles.ctaBtnText}>Jelajahi Jurusan Sekarang</Text>
        </Pressable>

      </View>
    </SafeAreaView>
  )
}

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

  content: {
    flex: 1, alignItems: 'center', justifyContent: 'center',
    padding: 28, gap: 16,
  },

  iconWrap: { position: 'relative', marginBottom: 4 },
  iconBg: {
    width: 96, height: 96, borderRadius: 24,
    backgroundColor: '#FFF0F6', alignItems: 'center', justifyContent: 'center',
  },
  soonBadge: {
    position: 'absolute', bottom: -8, right: -12,
    backgroundColor: '#EC4899',
    paddingHorizontal: 8, paddingVertical: 3,
    borderRadius: 20,
  },
  soonBadgeText: { fontSize: 10, fontFamily: fonts.bold, color: colors.white, letterSpacing: 0.4 },

  heading: {
    fontSize: 22, fontFamily: fonts.extraBold,
    color: colors.text, letterSpacing: -0.5, textAlign: 'center',
  },
  sub: {
    fontSize: 14, fontFamily: fonts.regular,
    color: colors.textSub, textAlign: 'center', lineHeight: 22,
  },

  featureCard: {
    width: '100%',
    backgroundColor: colors.surface,
    borderRadius: 16, padding: 16, gap: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
    marginTop: 4,
  },
  featureCardTitle: {
    fontSize: 12, fontFamily: fonts.bold,
    color: colors.textMuted, letterSpacing: 0.6, textTransform: 'uppercase',
  },
  featureRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  featureIcon: {
    width: 34, height: 34, borderRadius: 9,
    alignItems: 'center', justifyContent: 'center',
  },
  featureLabel: { flex: 1, fontSize: 14, fontFamily: fonts.medium, color: colors.text },

  ctaBtn: {
    width: '100%', flexDirection: 'row', alignItems: 'center',
    justifyContent: 'center', gap: 8,
    backgroundColor: '#EC4899',
    borderRadius: 14, paddingVertical: 15, marginTop: 4,
    shadowColor: '#EC4899',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.25, shadowRadius: 12, elevation: 5,
  },
  ctaBtnText: { fontSize: 15, fontFamily: fonts.bold, color: colors.white },
})

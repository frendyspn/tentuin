import React, { useEffect, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter, useLocalSearchParams } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { supabaseUrl, supabaseAnonKey, type UniversityRow } from '@tentuin/supabase'

// ─── Constants ────────────────────────────────────────────────────────────────

const RIASEC_LABEL: Record<string, string> = {
  R: 'Realistis', I: 'Investigatif', A: 'Artistik',
  S: 'Sosial',    E: 'Enterprising', C: 'Konvensional',
}

const RIASEC_DESC: Record<string, string> = {
  R: 'Suka pekerjaan teknis, praktis, dan hal nyata',
  I: 'Analitis, suka berpikir, dan meneliti',
  A: 'Kreatif, imajinatif, dan ekspresif',
  S: 'Suka membantu, mengajar, dan bekerja sama',
  E: 'Jiwa pemimpin, persuasif, dan wirausaha',
  C: 'Teratur, detail, dan berorientasi sistem',
}

const RIASEC_COLOR: Record<string, { bg: string; color: string }> = {
  R: { bg: '#F0F9FF', color: '#3B82F6' },
  I: { bg: '#EEF2FF', color: '#6C63FF' },
  A: { bg: '#FFF0F6', color: '#EC4899' },
  S: { bg: '#ECFDF5', color: '#10B981' },
  E: { bg: '#FFFBEB', color: '#F59E0B' },
  C: { bg: '#F9FAFB', color: '#6B7280' },
}

// ─── Types ────────────────────────────────────────────────────────────────────

type MajorUniversity = UniversityRow & { faculty: string | null }

// ─── Helpers ─────────────────────────────────────────────────────────────────

const getMajorIcon = (name: string): { icon: any; bg: string; color: string } => {
  const n = name.toLowerCase()
  if (n.includes('informatika') || n.includes('komputer') || n.includes('sistem informasi'))
    return { icon: 'desktop-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('teknik') && !n.includes('informatika'))
    return { icon: 'construct-outline', bg: '#F0F9FF', color: '#3B82F6' }
  if (n.includes('psikologi'))
    return { icon: 'sparkles-outline', bg: '#EEF2FF', color: colors.primary }
  if (n.includes('desain') || n.includes('seni') || n.includes('dkv'))
    return { icon: 'color-palette-outline', bg: '#FFF0F6', color: '#EC4899' }
  if (n.includes('arsitektur'))
    return { icon: 'business-outline', bg: '#FFF0F6', color: '#EC4899' }
  if (n.includes('manajemen') || n.includes('bisnis'))
    return { icon: 'trending-up-outline', bg: '#FFFBEB', color: '#F59E0B' }
  if (n.includes('akuntansi') || n.includes('keuangan') || n.includes('ekonomi'))
    return { icon: 'cash-outline', bg: '#FFFBEB', color: '#F59E0B' }
  if (n.includes('kedokteran') || n.includes('medis'))
    return { icon: 'medkit-outline', bg: '#FFF0F0', color: '#EF4444' }
  if (n.includes('farmasi') || n.includes('apoteker'))
    return { icon: 'flask-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('hukum'))
    return { icon: 'scale-outline', bg: '#EEF2FF', color: colors.primary }
  if (n.includes('komunikasi') || n.includes('jurnalis'))
    return { icon: 'megaphone-outline', bg: '#FFF0F6', color: '#EC4899' }
  if (n.includes('biologi') || n.includes('kimia') || n.includes('fisika') || n.includes('matematika'))
    return { icon: 'flask-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('pertanian') || n.includes('agro') || n.includes('peternakan') || n.includes('perikanan'))
    return { icon: 'leaf-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('musik') || n.includes('teater') || n.includes('film'))
    return { icon: 'musical-notes-outline', bg: '#FFF0F6', color: '#EC4899' }
  return { icon: 'school-outline', bg: '#EEF2FF', color: colors.primary }
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

export default function MajorDetailScreen() {
  const router = useRouter()
  const { name, codes } = useLocalSearchParams<{ name: string; codes: string }>()

  const [universities, setUniversities] = useState<MajorUniversity[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const riasecCodes = codes ? codes.split(',').filter(Boolean) : []
  const majorName = name ? decodeURIComponent(name) : ''
  const { icon, bg, color } = getMajorIcon(majorName)

  useEffect(() => {
    if (!majorName) { setError('Nama jurusan tidak valid'); setLoading(false); return }

    const fetchUniversities = async () => {
      try {
        // Fetch all majors with this exact name + their university data
        const encoded = encodeURIComponent(majorName)
        const res = await fetch(
          `${supabaseUrl}/rest/v1/majors?name=eq.${encoded}&is_active=eq.true` +
          `&select=faculty,universities(id,name,short_name,city,province,type,is_partner,partner_tier)`,
          {
            headers: {
              apikey: supabaseAnonKey,
              Authorization: `Bearer ${supabaseAnonKey}`,
            },
          }
        )
        if (!res.ok) throw new Error(`${res.status}`)
        const data: { faculty: string | null; universities: UniversityRow }[] = await res.json()

        // Flatten + sort: partner first, then alphabetical
        const result: MajorUniversity[] = data
          .filter(d => d.universities)
          .map(d => ({ ...d.universities, faculty: d.faculty }))
          .sort((a, b) => {
            if (a.is_partner !== b.is_partner) return a.is_partner ? -1 : 1
            return a.name.localeCompare(b.name)
          })

        setUniversities(result)
      } catch (e: any) {
        setError(e?.message ?? 'Gagal memuat data')
      } finally {
        setLoading(false)
      }
    }

    fetchUniversities()
  }, [majorName])

  const ListHeader = () => (
    <>
      {/* ── Hero ── */}
      <View style={styles.hero}>
        <View style={[styles.heroIcon, { backgroundColor: bg }]}>
          <Ionicons name={icon} size={40} color={color} />
        </View>
        <Text style={styles.heroName}>{majorName}</Text>

        {/* RIASEC chips */}
        {riasecCodes.length > 0 && (
          <View style={styles.riasecRow}>
            {riasecCodes.map(code => {
              const rc = RIASEC_COLOR[code] ?? { bg: colors.primaryMuted, color: colors.primary }
              return (
                <View key={code} style={[styles.riasecChip, { backgroundColor: rc.bg }]}>
                  <Text style={[styles.riasecChipCode, { color: rc.color }]}>{code}</Text>
                  <Text style={[styles.riasecChipLabel, { color: rc.color }]}>{RIASEC_LABEL[code]}</Text>
                </View>
              )
            })}
          </View>
        )}

        {/* RIASEC trait descriptions */}
        {riasecCodes.length > 0 && (
          <View style={styles.traitsRow}>
            {riasecCodes.map(code => (
              <View key={code} style={styles.traitItem}>
                <Ionicons name="checkmark-circle" size={14} color={color} />
                <Text style={styles.traitText}>{RIASEC_DESC[code]}</Text>
              </View>
            ))}
          </View>
        )}
      </View>

      {/* ── Section header ── */}
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>Kampus yang Menyediakan</Text>
        {!loading && (
          <View style={styles.countBadge}>
            <Text style={styles.countText}>{universities.length} kampus</Text>
          </View>
        )}
      </View>
    </>
  )

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      {/* Header bar */}
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={22} color={colors.text} />
        </Pressable>
        <Text style={styles.headerTitle} numberOfLines={1}>{majorName}</Text>
        <View style={{ width: 36 }} />
      </View>

      {error ? (
        <View style={styles.centered}>
          <Ionicons name="alert-circle-outline" size={40} color={colors.error} />
          <Text style={styles.errorText}>{error}</Text>
          <Pressable style={styles.retryBtn} onPress={() => router.back()}>
            <Text style={styles.retryText}>Kembali</Text>
          </Pressable>
        </View>
      ) : (
        <FlatList
          data={loading ? [] : universities}
          keyExtractor={item => item.id}
          ListHeaderComponent={<ListHeader />}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          ListEmptyComponent={
            <View style={styles.centered}>
              {loading
                ? <ActivityIndicator size="large" color={colors.primary} />
                : <>
                    <Ionicons name="business-outline" size={40} color={colors.gray[300]} />
                    <Text style={styles.emptyText}>Belum ada kampus terdaftar</Text>
                  </>
              }
            </View>
          }
          renderItem={({ item }) => {
            const initial = item.short_name?.[0]?.toUpperCase() ?? item.name[0]?.toUpperCase() ?? '?'
            return (
              <Pressable
                style={({ pressed }) => [styles.uniCard, pressed && { opacity: 0.88 }]}
                onPress={() => router.push(`/university-detail?id=${item.id}`)}
              >
                <View style={styles.uniLogo}>
                  <Text style={styles.uniLogoText}>{initial}</Text>
                </View>

                <View style={styles.uniBody}>
                  <View style={styles.uniNameRow}>
                    <Text style={styles.uniName} numberOfLines={1}>{item.name}</Text>
                    {item.is_partner && (
                      <View style={[
                        styles.partnerBadge,
                        item.partner_tier === 'premium' && styles.partnerBadgePremium,
                      ]}>
                        <Ionicons
                          name={item.partner_tier === 'premium' ? 'star' : 'checkmark-circle'}
                          size={9}
                          color={item.partner_tier === 'premium' ? '#F59E0B' : colors.primary}
                        />
                        <Text style={[
                          styles.partnerText,
                          item.partner_tier === 'premium' && styles.partnerTextPremium,
                        ]}>
                          {item.partner_tier === 'premium' ? 'Premium' : 'Partner'}
                        </Text>
                      </View>
                    )}
                  </View>

                  <Text style={styles.uniShort}>{item.short_name}</Text>

                  <View style={styles.uniMeta}>
                    <View style={[
                      styles.typeBadge,
                      item.type === 'negeri' ? styles.typeBadgeNegeri : styles.typeBadgeSwasta,
                    ]}>
                      <Text style={[
                        styles.typeText,
                        item.type === 'negeri' ? styles.typeTextNegeri : styles.typeTextSwasta,
                      ]}>
                        PT{item.type === 'negeri' ? 'N' : 'S'}
                      </Text>
                    </View>
                    <Ionicons name="location-outline" size={11} color={colors.textMuted} />
                    <Text style={styles.uniCity} numberOfLines={1}>{item.city}</Text>
                    {item.faculty && (
                      <>
                        <Text style={styles.uniDot}>·</Text>
                        <Text style={styles.uniFaculty} numberOfLines={1}>{item.faculty}</Text>
                      </>
                    )}
                  </View>
                </View>

                <Ionicons name="chevron-forward" size={16} color={colors.gray[300]} />
              </Pressable>
            )
          }}
        />
      )}
    </SafeAreaView>
  )
}

// ─── Styles ──────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  centered: { alignItems: 'center', justifyContent: 'center', paddingVertical: 64, gap: 14 },
  listContent: { paddingBottom: 48 },

  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 20, paddingTop: 4, paddingBottom: 12,
    borderBottomWidth: 1, borderBottomColor: colors.border,
  },
  headerTitle: { flex: 1, textAlign: 'center', fontSize: 17, fontFamily: fonts.bold, color: colors.text, marginHorizontal: 8 },
  backBtn: { width: 36, height: 36, borderRadius: 10, backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center' },

  // Hero section
  hero: {
    alignItems: 'center', paddingHorizontal: 24, paddingTop: 28, paddingBottom: 8, gap: 14,
  },
  heroIcon: { width: 88, height: 88, borderRadius: 24, alignItems: 'center', justifyContent: 'center' },
  heroName: { fontSize: 22, fontFamily: fonts.extraBold, color: colors.text, textAlign: 'center', letterSpacing: -0.5, lineHeight: 30 },

  riasecRow: { flexDirection: 'row', gap: 8, flexWrap: 'wrap', justifyContent: 'center' },
  riasecChip: {
    flexDirection: 'row', alignItems: 'center', gap: 5,
    paddingHorizontal: 12, paddingVertical: 6, borderRadius: 100,
  },
  riasecChipCode: { fontSize: 13, fontFamily: fonts.extraBold },
  riasecChipLabel: { fontSize: 13, fontFamily: fonts.semiBold },

  traitsRow: {
    alignSelf: 'stretch',
    backgroundColor: colors.surface, borderRadius: 12,
    padding: 16, gap: 10,
    borderWidth: 1, borderColor: colors.border,
  },
  traitItem: { flexDirection: 'row', alignItems: 'flex-start', gap: 8 },
  traitText: { flex: 1, fontSize: 13, fontFamily: fonts.regular, color: colors.textSub, lineHeight: 20 },

  // Section header
  sectionHeader: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 24, paddingTop: 24, paddingBottom: 10,
  },
  sectionTitle: { fontSize: 16, fontFamily: fonts.bold, color: colors.text },
  countBadge: { backgroundColor: colors.primaryMuted, paddingHorizontal: 10, paddingVertical: 4, borderRadius: 100 },
  countText: { fontSize: 12, fontFamily: fonts.semiBold, color: colors.primary },

  // University card
  uniCard: {
    flexDirection: 'row', alignItems: 'center', gap: 14,
    marginHorizontal: 24, marginBottom: 10,
    backgroundColor: colors.surface, borderRadius: 14, padding: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
  },
  uniLogo: {
    width: 50, height: 50, borderRadius: 12,
    backgroundColor: colors.primaryLight, alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  uniLogoText: { fontSize: 20, fontFamily: fonts.extraBold, color: colors.primary },
  uniBody: { flex: 1, gap: 3 },
  uniNameRow: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  uniName: { flex: 1, fontSize: 14, fontFamily: fonts.bold, color: colors.text },
  uniShort: { fontSize: 12, fontFamily: fonts.medium, color: colors.textSub },
  uniMeta: { flexDirection: 'row', alignItems: 'center', gap: 5, flexWrap: 'wrap' },
  uniCity: { fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },
  uniDot: { fontSize: 12, color: colors.textMuted },
  uniFaculty: { flex: 1, fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },

  typeBadge: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 5 },
  typeBadgeNegeri: { backgroundColor: '#EEF2FF' },
  typeBadgeSwasta: { backgroundColor: '#FFF3CD' },
  typeText: { fontSize: 9, fontFamily: fonts.bold },
  typeTextNegeri: { color: colors.primary },
  typeTextSwasta: { color: '#D97706' },

  partnerBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, paddingHorizontal: 6, paddingVertical: 2, borderRadius: 5, backgroundColor: colors.primaryMuted },
  partnerBadgePremium: { backgroundColor: '#FFFBEB' },
  partnerText: { fontSize: 9, fontFamily: fonts.bold, color: colors.primary },
  partnerTextPremium: { color: '#F59E0B' },

  emptyText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub },
  errorText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub, textAlign: 'center', paddingHorizontal: 24 },
  retryBtn: { paddingHorizontal: 24, paddingVertical: 10, backgroundColor: colors.primaryLight, borderRadius: 10 },
  retryText: { fontSize: 14, fontFamily: fonts.semiBold, color: colors.primary },
})

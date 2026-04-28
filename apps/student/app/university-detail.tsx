import React, { useEffect, useState } from 'react'
import {
  ActivityIndicator,
  Alert,
  Linking,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter, useLocalSearchParams } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import {
  getUniversityWithMajors,
  checkUniversityBookmark,
  saveUniversityBookmark,
  deleteUniversityBookmark,
  type UniversityWithMajors,
} from '@tentuin/supabase'
import { useAuthStore } from '../stores/authStore'
import { hapticSuccess, hapticWarning } from '../utils/haptics'

const RIASEC_COLOR: Record<string, { bg: string; color: string }> = {
  R: { bg: '#F0F9FF', color: '#3B82F6' },
  I: { bg: '#EEF2FF', color: '#6C63FF' },
  A: { bg: '#FFF0F6', color: '#EC4899' },
  S: { bg: '#ECFDF5', color: '#10B981' },
  E: { bg: '#FFFBEB', color: '#F59E0B' },
  C: { bg: '#F9FAFB', color: '#6B7280' },
}

export default function UniversityDetailScreen() {
  const router = useRouter()
  const { id } = useLocalSearchParams<{ id: string }>()
  const { session } = useAuthStore()

  const [data, setData] = useState<UniversityWithMajors | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [searchMajor, setSearchMajor] = useState('')

  const [bookmarked, setBookmarked] = useState(false)
  const [bookmarkLoading, setBookmarkLoading] = useState(false)

  // Load university data + check bookmark status in parallel
  useEffect(() => {
    if (!id) { setError('ID tidak valid'); setLoading(false); return }

    const tasks: Promise<any>[] = [getUniversityWithMajors(id)]
    if (session?.user?.id) {
      tasks.push(checkUniversityBookmark(session.user.id, id, session.access_token))
    }

    Promise.all(tasks)
      .then(([uni, bm]) => {
        setData(uni)
        if (bm !== undefined) setBookmarked(bm !== null)
      })
      .catch((e) => setError(e?.message ?? 'Gagal memuat data'))
      .finally(() => setLoading(false))
  }, [id])

  const handleToggleBookmark = async () => {
    if (!session?.user?.id) {
      Alert.alert('Login diperlukan', 'Masuk untuk menyimpan kampus favorit.')
      return
    }
    if (!id) return

    setBookmarkLoading(true)
    try {
      if (bookmarked) {
        await deleteUniversityBookmark(session.user.id, id, session.access_token)
        setBookmarked(false)
        hapticWarning()
      } else {
        await saveUniversityBookmark(session.user.id, id, [], session.access_token)
        setBookmarked(true)
        hapticSuccess()
      }
    } catch (e: any) {
      Alert.alert('Gagal', e?.message ?? 'Coba lagi.')
    } finally {
      setBookmarkLoading(false)
    }
  }

  const HeaderBar = ({ title }: { title: string }) => (
    <View style={styles.header}>
      <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
        <Ionicons name="arrow-back" size={22} color={colors.text} />
      </Pressable>
      <Text style={styles.headerTitle} numberOfLines={1}>{title}</Text>
      <Pressable
        onPress={handleToggleBookmark}
        hitSlop={12}
        style={[styles.bookmarkBtn, bookmarked && styles.bookmarkBtnActive]}
        disabled={bookmarkLoading}
      >
        {bookmarkLoading
          ? <ActivityIndicator size="small" color={bookmarked ? colors.white : colors.primary} />
          : <Ionicons
              name={bookmarked ? 'bookmark' : 'bookmark-outline'}
              size={20}
              color={bookmarked ? colors.white : colors.primary}
            />
        }
      </Pressable>
    </View>
  )

  if (loading) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <HeaderBar title="Detail Kampus" />
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </SafeAreaView>
    )
  }

  if (error || !data) {
    return (
      <SafeAreaView style={styles.safe} edges={['top']}>
        <HeaderBar title="Detail Kampus" />
        <View style={styles.centered}>
          <Ionicons name="alert-circle-outline" size={40} color={colors.error} />
          <Text style={styles.errorText}>{error ?? 'Data tidak ditemukan'}</Text>
          <Pressable style={styles.retryBtn} onPress={() => router.back()}>
            <Text style={styles.retryText}>Kembali</Text>
          </Pressable>
        </View>
      </SafeAreaView>
    )
  }

  const initial = data.short_name?.[0]?.toUpperCase() ?? data.name[0]?.toUpperCase() ?? '?'

  const filteredMajors = data.majors.filter(m =>
    !searchMajor.trim() || m.name.toLowerCase().includes(searchMajor.toLowerCase())
  )

  const byFaculty = filteredMajors.reduce<Record<string, typeof filteredMajors>>((acc, m) => {
    const key = m.faculty ?? 'Lainnya'
    acc[key] = acc[key] ?? []
    acc[key].push(m)
    return acc
  }, {})

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <HeaderBar title={data.short_name} />

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>

        {/* ── Hero Card ── */}
        <View style={styles.heroCard}>
          <View style={styles.heroLogo}>
            <Text style={styles.heroLogoText}>{initial}</Text>
          </View>

          <View style={styles.heroInfo}>
            <View style={styles.heroNameRow}>
              {data.is_partner && (
                <View style={[
                  styles.partnerBadge,
                  data.partner_tier === 'premium' && styles.partnerBadgePremium,
                ]}>
                  <Ionicons
                    name={data.partner_tier === 'premium' ? 'star' : 'checkmark-circle'}
                    size={10}
                    color={data.partner_tier === 'premium' ? '#F59E0B' : colors.primary}
                  />
                  <Text style={[
                    styles.partnerText,
                    data.partner_tier === 'premium' && styles.partnerTextPremium,
                  ]}>
                    {data.partner_tier === 'premium' ? 'Partner Premium' : 'Partner'}
                  </Text>
                </View>
              )}
              <View style={[
                styles.typeBadge,
                data.type === 'negeri' ? styles.typeBadgeNegeri : styles.typeBadgeSwasta,
              ]}>
                <Text style={[
                  styles.typeText,
                  data.type === 'negeri' ? styles.typeTextNegeri : styles.typeTextSwasta,
                ]}>
                  PT{data.type === 'negeri' ? 'N' : 'S'}
                </Text>
              </View>
            </View>

            <Text style={styles.heroName}>{data.name}</Text>
            <Text style={styles.heroShort}>{data.short_name}</Text>
          </View>
        </View>

        {/* ── Info chips ── */}
        <View style={styles.infoRow}>
          <View style={styles.infoChip}>
            <Ionicons name="location-outline" size={14} color={colors.primary} />
            <Text style={styles.infoChipText}>{data.city}, {data.province}</Text>
          </View>
          <View style={styles.infoChip}>
            <Ionicons name="book-outline" size={14} color={colors.primary} />
            <Text style={styles.infoChipText}>{data.majors.length} jurusan</Text>
          </View>
        </View>

        {/* ── Bookmark banner (when saved) ── */}
        {bookmarked && (
          <View style={styles.savedBanner}>
            <Ionicons name="bookmark" size={16} color={colors.primary} />
            <Text style={styles.savedBannerText}>Tersimpan di Kampus Favorit</Text>
            <Pressable onPress={handleToggleBookmark} hitSlop={8}>
              <Text style={styles.savedBannerRemove}>Hapus</Text>
            </Pressable>
          </View>
        )}

        {/* ── Description ── */}
        {data.description && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Tentang</Text>
            <Text style={styles.description}>{data.description}</Text>
          </View>
        )}

        {/* ── Contact / Website ── */}
        {(data.website || data.email || data.phone) && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Kontak</Text>
            <View style={styles.contactCard}>
              {data.website && (
                <Pressable
                  style={styles.contactRow}
                  onPress={() => Linking.openURL(data.website!)}
                >
                  <Ionicons name="globe-outline" size={16} color={colors.primary} />
                  <Text style={styles.contactLink} numberOfLines={1}>{data.website}</Text>
                  <Ionicons name="open-outline" size={14} color={colors.textMuted} />
                </Pressable>
              )}
              {data.email && (
                <View style={[styles.contactRow, data.website && styles.contactRowBorder]}>
                  <Ionicons name="mail-outline" size={16} color={colors.textSub} />
                  <Text style={styles.contactText} numberOfLines={1}>{data.email}</Text>
                </View>
              )}
              {data.phone && (
                <View style={[styles.contactRow, (data.website || data.email) && styles.contactRowBorder]}>
                  <Ionicons name="call-outline" size={16} color={colors.textSub} />
                  <Text style={styles.contactText}>{data.phone}</Text>
                </View>
              )}
            </View>
          </View>
        )}

        {/* ── Majors ── */}
        <View style={styles.section}>
          <View style={styles.majorsSectionHeader}>
            <Text style={styles.sectionTitle}>Daftar Jurusan</Text>
            <Text style={styles.majorsCount}>{data.majors.length} program studi</Text>
          </View>

          {data.majors.length > 8 && (
            <View style={styles.majorSearch}>
              <Ionicons name="search-outline" size={15} color={colors.textMuted} />
              <TextInput
                style={styles.majorSearchInput}
                placeholder="Cari jurusan..."
                placeholderTextColor={colors.textMuted}
                value={searchMajor}
                onChangeText={setSearchMajor}
              />
              {searchMajor.length > 0 && (
                <Pressable onPress={() => setSearchMajor('')} hitSlop={8}>
                  <Ionicons name="close-circle" size={16} color={colors.textMuted} />
                </Pressable>
              )}
            </View>
          )}

          {Object.entries(byFaculty).map(([faculty, ms]) => (
            <View key={faculty} style={styles.facultyGroup}>
              <Text style={styles.facultyLabel}>{faculty}</Text>
              {ms.map((m) => (
                <View key={m.id} style={styles.majorItem}>
                  <Text style={styles.majorItemName}>{m.name}</Text>
                  <View style={styles.majorItemRiasec}>
                    {m.riasec_codes.slice(0, 3).map((code) => {
                      const rc = RIASEC_COLOR[code] ?? { bg: colors.primaryMuted, color: colors.primary }
                      return (
                        <View key={code} style={[styles.riasecChip, { backgroundColor: rc.bg }]}>
                          <Text style={[styles.riasecText, { color: rc.color }]}>{code}</Text>
                        </View>
                      )
                    })}
                  </View>
                </View>
              ))}
            </View>
          ))}
        </View>

        {/* ── Save / Website CTAs ── */}
        <View style={styles.ctaRow}>
          <Pressable
            style={[styles.ctaBookmark, bookmarked && styles.ctaBookmarkActive]}
            onPress={handleToggleBookmark}
            disabled={bookmarkLoading}
          >
            {bookmarkLoading
              ? <ActivityIndicator size="small" color={bookmarked ? colors.white : colors.primary} />
              : <Ionicons
                  name={bookmarked ? 'bookmark' : 'bookmark-outline'}
                  size={18}
                  color={bookmarked ? colors.white : colors.primary}
                />
            }
            <Text style={[styles.ctaBookmarkText, bookmarked && styles.ctaBookmarkTextActive]}>
              {bookmarked ? 'Tersimpan' : 'Simpan'}
            </Text>
          </Pressable>

          {data.website && (
            <Pressable
              style={styles.ctaWebsite}
              onPress={() => Linking.openURL(data.website!)}
            >
              <Ionicons name="globe-outline" size={18} color={colors.white} />
              <Text style={styles.ctaWebsiteText}>Website Resmi</Text>
            </Pressable>
          )}
        </View>

      </ScrollView>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  centered: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 16, padding: 24 },

  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 20, paddingTop: 4, paddingBottom: 12,
    borderBottomWidth: 1, borderBottomColor: colors.border,
  },
  headerTitle: { flex: 1, textAlign: 'center', fontSize: 18, fontFamily: fonts.bold, color: colors.text, marginHorizontal: 8 },
  backBtn: { width: 36, height: 36, borderRadius: 10, backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center' },
  bookmarkBtn: { width: 36, height: 36, borderRadius: 10, backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center', borderWidth: 1.5, borderColor: colors.border },
  bookmarkBtnActive: { backgroundColor: colors.primary, borderColor: colors.primary },

  content: { padding: 24, gap: 20, paddingBottom: 64 },

  heroCard: {
    backgroundColor: colors.surface, borderRadius: 16, padding: 20,
    flexDirection: 'row', gap: 16, alignItems: 'flex-start',
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.06, shadowRadius: 12, elevation: 3,
  },
  heroLogo: {
    width: 72, height: 72, borderRadius: 16,
    backgroundColor: colors.primaryLight, alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  heroLogoText: { fontSize: 28, fontFamily: fonts.extraBold, color: colors.primary },
  heroInfo: { flex: 1, gap: 6 },
  heroNameRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  heroName: { fontSize: 18, fontFamily: fonts.extraBold, color: colors.text, lineHeight: 24 },
  heroShort: { fontSize: 13, fontFamily: fonts.medium, color: colors.textSub },

  partnerBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6, backgroundColor: colors.primaryMuted },
  partnerBadgePremium: { backgroundColor: '#FFFBEB' },
  partnerText: { fontSize: 10, fontFamily: fonts.bold, color: colors.primary },
  partnerTextPremium: { color: '#F59E0B' },
  typeBadge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6 },
  typeBadgeNegeri: { backgroundColor: '#EEF2FF' },
  typeBadgeSwasta: { backgroundColor: '#FFF3CD' },
  typeText: { fontSize: 10, fontFamily: fonts.bold },
  typeTextNegeri: { color: colors.primary },
  typeTextSwasta: { color: '#D97706' },

  infoRow: { flexDirection: 'row', gap: 10, flexWrap: 'wrap' },
  infoChip: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    backgroundColor: colors.surface, borderRadius: 10,
    paddingHorizontal: 12, paddingVertical: 8,
    borderWidth: 1, borderColor: colors.border,
  },
  infoChipText: { fontSize: 13, fontFamily: fonts.medium, color: colors.textSub },

  savedBanner: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    backgroundColor: colors.primaryMuted, borderRadius: 10,
    paddingHorizontal: 14, paddingVertical: 10,
  },
  savedBannerText: { flex: 1, fontSize: 13, fontFamily: fonts.medium, color: colors.primary },
  savedBannerRemove: { fontSize: 13, fontFamily: fonts.bold, color: colors.error },

  section: { gap: 12 },
  sectionTitle: { fontSize: 16, fontFamily: fonts.bold, color: colors.text },
  description: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub, lineHeight: 22 },

  contactCard: { backgroundColor: colors.surface, borderRadius: 12, borderWidth: 1, borderColor: colors.border, overflow: 'hidden' },
  contactRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingHorizontal: 16, paddingVertical: 14 },
  contactRowBorder: { borderTopWidth: 1, borderTopColor: colors.border },
  contactLink: { flex: 1, fontSize: 14, fontFamily: fonts.medium, color: colors.primary },
  contactText: { flex: 1, fontSize: 14, fontFamily: fonts.regular, color: colors.textSub },

  majorsSectionHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  majorsCount: { fontSize: 12, fontFamily: fonts.medium, color: colors.textMuted },

  majorSearch: {
    flexDirection: 'row', alignItems: 'center', gap: 10,
    backgroundColor: colors.surface, borderRadius: 10,
    paddingHorizontal: 14, paddingVertical: 10,
    borderWidth: 1, borderColor: colors.border,
  },
  majorSearchInput: { flex: 1, fontSize: 14, fontFamily: fonts.regular, color: colors.text },

  facultyGroup: { gap: 2 },
  facultyLabel: {
    fontSize: 11, fontFamily: fonts.bold, color: colors.textMuted,
    letterSpacing: 0.8, textTransform: 'uppercase',
    paddingTop: 8, paddingBottom: 4,
    borderBottomWidth: 1, borderBottomColor: colors.border,
    marginBottom: 4,
  },
  majorItem: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingVertical: 10, gap: 12,
    borderBottomWidth: 1, borderBottomColor: colors.border,
  },
  majorItemName: { flex: 1, fontSize: 14, fontFamily: fonts.medium, color: colors.text },
  majorItemRiasec: { flexDirection: 'row', gap: 4 },
  riasecChip: { width: 22, height: 22, borderRadius: 6, alignItems: 'center', justifyContent: 'center' },
  riasecText: { fontSize: 10, fontFamily: fonts.bold },

  ctaRow: { flexDirection: 'row', gap: 12 },
  ctaBookmark: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    borderRadius: 12, paddingVertical: 14,
    backgroundColor: colors.surface, borderWidth: 1.5, borderColor: colors.primary,
  },
  ctaBookmarkActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  ctaBookmarkText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.primary },
  ctaBookmarkTextActive: { color: colors.white },
  ctaWebsite: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    backgroundColor: colors.primary, borderRadius: 12, paddingVertical: 14,
  },
  ctaWebsiteText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.white },

  errorText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub, textAlign: 'center' },
  retryBtn: { paddingHorizontal: 24, paddingVertical: 10, backgroundColor: colors.primaryLight, borderRadius: 10 },
  retryText: { fontSize: 14, fontFamily: fonts.semiBold, color: colors.primary },
})
